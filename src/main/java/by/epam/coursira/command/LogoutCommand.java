package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.service.PrincipalService;
import by.epam.coursira.servlet.CoursiraUrlPatterns;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogoutCommand extends CommandAbstract {
  private static final Logger logger = LogManager.getLogger();
  private PrincipalService principalService;

  public LogoutCommand(PrincipalService principalService) {
    super(CoursiraUrlPatterns.LOGOUT);
    this.principalService = principalService;
  }
  // update session, redirect to redirectPage

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws CommandException, PageNotFoundException{
    logger.debug("In LogoutCommand");
    switch (request.getMethod()) {
      case "POST":
        return postLogout(principal);
      case "GET":
        return getLogout();
      default:
        throw new PageNotFoundException();
    }
  }

  private CommandResult postLogout(Principal principal) throws CommandException {
    try {
      principalService.logout(principal);
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    return new CommandResult(CoursiraUrlPatterns.INDEX);
  }

  private CommandResult getLogout() {
    return new CommandResult(CoursiraUrlPatterns.PAGE_NOT_FOUND);
  }
}
