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

/** Class is intended to process client's requests to resource corresponding to "/login" pattern. */
public class LoginCommand implements Command {
  private static final Logger logger = LogManager.getLogger();
  private static final Pattern resourcePattern = Pattern.compile("/login");
  private static final String URL_TO_REDIRECT = "/";
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
        Map<String, String[]> queryParams = request.getParameterMap();
        logger.info("queryParams {}", queryParams.keySet().toString());
        return postLogin(principal, request.getParameterMap());
      case "GET":
        return getLogin(principal);
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  private CommandResult getLogin(Principal principal) {
    LoginModel loginModel = new LoginModel();
    loginModel.setPrincipal(principal);
    return new CommandResult(CoursiraJspPath.LOGIN, loginModel);
  }

  private CommandResult postLogin(Principal principal, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    String loginValue =
        CommandUtils.parseOptionalString(queryParams, "login")
            .orElseThrow(() -> new ClientCommandException("Value <login> must be defined"));
    String passwordValue =
        CommandUtils.parseOptionalString(queryParams, "password")
            .orElseThrow(() -> new ClientCommandException("Value <password> must be defined"));
    logger.debug("login {}, password {}", loginValue, passwordValue);
    try {
      Principal current =
          principalService.verifyPrincipleByPass(principal, loginValue, passwordValue);
      logger.debug("user login: {}", current.toString());
      // return redirect
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
