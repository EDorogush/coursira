package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.exception.AccessDeniedException;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.UserUpdateModel;
import by.epam.coursira.service.UserService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
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
  private static final String RESOURCE_BUNDLE_ERROR_MESSAGE = "errorMessages";
  private static final String RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED = "ACCESS_DENIED";
  private static final String PARSE_PARAMETER_EXCEPTION_MESSAGE = "Value < %s > must be define";

  private static final String REQUEST_PARAMETER_FIRST_NAME = "firstName";
  private static final String REQUEST_PARAMETER_LAST_NAME = "lastName";
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
      throws ClientCommandException, CommandException {
    logger.debug("In PersonalUpdateCommand");
    switch (request.getMethod()) {
      case "GET":
        return getPersonalUpdate(principal);
      case "POST":
        String referer = CommandUtils.getReferer(request);
        if (request.getContentType().contains("multipart/form-data")) {
          final Part filePart;
          try {
            filePart =
                Optional.ofNullable(request.getPart("file"))
                    .orElseThrow(() -> new ClientCommandException("File can't be null "));
            return postImageUpdate(principal, filePart, referer);
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

  private CommandResult getPersonalUpdate(Principal principal) throws ClientCommandException {
    if (principal.getUser().getRole() == Role.ANONYMOUS) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle =
          ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, Locale.getDefault());
      throw new ClientCommandException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
    }
    UserUpdateModel updateModel = new UserUpdateModel();
    updateModel.setPrincipal(principal);
    return new CommandResult(CoursiraJspPath.UPDATE_PERSONAL, updateModel);
  }

  private CommandResult postPersonalUpdate(Principal principal, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    String firstName =
        CommandUtils.parseOptionalString(queryParams, REQUEST_PARAMETER_FIRST_NAME)
            .orElseThrow(
                () ->
                    new ClientCommandException(
                        String.format(
                            PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_FIRST_NAME)));

    String lastName =
        CommandUtils.parseOptionalString(queryParams, REQUEST_PARAMETER_LAST_NAME)
            .orElseThrow(
                () ->
                    new ClientCommandException(
                        String.format(
                            PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_LAST_NAME)));

    Integer age = CommandUtils.parseOptionalInt(queryParams, "age").orElse(null);
    String interests = CommandUtils.parseOptionalString(queryParams, "interest").orElse(null);
    String organization =
        CommandUtils.parseOptionalString(queryParams, "organization").orElse(null);
    try {
      userService.updateUserData(principal, firstName, lastName, age, organization, interests);
      return new CommandResult(URL_TO_REDIRECT);
    } catch (ClientServiceException e) {
      UserUpdateModel userUpdateModel = new UserUpdateModel();
      userUpdateModel.setPrincipal(principal);
      userUpdateModel.setErrorDataMessage(e.getMessage());
      return new CommandResult(CoursiraJspPath.UPDATE_PERSONAL, userUpdateModel);

    } catch (AccessDeniedException e) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle =
          ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, Locale.getDefault());
      throw new ClientCommandException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }

  private CommandResult postImageUpdate(Principal principal, Part part, String referer)
      throws CommandException, ClientCommandException {

    try {
      userService.updateUserPhoto(principal, part);
      return new CommandResult(referer);
    } catch (ClientServiceException e) {
      logger.error("ClientServiceException", e);
      UserUpdateModel userUpdateModel = new UserUpdateModel();
      userUpdateModel.setErrorImageMessage(e.getMessage());
      userUpdateModel.setPrincipal(principal);
      return new CommandResult(CoursiraJspPath.UPDATE_PERSONAL, userUpdateModel);
    } catch (AccessDeniedException e) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle =
          ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, Locale.getDefault());
      throw new ClientCommandException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }
}
