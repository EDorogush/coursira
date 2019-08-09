package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.LoginModel;
import by.epam.coursira.service.PrincipalService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class is intended to process client's requests to resource corresponding to "/login" pattern.
 * When POST request executed, request parameters must contain "login" and "password" values.
 * Otherwise {@link ClientCommandException} will be thrown.
 */
public class LoginCommand implements Command {
  private static final Logger logger = LogManager.getLogger();
  /**
   * Field is used via urlPattern() * method in {@link CommandFactory} class to distinguish
   * request's URL and to delegate request to * current Command.
   */
  private static final Pattern resourcePattern = Pattern.compile("/login");

  /** The URL of page, client is redirected to after request processing */
  private static final String URL_TO_REDIRECT = "/";

  private static final String REQUEST_PARAMETER_LOGIN = "login";
  private static final String REQUEST_PARAMETER_PASSWORD = "password";
  private static final String PARSE_PARAMETER_EXCEPTION_MESSAGE = "Value < %s > must be define";
  private final PrincipalService principalService;

  public LoginCommand(PrincipalService principalService) {
    this.principalService = principalService;
  }

  @Override
  public Pattern urlPattern() {
    return resourcePattern;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws ClientCommandException, CommandException {
    logger.debug("In LoginCommand");
    switch (request.getMethod()) {
      case "POST":
        return postLogin(principal, request.getParameterMap());
      case "GET":
        return getLogin(principal);
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  /**
   * This method is used to process GET method of request which is being processing by current
   * Command. It fills {@link LoginModel} by {@link Principal} specified in arguments. LoginModel
   * will be used by JSP as response to client's request.
   *
   * @param principal current Principal.
   * @return {@link CommandResult} object filled by path to desired JSP file and filled model for
   *     JSP page.
   */
  private CommandResult getLogin(Principal principal) {
    LoginModel loginModel = new LoginModel();
    loginModel.setPrincipal(principal);
    return new CommandResult(CoursiraJspPath.LOGIN, loginModel);
  }

  /**
   * This method is used to process POST method of request which is being processing by current
   * Command. It parses from request parameters "login" and "password" values to {@link String},
   * tries to update current {@link Principal}. When it is not possible, explanatory message is sent
   * to client.
   *
   * @param principal current Principal.
   * @return {@link CommandResult} object filled by {@code URL_TO_REDIRECT} when login procedure
   *     succeed, or filled by by path to desired JSP file and filled model for JSP page when login
   *     procedure fails.
   * @throws ClientCommandException when attempt to parse "login" and "password" parameters fails.
   * @throws CommandException when server trouble occurs.
   */
  private CommandResult postLogin(Principal principal, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    String loginValue =
        CommandUtils.parseOptionalString(queryParams, REQUEST_PARAMETER_LOGIN)
            .orElseThrow(
                () ->
                    new ClientCommandException(
                        String.format(PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_LOGIN)));
    String passwordValue =
        CommandUtils.parseOptionalString(queryParams, REQUEST_PARAMETER_PASSWORD)
            .orElseThrow(
                () ->
                    new ClientCommandException(
                        String.format(
                            PARSE_PARAMETER_EXCEPTION_MESSAGE, REQUEST_PARAMETER_PASSWORD)));
    logger.debug("login {}, password {}", loginValue, passwordValue);
    try {
      Principal current =
          principalService.verifyPrincipleByPass(principal, loginValue, passwordValue);
      logger.debug("user login: {}", current::toString);
      return new CommandResult(URL_TO_REDIRECT);
    } catch (ClientServiceException e) {
      // return jsp with error message
      LoginModel loginModel = new LoginModel();
      loginModel.setPrincipal(principal);
      loginModel.setErrorMessage(e.getMessage());
      return new CommandResult(CoursiraJspPath.LOGIN, loginModel);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
  }
}
