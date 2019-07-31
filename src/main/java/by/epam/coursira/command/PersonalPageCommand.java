package by.epam.coursira.command;

import by.epam.coursira.entity.Lecture;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.PersonalModel;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.service.UserService;
import by.epam.coursira.servlet.CoursiraJspPath;
import by.epam.coursira.servlet.CoursiraUrlPatterns;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PersonalPageCommand extends CommandAbstract {
  private static final Logger logger = LogManager.getLogger();

  private static final int RECORD_ON_PAGE_LIMIT = 3;
  private final CourseService courseService;
  private final UserService userService;

  public PersonalPageCommand(CourseService courseService, UserService userService) {
    super(CoursiraUrlPatterns.PERSONAL);
    this.courseService = courseService;
    this.userService = userService;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws CommandException, ClientCommandException, PageNotFoundException {
    logger.debug("In PersonalPageCommand");
    switch (request.getMethod()) {
      case "GET":
        return getPersonal(principal, request.getParameterMap());
      case "POST":
        throw new PageNotFoundException();
      default:
        throw new CommandException("Unknown method invoked.");
    }
  }

  private CommandResult getPersonal(Principal principal, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    PersonalModel personalModel = new PersonalModel();
    int pageIndex = CommandUtils.parseOptionalInt(queryParams, "page").orElse(1);
    final int offset = (pageIndex - 1) * RECORD_ON_PAGE_LIMIT;
    try {

      personalModel.setPrincipal(principal);
      personalModel.setCourseAmount(courseService.countCourses(principal));
      // get schedule
      List<Lecture> schedule =
          courseService.viewSchedule(principal, RECORD_ON_PAGE_LIMIT + 1, offset);
      personalModel.setHasNextPage(CommandUtils.trimToLimit(schedule, RECORD_ON_PAGE_LIMIT));
      personalModel.setSchedule(schedule);
      personalModel.setCurrentPageIndex(pageIndex);
    } catch (ServiceException e) {
      throw new CommandException(e);
    } catch (ClientServiceException e) {
      return new CommandResult(CoursiraUrlPatterns.PAGE_NOT_FOUND);
    }

    return new CommandResult(CoursiraJspPath.PERSONAL, personalModel);
  }
}
