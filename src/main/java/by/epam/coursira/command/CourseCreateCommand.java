package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.exception.AccessDeniedException;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.CourseCreateModel;
import by.epam.coursira.model.LoginModel;
import by.epam.coursira.service.CourseModificationService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class is intended to process client's requests to resource corresponding to "/courses/newCourse"
 * pattern. POST request requires parameters "title", "description", "capacity".
 */
public class CourseCreateCommand implements Command {
  public static final Logger logger = LogManager.getLogger();
  /**
   * Field is used via urlPattern() * method in {@link CommandFactory} class to distinguish
   * request's URL and to delegate request to * current Command.
   */
  private static final Pattern resourcePattern = Pattern.compile("/courses/newCourse");
  private static final String REQUEST_PARAMETER_TITLE = "title";
  private static final String REQUEST_PARAMETER_DESCRIPTION = "description";
  private static final String REQUEST_PARAMETER_CAPACITY = "capacity";

  private final CourseModificationService courseModificationService;

  public CourseCreateCommand(CourseModificationService courseModificationService) {
    this.courseModificationService = courseModificationService;
  }

  @Override
  public Pattern urlPattern() {
    return resourcePattern;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws ClientCommandException, CommandException {
    logger.debug("In CourseCreateCommand");
    switch (request.getMethod()) {
      case "POST":
        Map<String, String[]> queryParams = request.getParameterMap();
        logger.info("queryParams {}", queryParams.keySet().toString());
        return postCourseCreate(principal, request.getParameterMap());
      case "GET":
        return getCourseCreate(principal);
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  /**
   * This method is used to process POST method of request which is being processing by current
   * Command. It parses from request parameters "title", "description" values to {@link String}, and
   * "capacity" value to int and tries to create new course {@link Principal}. When it is not
   * possible, explanatory message is sent to client.
   *
   * @param principal current Principal.
   * @return {@link CommandResult} object filled by {@code URL_TO_REDIRECT} when login procedure
   *     succeed, or filled by by path to desired JSP file and filled model for JSP page when login
   *     procedure fails.
   * @throws ClientCommandException when attempt to parse parameters fails.
   * @throws CommandException when server trouble occurs.
   */
  private CommandResult postCourseCreate(Principal principal, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {

    String title =
        CommandUtils.parseOptionalString(queryParams, REQUEST_PARAMETER_TITLE)
            .orElseThrow(() -> new ClientCommandException("Value <title> must be defined"));
    String description =
        CommandUtils.parseOptionalString(queryParams, REQUEST_PARAMETER_DESCRIPTION)
            .orElseThrow(() -> new ClientCommandException("Value <description> must be define"));
    int capacity =
        CommandUtils.parseOptionalInt(queryParams, REQUEST_PARAMETER_CAPACITY)
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
    } catch (AccessDeniedException e) {
      throw new ClientCommandException(e.getMessage());
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }

  /**
   * This method is used to process GET method of request which is being processing by current
   * Command. It fills {@link CourseCreateModel by {@link Principal} specified in arguments. CourseCreateModel
   * will be used by JSP as response to client's request.
   *
   * @param principal current Principal.
   * @return {@link CommandResult} object filled by path to desired JSP file and filled model for
   *     JSP page.
   */
  private CommandResult getCourseCreate(Principal principal) throws ClientCommandException {
    if (principal.getUser().getRole() != Role.LECTURER) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", Locale.getDefault());
      throw new ClientCommandException(bundle.getString("ACCESS_DENIED"));
    }
    CourseCreateModel model = new CourseCreateModel();
    model.setPrincipal(principal);
    return new CommandResult(CoursiraJspPath.NEW_COURSE, model);
  }
}
