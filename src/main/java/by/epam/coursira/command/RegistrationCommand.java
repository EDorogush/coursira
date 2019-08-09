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

/**
 * Class is intended to process client's requests to resource corresponding to "/sign" pattern. When
 * POST request executed, request parameters must contain "email" and "passwordFirst" ,
 * "passwordSecond", "firstName", "lastName", "role" values. Otherwise {@link
 * ClientCommandException} will be thrown. When registration process finish successfully, client is
 * redirected to "/registration?email=true" page with additional instructions. The registration
 * confirm message is also sent to users email with special request parameter "code" inside. See
 * {@link RegistrationConfirmCommand} for details.
 *
 * @see RegistrationConfirmCommand
 */
public class RegistrationCommand implements Command {
  private static final Logger logger = LogManager.getLogger();
  /**
   * Field is used via urlPattern() * method in {@link CommandFactory} class to distinguish
   * request's URL and to delegate request to * current Command.
   */
  private static final Pattern resourcePattern = Pattern.compile("/sign");
  /** The URL of page, client is redirected to after request processing */
  private static final String URL_TO_REDIRECT = "/registration";

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
        // Reconstruct original requesting URL
        String urlToGoFromEmail =
            request.getScheme() // http
                + "://"
                + request.getServerName() // localhost
                + ":"
                + request.getServerPort() // 8080
                + request.getContextPath()
                + URL_TO_REDIRECT;
        logger.debug("Full path {}", urlToGoFromEmail);
        return postRegister(principal, queryParams, urlToGoFromEmail);
      case "GET":
        return getRegister(principal);
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  /**
   * This method is used to process GET method of request which is being processing by current
   * Command. It fills {@link SignUpModel} by {@link Principal} specified in arguments. SignUpModel
   * will be used by JSP as response to client's request.
   *
   * @param principal current Principal.
   * @return {@link CommandResult} object filled by path to desired JSP file and filled model for
   *     JSP page.
   */
  private CommandResult getRegister(Principal principal) {
    SignUpModel signUpModel = new SignUpModel();
    signUpModel.setPrincipal(principal);
    return new CommandResult(CoursiraJspPath.SIGN_IN, signUpModel);
  }

  /**
   * This method is used to process POST method of request which is being processing by current
   * Command. It parses from request parameters "email", "passwordFirst", "passwordSecond",
   * "firstName", "lastName" values to {@link String} and "role" value to {@link Role}. After
   * parsing method tries register new. When it is not possible, explanatory message is sent to
   * client.
   *
   * @param principal current Principal.
   * @return {@link CommandResult} object filled by {@code URL_TO_REDIRECT} when sign-in procedure
   *     succeed, or filled by by path to desired JSP file and filled model for JSP page when
   *     sign-in * procedure fails.
   * @throws CommandException when server trouble occurs.
   */
  private CommandResult postRegister(
      Principal principal, Map<String, String[]> queryParams, String urlToGoFromEmail)
      throws CommandException, ClientCommandException {
    // check if fields is present
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
