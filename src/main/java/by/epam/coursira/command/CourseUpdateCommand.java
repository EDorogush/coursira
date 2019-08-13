package by.epam.coursira.command;

import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecturer;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.AccessDeniedException;
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
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
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
  private static final String PAGE_TO_REDIRECT_PATTERN = "/courses/";
  private static final String REQUEST_PARAMETER_UPDATE_COURSE_DATA = "updateCourseData";
  private static final String REQUEST_PARAMETER_UPDATE_LECTURE = "updateLecture";
  private static final String REQUEST_PARAMETER_DELETE_LECTURE = "deleteLecture";
  private static final String REQUEST_PARAMETER_NEW_LECTURE = "newLecture";
  private static final String REQUEST_PARAMETER_INVITE_LECTURER = "inviteLecturer";
  private static final String REQUEST_PARAMETER_DELETE_LECTURER = "deleteLecturer";
  private static final String REQUEST_PARAMETER_ACTIVATE_COURSE = "activateCourse";
  private static final String REQUEST_PARAMETER_TITLE = "title";
  private static final String REQUEST_PARAMETER_DESCRIPTION = "description";
  private static final String REQUEST_PARAMETER_CAPACITY = "capacity";
  private static final String REQUEST_PARAMETER_LECTURER_ID = "lecturerId";
  private static final String REQUEST_PARAMETER_LECTURE_ID = "lectureId";
  private static final String REQUEST_PARAMETER_LECTURE_DESCRIPTION = "lectureDescription";
  private static final String REQUEST_PARAMETER_LECTURE_DAY = "lectureDay";
  private static final String REQUEST_PARAMETER_LECTURE_START_TIME = "lectureStartTime";
  private static final String REQUEST_PARAMETER_LECTURE_END_TIME = "lectureEndTime";
  private static final String PARSE_PARAMETER_EXCEPTION_MESSAGE = "Value < %s > must be define";

  private static final String RESOURCE_BUNDLE_EXCEPTION_MESSAGE = "errorMessages";
  private static final String RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED = "ACCESS_DENIED";

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
    switch (request.getMethod()) {
      case "POST":
        String referer = request.getServletPath();
        logger.debug("referer is {}", referer);
        Map<String, String[]> queryParams = request.getParameterMap();
        if (CommandUtils.parseOptionalBoolean(queryParams, REQUEST_PARAMETER_UPDATE_COURSE_DATA)
            .orElse(false)) {
          return postCourseUpdate(principal, courseId, queryParams, referer);
        }
        if (CommandUtils.parseOptionalBoolean(queryParams, REQUEST_PARAMETER_UPDATE_LECTURE)
            .orElse(false)) {
          return postLectureUpdate(principal, courseId, queryParams, referer);
        }
        if (CommandUtils.parseOptionalBoolean(queryParams, REQUEST_PARAMETER_DELETE_LECTURE)
            .orElse(false)) {
          return postLectureDelete(principal, queryParams, referer);
        }
        if (CommandUtils.parseOptionalBoolean(queryParams, REQUEST_PARAMETER_NEW_LECTURE)
            .orElse(false)) {
          return postLectureCreate(principal, courseId, queryParams, referer);
        }
        if (CommandUtils.parseOptionalBoolean(queryParams, REQUEST_PARAMETER_INVITE_LECTURER)
            .orElse(false)) {
          return postInviteLecturer(principal, courseId, queryParams, referer);
        }
        if (CommandUtils.parseOptionalBoolean(queryParams, REQUEST_PARAMETER_DELETE_LECTURER)
            .orElse(false)) {
          return postDeleteLecturer(principal, courseId, queryParams, referer);
        }
        if (CommandUtils.parseOptionalBoolean(queryParams, REQUEST_PARAMETER_ACTIVATE_COURSE)
            .orElse(false)) {
          return postCourseSubmit(principal, courseId);
        }
        throw new ClientCommandException(
            "One of updateCourseData,updateLecture, deleteLecture, activateCourse, inviteLecturer, deleteLecturer must be define");

      case "GET":
        logger.debug("GET CourseUpdateCommand");
        return getCourseUpdate(principal, courseId);
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  private CommandResult postInviteLecturer(
      Principal principal, int courseId, Map<String, String[]> queryParams, String referer)
      throws CommandException, ClientCommandException {
    int invitedLecturerId =
        CommandUtils.parseOptionalInt(queryParams, REQUEST_PARAMETER_LECTURER_ID)
            .orElseThrow(
                () ->
                    new ClientCommandException(
                        String.format(
                            PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_LECTURER_ID)));
    try {
      courseModificationService.addLecturerToCourse(principal, courseId, invitedLecturerId);
      return new CommandResult(referer);
    } catch (ClientServiceException e) {
      logger.debug(e);
      CourseUpdateModel model = fillModelForView(principal, courseId);
      model.setErrorCourseDataMessage(e.getMessage());
      return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
    } catch (AccessDeniedException e) {
      throw new ClientCommandException(e);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }

  private CommandResult postDeleteLecturer(
      Principal principal, int courseId, Map<String, String[]> queryParams, String referer)
      throws CommandException, ClientCommandException {
    int lecturerId =
        CommandUtils.parseOptionalInt(queryParams, REQUEST_PARAMETER_LECTURER_ID)
            .orElseThrow(
                () ->
                    new ClientCommandException(
                        String.format(
                            PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_LECTURER_ID)));
    try {
      courseModificationService.deleteLecturerFromCourse(principal, courseId, lecturerId);
      return new CommandResult(referer);
    } catch (ClientServiceException e) {
      logger.debug(e);
      CourseUpdateModel model = fillModelForView(principal, courseId);
      model.setErrorCourseDataMessage(e.getMessage());
      return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
    } catch (AccessDeniedException e) {
      throw new ClientCommandException(e);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }

  private CommandResult postCourseUpdate(
      Principal principal, int courseId, Map<String, String[]> queryParams, String referer)
      throws CommandException, ClientCommandException {

    String title =
        CommandUtils.parseOptionalString(queryParams, REQUEST_PARAMETER_TITLE)
            .orElseThrow(
                () ->
                    new ClientCommandException(
                        String.format(PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_TITLE)));
    String description =
        CommandUtils.parseOptionalString(queryParams, REQUEST_PARAMETER_DESCRIPTION)
            .orElseThrow(
                () ->
                    new ClientCommandException(
                        String.format(
                            PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_DESCRIPTION)));
    int capacity =
        CommandUtils.parseOptionalInt(queryParams, REQUEST_PARAMETER_CAPACITY)
            .orElseThrow(
                () ->
                    new ClientCommandException(
                        String.format(
                            PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_CAPACITY)));
    logger.debug("values were taken from request");

    try {
      courseModificationService.updateCourse(principal, courseId, title, description, capacity);
      return new CommandResult(referer);
    } catch (ClientServiceException e) {
      logger.debug(e);
      CourseUpdateModel model = fillModelForView(principal, courseId);
      model.setErrorCourseDataMessage(e.getMessage());
      return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
    } catch (AccessDeniedException e) {
      throw new ClientCommandException(e);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }

  private CommandResult postLectureUpdate(
      Principal principal, int courseId, Map<String, String[]> queryParams, String referer)
      throws CommandException, ClientCommandException {
    int lectureId =
        CommandUtils.parseOptionalInt(queryParams, REQUEST_PARAMETER_LECTURE_ID)
            .orElseThrow(
                () ->
                    new ClientCommandException(
                        String.format(
                            PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_LECTURE_ID)));

    try {
      String description =
          CommandUtils.parseOptionalString(queryParams, REQUEST_PARAMETER_LECTURE_DESCRIPTION)
              .orElseThrow(
                  () ->
                      new ClientCommandException(
                          String.format(
                              PARSE_PARAMETER_EXCEPTION_MESSAGE,
                              REQUEST_PARAMETER_LECTURE_DESCRIPTION)));

      LocalDate date =
          CommandUtils.parseOptionalLocalDate(queryParams, REQUEST_PARAMETER_LECTURE_DAY)
              .orElseThrow(
                  () ->
                      new ClientCommandException(
                          String.format(
                              PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_LECTURE_DAY)));

      LocalTime begin =
          CommandUtils.parseOptionalLocalTime(queryParams, REQUEST_PARAMETER_LECTURE_START_TIME)
              .orElseThrow(
                  () ->
                      new ClientCommandException(
                          String.format(
                              PARSE_PARAMETER_EXCEPTION_MESSAGE,
                              REQUEST_PARAMETER_LECTURE_START_TIME)));

      LocalTime finish =
          CommandUtils.parseOptionalLocalTime(queryParams, REQUEST_PARAMETER_LECTURE_END_TIME)
              .orElseThrow(
                  () ->
                      new CommandException(
                          String.format(
                              PARSE_PARAMETER_EXCEPTION_MESSAGE,
                              REQUEST_PARAMETER_LECTURE_END_TIME)));

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
      return new CommandResult(referer);
    } catch (ClientServiceException e) {
      logger.debug(e);
      CourseUpdateModel model = fillModelForView(principal, courseId);
      model.setErrorCourseDataMessage(e.getMessage());
      return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
    } catch (AccessDeniedException e) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle =
          ResourceBundle.getBundle(RESOURCE_BUNDLE_EXCEPTION_MESSAGE, Locale.getDefault());
      throw new ClientCommandException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }

  private CommandResult postLectureCreate(
      Principal principal, int courseId, Map<String, String[]> queryParams, String referer)
      throws CommandException, ClientCommandException {
    try {
      String description =
          CommandUtils.parseOptionalString(queryParams, REQUEST_PARAMETER_LECTURE_DESCRIPTION)
              .orElseThrow(
                  () ->
                      new ClientCommandException(
                          String.format(
                              PARSE_PARAMETER_EXCEPTION_MESSAGE,
                              REQUEST_PARAMETER_LECTURE_DESCRIPTION)));

      LocalDate date =
          CommandUtils.parseOptionalLocalDate(queryParams, REQUEST_PARAMETER_LECTURE_DAY)
              .orElseThrow(
                  () ->
                      new ClientCommandException(
                          String.format(
                              PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_LECTURE_DAY)));

      LocalTime begin =
          CommandUtils.parseOptionalLocalTime(queryParams, REQUEST_PARAMETER_LECTURE_START_TIME)
              .orElseThrow(
                  () ->
                      new ClientCommandException(
                          String.format(
                              PARSE_PARAMETER_EXCEPTION_MESSAGE,
                              REQUEST_PARAMETER_LECTURE_START_TIME)));

      LocalTime finish =
          CommandUtils.parseOptionalLocalTime(queryParams, REQUEST_PARAMETER_LECTURE_END_TIME)
              .orElseThrow(
                  () ->
                      new ClientCommandException(
                          String.format(
                              PARSE_PARAMETER_EXCEPTION_MESSAGE,
                              REQUEST_PARAMETER_LECTURE_END_TIME)));

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
      return new CommandResult(referer);
    } catch (ClientServiceException e) {
      logger.debug(e);
      CourseUpdateModel model = fillModelForView(principal, courseId);
      model.setErrorCourseDataMessage(e.getMessage());
      return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
    } catch (AccessDeniedException e) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle =
          ResourceBundle.getBundle(RESOURCE_BUNDLE_EXCEPTION_MESSAGE, Locale.getDefault());
      throw new ClientCommandException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }

  private CommandResult postLectureDelete(
      Principal principal, Map<String, String[]> queryParams, String referer)
      throws CommandException, ClientCommandException {
    int lectureId =
        CommandUtils.parseOptionalInt(queryParams, REQUEST_PARAMETER_LECTURE_ID)
            .orElseThrow(
                () ->
                    new ClientCommandException(
                        String.format(
                            PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_LECTURE_ID)));
    try {
      courseModificationService.deleteLecture(principal, lectureId);
      return new CommandResult(referer);
    } catch (AccessDeniedException e) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle =
          ResourceBundle.getBundle(RESOURCE_BUNDLE_EXCEPTION_MESSAGE, Locale.getDefault());
      throw new ClientCommandException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }

  private CommandResult postCourseSubmit(Principal principal, int courseId)
      throws CommandException, ClientCommandException {
    try {
      courseModificationService.activateCourse(principal, courseId);
      return new CommandResult(PAGE_TO_REDIRECT_PATTERN + courseId);
    } catch (ClientServiceException e) {
      logger.debug(e);
      CourseUpdateModel model = fillModelForView(principal, courseId);
      model.setErrorCourseDataMessage(e.getMessage());
      return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
    } catch (AccessDeniedException e) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle =
          ResourceBundle.getBundle(RESOURCE_BUNDLE_EXCEPTION_MESSAGE, Locale.getDefault());
      throw new ClientCommandException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }

  private CommandResult getCourseUpdate(Principal principal, int courseId)
      throws CommandException, ClientCommandException {
    final boolean haveAccess;
    try {
      haveAccess = courseModificationService.isAllowToUpdateCourse(principal, courseId);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    if (!haveAccess) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle =
          ResourceBundle.getBundle(RESOURCE_BUNDLE_EXCEPTION_MESSAGE, Locale.getDefault());
      throw new ClientCommandException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
    }
    CourseUpdateModel model = fillModelForView(principal, courseId);
    return new CommandResult(CoursiraJspPath.COURSE_UPDATE, model);
  }

  private CourseUpdateModel fillModelForView(Principal principal, int courseId)
      throws ClientCommandException, CommandException {
    CourseUpdateModel model = new CourseUpdateModel();
    model.setPrincipal(principal);
    try {
      List<Lecturer> lecturers = userService.findAllLecturersList();
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
