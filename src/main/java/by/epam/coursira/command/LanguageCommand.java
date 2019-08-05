package by.epam.coursira.command;

import by.epam.coursira.entity.Language;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.service.PrincipalService;
import by.epam.coursira.servlet.CoursiraUrlPatterns;
import java.net.http.HttpHeaders;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LanguageCommand extends CommandAbstract {
  private static final Logger logger = LogManager.getLogger();
  private PrincipalService principalService;

  public LanguageCommand(PrincipalService principalService) {
    super(CoursiraUrlPatterns.LANGUAGE);
    this.principalService = principalService;
  }
  // take language from query, update session, redirect to redirectPage

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws CommandException, ClientCommandException, PageNotFoundException {
    logger.debug("In LanguageCommand");
    switch (request.getMethod()) {
      case "POST":
        String referer =
            request.getHeader("referer")
                .split(request.getContextPath())[1]; // ServletPath part of referer link
        logger.debug("Language referer {}", referer);

        return postLanguage(principal, referer, request.getParameterMap());
      case "GET":
        return getLanguage();
      default:
        throw new CommandException("Unknown method invoked.");
    }
  }

  private CommandResult postLanguage(
      Principal principal, String referer, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    Language current =
        CommandUtils.parseOptionalLanguage(queryParams, "language")
            .orElseThrow(() -> new CommandException("wrong <language> parameter value"));
    try {
      principalService.changeLanguage(principal, current);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    return new CommandResult(referer);
  }

  private CommandResult getLanguage() throws PageNotFoundException {
    throw new PageNotFoundException();
  }
}
