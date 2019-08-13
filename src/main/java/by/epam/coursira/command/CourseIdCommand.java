package by.epam.coursira.command;

import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.CourseDetailModel;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class is intended to process client's requests to resource corresponding to
 * "/courses/([^/?[A-Z]]+)(\\?.*)?" pattern.
 */
public class CourseIdCommand implements Command {
  public static final Logger logger = LogManager.getLogger();
  private static final Pattern resourcePattern = Pattern.compile("/courses/([^/?[A-Z]]+)(\\?.*)?");
  private static final String REQUEST_PARAMETER_PAGE = "page";

  private final int paginationLimit;
  private final CourseService courseService;

  public CourseIdCommand(CourseService courseService, int paginationLimit) {
    this.courseService = courseService;
    this.paginationLimit = paginationLimit;
  }

  @Override
  public Pattern urlPattern() {
    return resourcePattern;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws ClientCommandException, PageNotFoundException, CommandException {
    logger.debug("In CourseIdCommand");
    switch (request.getMethod()) {
      case "GET":
        final int courseId = CommandUtils.parseIdFromRequest(resourcePattern, request);
        logger.debug("course id parsed successfully {}", courseId);
        return getCourseDetails(principal, courseId, request.getParameterMap());
      case "POST":
        throw new PageNotFoundException();
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  private CommandResult getCourseDetails(
      Principal principal, int courseId, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    int pageIndex = CommandUtils.parseOptionalInt(queryParams, REQUEST_PARAMETER_PAGE).orElse(1);

    CourseDetailModel courseDetailModel = new CourseDetailModel();
    courseDetailModel.setPrincipal(principal);
    final int offset = (pageIndex - 1) * paginationLimit;
    final Course course;
    // get course by courseId
    try {
      course = courseService.viewCourseDetails(principal, courseId, paginationLimit + 1, offset);
      courseDetailModel.setAbleToJoin(!courseService.isStudentScheduleCross(principal, courseId));
      courseDetailModel.setInUserList(courseService.isInUserListCourse(principal, courseId));
    } catch (ClientServiceException e) {
      throw new ClientCommandException(e.getMessage());
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    courseDetailModel.setHasNextPage(
        CommandUtils.trimToLimit(course.getLectures(), paginationLimit));
    courseDetailModel.setCourse(course);
    courseDetailModel.setHasFreeSpot(course.getCapacity() > course.getStudentsAmount());
    courseDetailModel.setCurrentPageIndex(pageIndex);

    return new CommandResult(CoursiraJspPath.COURSE_DETAILS, courseDetailModel);
  }
}
