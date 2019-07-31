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
import by.epam.coursira.servlet.CoursiraUrlPatterns;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CourseIdCommand extends CommandAbstract {
  public static final Logger logger = LogManager.getLogger();
  private static final int RECORD_ON_PAGE_LIMIT = 10;
  private final CourseService courseService;

  public CourseIdCommand(CourseService courseService) {
    super(CoursiraUrlPatterns.COURSE_DETAILS);
    this.courseService = courseService;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws CommandException, ClientCommandException, PageNotFoundException {
    logger.debug("In CourseIdCommand");
    switch (request.getMethod()) {
      case "GET":
        final int courseId = CommandUtils.parseIdFromRequest(this.getPattern(), request);
        logger.debug("course id parsed successfully {}", courseId);
        return getCourseDetails(principal, courseId, request.getParameterMap());
      case "POST":
        throw new PageNotFoundException();
      default:
        throw new CommandException("Unknown method invoked.");
    }
  }

  private CommandResult getCourseDetails(
      Principal principal, int courseId, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    int pageIndex = CommandUtils.parseOptionalInt(queryParams, "page").orElse(1);

    CourseDetailModel courseDetailModel = new CourseDetailModel();
    courseDetailModel.setPrincipal(principal);
    final int offset = (pageIndex - 1) * RECORD_ON_PAGE_LIMIT;
    final Course course;
    // get course by courseId
    try {
      course =
          courseService.viewCourseDetails(principal, courseId, RECORD_ON_PAGE_LIMIT + 1, offset);
      courseDetailModel.setAbleToJoin(courseService.isScheduleCross(principal, courseId));
      courseDetailModel.setInUserList(courseService.isInUserListCourse(principal, courseId));
    } catch (ClientServiceException e) {
      throw new ClientCommandException(e);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    courseDetailModel.setHasNextPage(
        CommandUtils.trimToLimit(course.getLectures(), RECORD_ON_PAGE_LIMIT));
    courseDetailModel.setCourse(course);
    courseDetailModel.setHasFreeSpot(course.getCapacity() > course.getStudentsAmount());
    courseDetailModel.setCurrentPageIndex(pageIndex);

    return new CommandResult(CoursiraJspPath.COURSE_DETAILS, courseDetailModel);
  }
}
