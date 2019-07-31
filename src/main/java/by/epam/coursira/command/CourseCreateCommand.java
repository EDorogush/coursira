package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.CourseCreateModel;
import by.epam.coursira.service.CourseModificationService;
import by.epam.coursira.servlet.CoursiraJspPath;
import by.epam.coursira.servlet.CoursiraUrlPatterns;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CourseCreateCommand extends CommandAbstract {
  public static final Logger logger = LogManager.getLogger();
  private final CourseModificationService courseModificationService;

  public CourseCreateCommand(CourseModificationService courseModificationService) {
    super(CoursiraUrlPatterns.NEW_COURSE);
    this.courseModificationService = courseModificationService;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws CommandException, ClientCommandException {
    logger.debug("In CourseCreateCommand");
    if (principal.getUser().getRole() != Role.LECTURER) {
      throw new ClientCommandException("Access denied");
    }
    switch (request.getMethod()) {
      case "POST":
        Map<String, String[]> queryParams = request.getParameterMap();
        logger.info("queryParams {}", queryParams.keySet().toString());
        return postCourseCreate(principal, request.getParameterMap());
      case "GET":
        return getCourseCreate(principal);
      default:
        throw new CommandException("Unknown method invoked.");
    }
  }

  private CommandResult postCourseCreate(Principal principal, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {

    String title =
        CommandUtils.parseOptionalString(queryParams, "title")
            .orElseThrow(() -> new ClientCommandException("Value <title> must be defined"));
    String description =
        CommandUtils.parseOptionalString(queryParams, "description")
            .orElseThrow(() -> new ClientCommandException("Value <description> must be define"));
    int capacity =
        CommandUtils.parseOptionalInt(queryParams, "capacity")
            .orElseThrow(() -> new ClientCommandException("Value <capacity> must be define"));
    try {
      int courseId =
          courseModificationService.createCourse(principal, title, description, capacity);
      return new CommandResult(String.format("/courses/%d/update", courseId));
    } catch (ClientServiceException e) {
      logger.error(e.getMessage());
      // return with message
      CourseCreateModel model = new CourseCreateModel();
      model.setPrincipal(principal);
      model.setTitle(title);
      model.setDescription(description);
      model.setCapacity(capacity);
      model.setErrorDataMessage(e.getMessage());
      return new CommandResult(CoursiraJspPath.NEW_COURSE, model);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }

  private CommandResult getCourseCreate(Principal principal){

    CourseCreateModel model = new CourseCreateModel();
    model.setPrincipal(principal);
    return new CommandResult(CoursiraJspPath.NEW_COURSE, model);
  }
}
