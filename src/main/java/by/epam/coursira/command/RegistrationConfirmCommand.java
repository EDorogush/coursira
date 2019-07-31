package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.RegistrationCompletedModel;
import by.epam.coursira.service.PrincipalService;
import by.epam.coursira.servlet.CoursiraJspPath;
import by.epam.coursira.servlet.CoursiraUrlPatterns;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistrationConfirmCommand extends CommandAbstract {
  private static final Logger logger = LogManager.getLogger();
  private final PrincipalService principalService;

  public RegistrationConfirmCommand(PrincipalService principalService) {
    super(CoursiraUrlPatterns.REGISTRATION_CONFIRM);
    this.principalService = principalService;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws CommandException, PageNotFoundException {
    logger.debug("In RegistrationConfirmCommand");
    switch (request.getMethod()) {
      case "GET":
        return getRegistrationConfirm(principal, request.getParameterMap());
      case "POST":
        throw new PageNotFoundException();
      default:
        throw new CommandException("Unknown method invoked.");
    }
  }

  private CommandResult getRegistrationConfirm(
      Principal principal, Map<String, String[]> queryParams) throws CommandException {
    // todo: think and rewrite. why do I need email parameter?
    Optional<String> code = CommandUtils.parseOptionalString(queryParams, "code");
    boolean emailKey = CommandUtils.parseOptionalBoolean(queryParams, "email").orElse(false);
    if (code.isEmpty() && !emailKey) {
      logger.error("code parameter is absent");
      return new CommandResult(CoursiraUrlPatterns.PAGE_NOT_FOUND);
    }
    if (code.isPresent() && emailKey) {
      throw new CommandException("Only one of parameters expected to present");
    }
    ResourceBundle bundle =
        ResourceBundle.getBundle("errorMessages", principal.getSession().getLanguage().getLocale());
    RegistrationCompletedModel model = new RegistrationCompletedModel();
    model.setPrincipal(principal);

    if (emailKey) {
      model.setActivate(true);
      model.setTextMessage(bundle.getString("MEGGAGE_WAS_SENT_TO_EMAIL"));
    } else {
      try {
        principalService.activateRegistration(principal, code.get());
        logger.info("user's registration completed");
        model.setActivate(true);
        model.setTextMessage(bundle.getString("USER_ACTIVATION_CONFIRM"));
      } catch (ServiceException e) {
        throw new CommandException(e);
      } catch (ClientServiceException e) {
        logger.error(e);
        model.setActivate(false);
        model.setTextMessage(e.getMessage());
      }
    }

    return new CommandResult(CoursiraJspPath.REGISTRATION_CONFIRM, model);
  }
}
