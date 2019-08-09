package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.AccessDeniedException;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.service.CourseService;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class is intended to process client's requests to resource corresponding to
 * "/courses/([^/?]+)/subscriptions(\\?.*)?" pattern.
 */
public class CourseIdSubscriptionCommand implements Command {
  public static final Logger logger = LogManager.getLogger();
  private static final Pattern resourcePattern =
      Pattern.compile("/courses/([^/?]+)/subscriptions(\\?.*)?");
  private static final String REQUEST_PARAMETER_SUBSCRIBE = "subscribe";
  private final CourseService courseService;

  public CourseIdSubscriptionCommand(CourseService courseService) {
    this.courseService = courseService;
  }

  @Override
  public Pattern urlPattern() {
    return resourcePattern;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws PageNotFoundException, ClientCommandException, CommandException {
    logger.debug("In CourseIdSubscriptionCommand");
    switch (request.getMethod()) {
      case "GET":
        throw new PageNotFoundException();
      case "POST":
        String referer = request.getServletPath();
        logger.debug("referer is {}", referer);
        Matcher matcher = resourcePattern.matcher(request.getServletPath());
        if (!matcher.matches()) {
          throw new PageNotFoundException();
        }
        final int courseId;
        try {
          courseId = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
          throw new ClientCommandException("Can't parse courseId from request");
        }
        logger.debug("course id parsed successfully {}", courseId);
        return postSubscription(principal, referer, courseId, request.getParameterMap());
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  private CommandResult postSubscription(
      Principal principal, String referer, int courseId, Map<String, String[]> queryParams)
      throws CommandException, ClientCommandException {
    boolean subscribe =
        CommandUtils.parseOptionalBoolean(queryParams, REQUEST_PARAMETER_SUBSCRIBE).orElse(false);
    logger.debug("subscribe = {}", subscribe);
    try {
      if (subscribe) {
        courseService.joinToCourse(principal, courseId);
      } else {
        courseService.leaveCourse(principal, courseId);
      }
    } catch (ClientServiceException | AccessDeniedException e) {
      throw new ClientCommandException(e.getMessage());
    } catch (ServiceException e) {
      throw new CommandException(e);
    }
    return new CommandResult(referer);
  }
}
