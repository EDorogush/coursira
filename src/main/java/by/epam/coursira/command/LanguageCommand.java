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
      throws ClientCommandException, PageNotFoundException,CommandException {
    logger.debug("In LanguageCommand");
    switch (request.getMethod()) {
      case "POST":
        String referer =
            request.getHeader("referer")
                .split(request.getContextPath())[1]; // ServletPath part of referer link
        logger.debug("Language referer {}", referer);

        return postLanguage(principal, referer, request.getParameterMap());
      case "GET":
        throw new PageNotFoundException();
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

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
