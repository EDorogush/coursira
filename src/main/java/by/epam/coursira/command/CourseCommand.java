package by.epam.coursira.command;

import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecturer;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.exception.AccessDeniedException;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.CourseModel;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.service.UserService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class is intended to process client's requests to resource corresponding to "/courses" pattern.
 * Private field {@link Pattern} is used via urlPattern() method in {@link CommandFactory} class to
 * distinguish request's URL and to delegate request to current Command.
 */
public class CourseCommand implements Command {
  private static final Logger logger = LogManager.getLogger();
  private static final Pattern RESOURCE_PATTERN = Pattern.compile("/courses");
  private static final String REQUEST_PARAMETER_PAGE = "page";
  private static final String REQUEST_PARAMETER_PERSONAL = "personal";
  private static final String REQUEST_PARAMETER_LECTURER_ID = "lecturerId";
  private final int paginationLimit;
  private final CourseService courseService;
  private final UserService userService;

  public CourseCommand(CourseService courseService, UserService userService, int paginationLimit) {
    this.courseService = courseService;
    this.userService = userService;
    this.paginationLimit = paginationLimit;
  }

  @Override
  public Pattern urlPattern() {
    return RESOURCE_PATTERN;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws ClientCommandException, PageNotFoundException, CommandException {
    logger.debug("In CourseCommand");
    switch (request.getMethod()) {
      case "GET":
        return getCourse(principal, request.getParameterMap());
      case "POST":
        throw new PageNotFoundException();
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  private CommandResult getCourse(Principal principal, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {

    int pageIndex = CommandUtils.parseOptionalInt(queryParams, REQUEST_PARAMETER_PAGE).orElse(1);
    boolean isPersonal =
        CommandUtils.parseOptionalBoolean(queryParams, REQUEST_PARAMETER_PERSONAL).orElse(false);
    Optional<Integer> lecturerId =
        CommandUtils.parseOptionalInt(queryParams, REQUEST_PARAMETER_LECTURER_ID);

    CourseModel courseModel = new CourseModel();
    courseModel.setPrincipal(principal);
    final int offset = (pageIndex - 1) * paginationLimit;
    final List<Course> courses;
    if (lecturerId.isPresent() && isPersonal) {
      throw new ClientCommandException(
          "Wrong request parameters. <personal> and <lecturerId> can't be defined simultaneously.");
    }
    try {
      if (lecturerId.isPresent()) {
        // when any user looks information about some lecturer
        // ready courses only
        courses =
            courseService.viewCoursesByLectureId(
                principal, lecturerId.get(), paginationLimit + 1, offset);
        // fetch / read / retrieve
        Lecturer lecturer = userService.defineLecturerNameById(principal, lecturerId.get());
        courseModel.setLecturerCourses(true);
        courseModel.setLecturer(lecturer);
      } else if (isPersonal && principal.getUser().getRole() != Role.ANONYMOUS) {
        /* get user only courses */
        courses = courseService.viewCoursesPersonal(principal, paginationLimit + 1, offset);
        courseModel.setPersonal(true);
      } else {
        /* get all courses */
        courses = courseService.viewCourses(principal, paginationLimit + 1, offset);
      }
    } catch (ClientServiceException | AccessDeniedException e) {
      throw new ClientCommandException(e);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    logger.debug(" course's records: {}", courses.size());
    // filling model for JSP
    courseModel.setHasNextPage(CommandUtils.trimToLimit(courses, paginationLimit));
    courseModel.setCourses(courses);
    courseModel.setCurrentPageIndex(pageIndex);
    return new CommandResult(CoursiraJspPath.COURSES, courseModel);
  }
}
