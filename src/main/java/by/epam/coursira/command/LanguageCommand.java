package by.epam.coursira.command;

import by.epam.coursira.entity.Language;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.service.PrincipalService;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Class is intended to process client's requests to resource corresponding to "/language" pattern.
 */
public class LanguageCommand implements Command {
  private static final Logger logger = LogManager.getLogger();
  /**
   * Field is used via urlPattern() method in {@link CommandFactory} class to distinguish request's
   * URL and to delegate request to current Command. This is supporting page and only POST method of
   * request is possible. Request with GET method will throw {@link PageNotFoundException}
   */
  private static final Pattern resourcePattern = Pattern.compile("/language");

  private PrincipalService principalService;

  public LanguageCommand(PrincipalService principalService) {
    this.principalService = principalService;
  }

  @Override
  public Pattern urlPattern() {
    return resourcePattern;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws ClientCommandException, PageNotFoundException, CommandException {
    logger.debug("In LanguageCommand");
    switch (request.getMethod()) {
      case "POST":
        String referer = request.getServletPath();
        logger.debug("referer is {}", referer);
        return postLanguage(principal, referer, request.getParameterMap());
      case "GET":
        throw new PageNotFoundException();
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  /**
   * This method is used to process POST method of request which is being processing by current
   * Command. It parses from request parameters "language" value to {@link Language}, and change
   * current language of the session to parsed one. After updates client is redirected to previous
   * page.
   *
   * @param principal current Principal.
   * @return {@link CommandResult} object filled by referer URL.
   * @throws ClientCommandException when attempt to parse "language" parameter fails.
   * @throws CommandException when server trouble occurs.
   */
  private CommandResult postLanguage(
      Principal principal, String referer, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    Language current =
        CommandUtils.parseOptionalLanguage(queryParams, "language")
            .orElseThrow(() -> new ClientCommandException("wrong <language> parameter value"));
    try {
      principalService.changeLanguage(principal, current);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    return new CommandResult(referer);
  }
}
