package by.epam.coursira.command;

import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecturer;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.CourseModel;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.service.UserService;
import by.epam.coursira.servlet.CoursiraJspPath;
import by.epam.coursira.servlet.CoursiraUrlPatterns;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CourseCommand extends CommandAbstract {
  private static final Logger logger = LogManager.getLogger();
  private static final int RECORD_ON_PAGE_LIMIT = 3;
  private final CourseService courseService;
  private final UserService userService;

  public CourseCommand(CourseService courseService, UserService userService) {
    super(CoursiraUrlPatterns.COURSES);
    this.courseService = courseService;
    this.userService = userService;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws CommandException, ClientCommandException, PageNotFoundException {
    logger.debug("In CourseCommand");
    switch (request.getMethod()) {
      case "GET":
        return getCourse(principal, request.getParameterMap());
      case "POST":
        throw new PageNotFoundException();
      default:
        throw new CommandException("Unknown method invoked.");
    }
  }

  private CommandResult getCourse(Principal principal, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {

    int pageIndex = CommandUtils.parseOptionalInt(queryParams, "page").orElse(1);
    boolean isPersonal = CommandUtils.parseOptionalBoolean(queryParams, "personal").orElse(false);
    Optional<Integer> lecturerId = CommandUtils.parseOptionalInt(queryParams, "lecturerId");

    CourseModel courseModel = new CourseModel();
    courseModel.setPrincipal(principal);
    final int offset = (pageIndex - 1) * RECORD_ON_PAGE_LIMIT;
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
                principal, lecturerId.get(), RECORD_ON_PAGE_LIMIT + 1, offset);
        // fetch / read / retrieve
        Lecturer lecturer = userService.defineLecturerNameById(principal, lecturerId.get());
        courseModel.setLecturerCourses(true);
        courseModel.setLecturer(lecturer);
      } else if (isPersonal && principal.getUser().getRole() != Role.ANONYMOUS) {
        // get user only courses;
        courses = courseService.viewCoursesPersonal(principal, RECORD_ON_PAGE_LIMIT + 1, offset);
        courseModel.setPersonal(true);
      } else {
        // get all courses;
        courses = courseService.viewCourses(principal, RECORD_ON_PAGE_LIMIT + 1, offset);
      }
    } catch (ClientServiceException e) {
      throw new ClientCommandException(e);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    logger.debug(" course's records: {}", courses.size());
    // filling model for JSP
    courseModel.setHasNextPage(CommandUtils.trimToLimit(courses, RECORD_ON_PAGE_LIMIT));
    courseModel.setCourses(courses);
    courseModel.setCurrentPageIndex(pageIndex);
    return new CommandResult(CoursiraJspPath.COURSES, courseModel);
  }
}
