package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.service.PrincipalService;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Class is intended to process client's requests to resource corresponding to "/logout" pattern.
 * This is supporting page and only POST method of request is possible. Request with GET method will
 * throw {@link PageNotFoundException}
 */
@Component
public class LogoutCommand implements Command {
  private static final Logger logger = LogManager.getLogger();
  /**
   * Field is used via urlPattern() * method in {@link CommandFactory} class to distinguish
   * request's URL and to delegate request to * current Command.
   */
  private static final Pattern resourcePattern = Pattern.compile("/logout");
  /** The URL of page, client is redirected to after request processing */
  private static final String URL_TO_REDIRECT = "/";

  private PrincipalService principalService;

  public LogoutCommand(PrincipalService principalService) {
    this.principalService = principalService;
  }

  @Override
  public Pattern urlPattern() {
    return resourcePattern;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws ClientCommandException, CommandException, PageNotFoundException {
    logger.debug("In LogoutCommand");
    switch (request.getMethod()) {
      case "POST":
        return postLogout(principal);
      case "GET":
        throw new PageNotFoundException();
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }
  /**
   * This method is used to process POST method of request which is being processing by current
   * Command. It provides logout command for current {@link Principal} specified in argument
   *
   * @param principal current Principal.
   * @return {@link CommandResult} object filled by URL_TO_REDIRECT.
   * @throws CommandException when server trouble occurs.
   */
  private CommandResult postLogout(Principal principal) throws CommandException {
    try {
      principalService.logout(principal);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    return new CommandResult(URL_TO_REDIRECT);
  }
}
