package by.epam.coursira.command;

import by.epam.coursira.entity.Lecture;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.AccessDeniedException;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.PersonalModel;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Class is intended to process client's requests to resource corresponding to "/personal" pattern.
 */
public class PersonalPageCommand implements Command {
  private static final Logger logger = LogManager.getLogger();
  private static final Pattern resourcePattern = Pattern.compile("/personal");
  private final int paginationLimit;
  private final CourseService courseService;

  public PersonalPageCommand(CourseService courseService, int paginationLimit) {
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
    logger.debug("In PersonalPageCommand");
    switch (request.getMethod()) {
      case "GET":
        return getPersonal(principal, request.getParameterMap());
      case "POST":
        throw new PageNotFoundException();
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  private CommandResult getPersonal(Principal principal, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    PersonalModel personalModel = new PersonalModel();
    int pageIndex = CommandUtils.parseOptionalInt(queryParams, "page").orElse(1);
    final int offset = (pageIndex - 1) * paginationLimit;
    try {

      personalModel.setPrincipal(principal);
      personalModel.setCourseAmount(courseService.countCourses(principal));
      // get schedule
      List<Lecture> schedule = courseService.viewSchedule(principal, paginationLimit + 1, offset);
      personalModel.setHasNextPage(CommandUtils.trimToLimit(schedule, paginationLimit));
      personalModel.setSchedule(schedule);
      personalModel.setCurrentPageIndex(pageIndex);
    } catch (ServiceException e) {
      throw new CommandException(e);
    } catch (ClientServiceException | AccessDeniedException e) {
      throw new ClientCommandException(e);
    }

    return new CommandResult(CoursiraJspPath.PERSONAL, personalModel);
  }
}
