package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.LoginModel;
import by.epam.coursira.service.PrincipalService;
import by.epam.coursira.servlet.CoursiraJspPath;
import by.epam.coursira.servlet.CoursiraUrlPatterns;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginCommand extends CommandAbstract {
  private static final Logger logger = LogManager.getLogger();
  private final PrincipalService principalService;

  public LoginCommand(PrincipalService principalService) {
    super(CoursiraUrlPatterns.LOGIN);
    this.principalService = principalService;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws CommandException, ClientCommandException {
    logger.debug("In LoginCommand");
    switch (request.getMethod()) {
      case "POST":
        Map<String, String[]> queryParams = request.getParameterMap();
        logger.info("queryParams {}", queryParams.keySet().toString());
        return postLogin(principal, request.getParameterMap());
      case "GET":
        return getLogin(principal);
      default:
        throw new CommandException("Unknown method invoked.");
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
      return new CommandResult(CoursiraUrlPatterns.INDEX);
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
