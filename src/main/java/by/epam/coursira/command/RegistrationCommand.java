package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.SignUpModel;
import by.epam.coursira.service.PrincipalService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class is intended to process client's requests to resource corresponding to "/sign" pattern. */
public class RegistrationCommand implements Command {
  private static final Logger logger = LogManager.getLogger();
  private static final Pattern resourcePattern = Pattern.compile("/sign");
  public static final String URL_TO_REDIRECT = "/registration";
  private final PrincipalService principalService;

  public RegistrationCommand(PrincipalService principalService) {
    this.principalService = principalService;
  }

  @Override
  public Pattern urlPattern() {
    return resourcePattern;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws ClientCommandException, CommandException {
    logger.debug("Registration command");
    switch (request.getMethod()) {
      case "POST":
        Map<String, String[]> queryParams = request.getParameterMap();
        logger.info("queryParams {}", queryParams.keySet().toString());
        // Reconstruct original requesting URL
        String urlToGoFromEmail =
            request.getScheme() // http
                + "://"
                + request.getServerName() // localhost
                + ":"
                + request.getServerPort() // 8080
                + request.getContextPath()
                + URL_TO_REDIRECT;
        logger.info("Full path {}", urlToGoFromEmail);
        return postRegister(principal, queryParams, urlToGoFromEmail);
      case "GET":
        return getRegister(principal);
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  private CommandResult getRegister(Principal principal) throws CommandException {
    logger.info("Register");
    SignUpModel signUpModel = new SignUpModel();
    signUpModel.setPrincipal(principal);
    return new CommandResult(CoursiraJspPath.SIGN_IN, signUpModel);
  }

  private CommandResult postRegister(
      Principal principal, Map<String, String[]> queryParams, String urlToGoFromEmail)
      throws CommandException, ClientCommandException {
    // check if fiels is present
    String email =
        CommandUtils.parseOptionalString(queryParams, "email")
            .orElseThrow(() -> new ClientCommandException("Value <email> must be defined"));
    String passwordFirst =
        CommandUtils.parseOptionalString(queryParams, "passwordFirst")
            .orElseThrow(() -> new ClientCommandException("Value <passwordFirst> must be define"));
    String passwordSecond =
        CommandUtils.parseOptionalString(queryParams, "passwordSecond")
            .orElseThrow(() -> new ClientCommandException("Value <passwordSecond> must be define"));
    String firstName =
        CommandUtils.parseOptionalString(queryParams, "firstName")
            .orElseThrow(() -> new ClientCommandException("Value <firstName> must be define"));
    String lastName =
        CommandUtils.parseOptionalString(queryParams, "lastName")
            .orElseThrow(() -> new ClientCommandException("Value <lastName> must be define"));
    String roleString =
        CommandUtils.parseOptionalString(queryParams, "role")
            .orElseThrow(() -> new ClientCommandException("Value <role> must be define"));
    final Role role;
    try {
      role = Role.valueOf(roleString);
    } catch (IllegalArgumentException e) {
      throw new ClientCommandException(
          "Value <role> must be one of:  " + Arrays.asList(Role.values()).toString());
    }
    logger.debug("values were taken from request");
    try {
      principalService.registerUser(
          principal,
          urlToGoFromEmail,
          email,
          passwordFirst,
          passwordSecond,
          firstName,
          lastName,
          role);
    } catch (ClientServiceException e) {
      SignUpModel signUpModel = new SignUpModel();
      signUpModel.setPrincipal(principal);
      signUpModel.setErrorDataMessage(e.getMessage());
      signUpModel.setEmail(email);
      signUpModel.setFirstName(firstName);
      signUpModel.setLastName(lastName);
      signUpModel.setRole(role);
      return new CommandResult(CoursiraJspPath.SIGN_IN, signUpModel);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    return new CommandResult(URL_TO_REDIRECT + "?email=true");
  }
}
