package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.UserUpdateModel;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.service.UserService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Class is intended to process client's requests to resource corresponding to "/personal/update"
 * pattern.
 */
public class PersonalUpdateCommand implements Command {
  private static final Logger logger = LogManager.getLogger();
  private static final Pattern resourcePattern = Pattern.compile("/personal/update");
  private static final String URL_TO_REDIRECT = "/personal";
  private final UserService userService;

  public PersonalUpdateCommand(UserService userService) {
    this.userService = userService;
  }

  @Override
  public Pattern urlPattern() {
    return resourcePattern;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws ClientCommandException, PageNotFoundException,CommandException {
    logger.debug("In PersonalUpdateCommand");
    switch (request.getMethod()) {
      case "GET":
        if (principal.getUser().getRole() == Role.ANONYMOUS) {
          throw new PageNotFoundException();
        }
        return getPersonalUpdate(principal);
      case "POST":
        if (request.getContentType().contains("multipart/form-data")) {
          final Part filePart;
          try {
            filePart =
                Optional.ofNullable(request.getPart("file"))
                    .orElseThrow(() -> new ClientCommandException("File can't be null "));
            return postImageUpdate(principal, filePart);
          } catch (IOException | ServletException e) {
            throw new CommandException(e);
          }
        } else {
          return postPersonalUpdate(principal, request.getParameterMap());
        }
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  private CommandResult getPersonalUpdate(Principal principal) {
    UserUpdateModel updateModel = new UserUpdateModel();
    updateModel.setPrincipal(principal);
    return new CommandResult(CoursiraJspPath.UPDATE_PERSONAL, updateModel);
  }

  private CommandResult postPersonalUpdate(Principal principal, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    // todo: ask about final init
    logger.info("checking forms");
    String firstName =
        CommandUtils.parseOptionalString(queryParams, "firstName")
            .orElseThrow(() -> new ClientCommandException("Value <FirstName> must be define"));

    String lastName =
        CommandUtils.parseOptionalString(queryParams, "lastName")
            .orElseThrow(() -> new ClientCommandException("Value <lastName> must be define"));

    Integer age = CommandUtils.parseOptionalInt(queryParams, "age").orElse(null);
    String interests = CommandUtils.parseOptionalString(queryParams, "interest").orElse(null);
    String organization =
        CommandUtils.parseOptionalString(queryParams, "organization").orElse(null);

    try {
      principal =
          userService.updateUserData(principal, firstName, lastName, age, organization, interests);
      return new CommandResult(URL_TO_REDIRECT);
    } catch (ClientServiceException e) {
      UserUpdateModel userUpdateModel = new UserUpdateModel();
      userUpdateModel.setPrincipal(principal);
      userUpdateModel.setErrorDataMessage(e.getMessage());
      return new CommandResult(CoursiraJspPath.UPDATE_PERSONAL, userUpdateModel);

    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }

  private CommandResult postImageUpdate(Principal principal, Part part) throws CommandException {
    UserUpdateModel userUpdateModel = new UserUpdateModel();
    try {
      principal = userService.updateUserPhoto(principal, part);
    } catch (ClientServiceException e) {
      logger.error("ClientServiceException", e);
      userUpdateModel.setErrorImageMessage(e.getMessage());
    } catch (ServiceException ex) {
      throw new CommandException(ex);
    }
    userUpdateModel.setPrincipal(principal);
    return new CommandResult(CoursiraJspPath.UPDATE_PERSONAL, userUpdateModel);
  }
}
