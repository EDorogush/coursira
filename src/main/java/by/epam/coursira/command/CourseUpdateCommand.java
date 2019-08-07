package by.epam.coursira.command;

import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecturer;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.CourseUpdateModel;
import by.epam.coursira.service.CourseModificationService;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.service.UserService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class is intended to process client's requests to resource corresponding to
 * "/courses/([^/?[A-Z]]+)/update" pattern.
 */
public class CourseUpdateCommand implements Command {
  public static final Logger logger = LogManager.getLogger();
  private static final Pattern resourcePattern = Pattern.compile("/courses/([^/?[A-Z]]+)/update");
  private final CourseModificationService courseModificationService;
  private final CourseService courseService;
  private final UserService userService;

  public CourseUpdateCommand(
      CourseModificationService courseModificationService,
      CourseService courseService,
      UserService userService) {
    this.courseModificationService = courseModificationService;
    this.courseService = courseService;
    this.userService = userService;
  }

  @Override
  public Pattern urlPattern() {
    return resourcePattern;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws ClientCommandException, CommandException {
    logger.debug("In CourseUpdateCommand");
    final int courseId = CommandUtils.parseIdFromRequest(resourcePattern, request);
    final boolean haveAccess;
    try {
      haveAccess = courseModificationService.isAllowToUpdateCourse(principal, courseId);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    if (!haveAccess) {
      throw new ClientCommandException("Access denied");
    }
    switch (request.getMethod()) {
      case "POST":
        logger.debug("POST CourseUpdateCommand");
        final CommandResult result;
        Map<String, String[]> queryParams = request.getParameterMap();
        boolean updateCourseData =
            CommandUtils.parseOptionalBoolean(queryParams, "updateCourseData").orElse(false);
        if (updateCourseData) {
          result = postCourseUpdate(principal, courseId, queryParams);
        } else {
          boolean updateLecture =
              CommandUtils.parseOptionalBoolean(queryParams, "updateLecture").orElse(false);
          if (updateLecture) {
            result = postLectureUpdate(principal, courseId, queryParams);
          } else {
            boolean deleteLecture =
                CommandUtils.parseOptionalBoolean(queryParams, "deleteLecture").orElse(false);
            if (deleteLecture) {
              result = postLectureDelete(principal, courseId, queryParams);
            } else {
              boolean newLecture =
                  CommandUtils.parseOptionalBoolean(queryParams, "newLecture").orElse(false);
              if (newLecture) {
                result = postLectureCreate(principal, courseId, queryParams);
              } else {
                boolean activateCourse =
                    CommandUtils.parseOptionalBoolean(queryParams, "activateCourse").orElse(false);
                if (activateCourse) {
                  result = postCourseSubmit(principal, courseId);
                } else
                  throw new ClientCommandException(
                      "one of updateCourseData,updateLecture, deleteLecture, activateCourse must be define");
              }
            }
          }
        }
        return result;

      case "GET":
        logger.debug("GET CourseUpdateCommand");
        return getCourseUpdate(principal, courseId);
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  private CommandResult postCourseUpdate(
      Principal principal, int courseId, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    // access already checked
    String title =
        CommandUtils.parseOptionalString(queryParams, "title")
            .orElseThrow(() -> new ClientCommandException("Value <title> must be defined"));
    String description =
        CommandUtils.parseOptionalString(queryParams, "description")
            .orElseThrow(() -> new ClientCommandException("Value <description> must be define"));
    int capacity =
        CommandUtils.parseOptionalInt(queryParams, "capacity")
            .orElseThrow(() -> new ClientCommandException("Value <capacity> must be define"));
    int invitedLecturerId = CommandUtils.parseOptionalInt(queryParams, "lecturerId").orElse(0);

    logger.debug("values were taken from request");

    CourseUpdateModel model = new CourseUpdateModel();
    // updateCourseData
    try {
      courseModificationService.updateCourse(principal, courseId, title, description, capacity);
      if (invitedLecturerId != 0) {
        courseModificationService.addLecturerToCourse(principal, courseId, invitedLecturerId);
      }
      // when finish update we need to view current page
      model = fillModelForView(principal, courseId);
    } catch (ClientServiceException e) {
      logger.error(e);
      model.setErrorCourseDataMessage(e.getMessage());
    } catch (ServiceException e) {
      throw new CommandException(e);
    }

    return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
  }

  private CommandResult postLectureUpdate(
      Principal principal, int courseId, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    int lectureId =
        CommandUtils.parseOptionalInt(queryParams, "lectureId")
            .orElseThrow(() -> new ClientCommandException("Value <lectureId> must be define"));

    CourseUpdateModel model = new CourseUpdateModel();
    try {
      if (!courseModificationService.isAllowToUpdateLecture(principal, lectureId)) {
        throw new ClientCommandException("Access denied");
      }
      String description =
          CommandUtils.parseOptionalString(queryParams, "lectureDescription")
              .orElseThrow(
                  () -> new ClientCommandException("Value <lectureDescription> must be define"));

      LocalDate date =
          CommandUtils.parseOptionalLocalDate(queryParams, "lectureDay")
              .orElseThrow(() -> new ClientCommandException("Value <lectureDay> must be define"));

      LocalTime begin =
          CommandUtils.parseOptionalLocalTime(queryParams, "lectureStartTime")
              .orElseThrow(
                  () -> new ClientCommandException("Value <lectureStartTime> must be define"));

      LocalTime finish =
          CommandUtils.parseOptionalLocalTime(queryParams, "lectureEndTime")
              .orElseThrow(() -> new CommandException("Value <lectureEndTime> must be define"));

      Instant lectureBegins =
          LocalDateTime.of(date, begin)
              .atOffset(principal.getSession().getZoneOffset())
              .toInstant();
      Instant lectureFinish =
          LocalDateTime.of(date, finish)
              .atOffset(principal.getSession().getZoneOffset())
              .toInstant();

      courseModificationService.updateLecture(
          principal, lectureId, courseId, description, lectureBegins, lectureFinish);
      // when finish update we need to view current page
      model = fillModelForView(principal, courseId);
    } catch (ClientServiceException e) {
      logger.error(e);
      model.setErrorCourseDataMessage(e.getMessage());
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
  }

  private CommandResult postLectureCreate(
      Principal principal, int courseId, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    CourseUpdateModel model = new CourseUpdateModel();
    try {
      String description =
          CommandUtils.parseOptionalString(queryParams, "lectureDescription")
              .orElseThrow(
                  () -> new ClientCommandException("Value <lectureDescription> must be define"));

      LocalDate date =
          CommandUtils.parseOptionalLocalDate(queryParams, "lectureDay")
              .orElseThrow(() -> new ClientCommandException("Value <lectureDay> must be define"));

      LocalTime begin =
          CommandUtils.parseOptionalLocalTime(queryParams, "lectureStartTime")
              .orElseThrow(
                  () -> new ClientCommandException("Value <lectureStartTime> must be define"));

      LocalTime finish =
          CommandUtils.parseOptionalLocalTime(queryParams, "lectureEndTime")
              .orElseThrow(
                  () -> new ClientCommandException("Value <lectureEndTime> must be define"));

      Instant lectureBegins =
          LocalDateTime.of(date, begin)
              .atOffset(principal.getSession().getZoneOffset())
              .toInstant();
      Instant lectureFinish =
          LocalDateTime.of(date, finish)
              .atOffset(principal.getSession().getZoneOffset())
              .toInstant();
      courseModificationService.createLecture(
          principal, courseId, description, lectureBegins, lectureFinish);

      model = fillModelForView(principal, courseId);
    } catch (ClientServiceException e) {
      logger.error(e);
      model.setErrorCourseDataMessage(e.getMessage());
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
  }

  private CommandResult postLectureDelete(
      Principal principal, int courseId, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    int lectureId =
        CommandUtils.parseOptionalInt(queryParams, "lectureId")
            .orElseThrow(() -> new ClientCommandException("Value <lectureId> must be define"));

    CourseUpdateModel model = new CourseUpdateModel();
    try {
      courseModificationService.deleteLecture(principal, lectureId);
      model = fillModelForView(principal, courseId);
    } catch (ClientServiceException e) {
      logger.error(e);
      model.setErrorCourseDataMessage(e.getMessage());
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
  }

  private CommandResult postCourseSubmit(Principal principal, int courseId)
      throws CommandException, ClientCommandException {
    CourseUpdateModel model = new CourseUpdateModel();
    try {
      courseModificationService.activateCourse(principal, courseId);
      model = fillModelForView(principal, courseId);
    } catch (ClientServiceException e) {
      logger.error(e);
      model.setErrorCourseDataMessage(e.getMessage());
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
  }

  private CommandResult getCourseUpdate(Principal principal, int courseId)
      throws CommandException, ClientCommandException {
    CourseUpdateModel model = fillModelForView(principal, courseId);
    return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
  }

  private CourseUpdateModel fillModelForView(Principal principal, int courseId)
      throws ClientCommandException, CommandException {
    CourseUpdateModel model = new CourseUpdateModel();
    model.setPrincipal(principal);
    try {
      List<Lecturer> lecturers = userService.findAllLecturersList(principal);
      Course course = courseService.viewCourseDetails(principal, courseId, Integer.MAX_VALUE, 0);
      model.setAllLecturers(lecturers);
      model.setCourse(course);

    } catch (ServiceException e) {
      throw new CommandException(e);
    } catch (ClientServiceException e) {
      throw new ClientCommandException(e);
    }
    return model;
  }
}
