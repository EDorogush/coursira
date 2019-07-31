package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.servlet.CoursiraUrlPatterns;
import java.util.Map;
import java.util.regex.Matcher;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CourseIdSubscriptionCommand extends CommandAbstract {
  public static final Logger logger = LogManager.getLogger();
  private final CourseService courseService;

  public CourseIdSubscriptionCommand(CourseService courseService) {
    super(CoursiraUrlPatterns.COURSE_SUBSCRIBE);
    this.courseService = courseService;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws CommandException, PageNotFoundException, ClientCommandException {
    logger.debug("In CourseIdSubscriptionCommand");
    switch (request.getMethod()) {
      case "GET":
        throw new PageNotFoundException();
      case "POST":
        String referer = request.getHeader("referer");
        logger.debug("referer is {}", referer);
        Matcher matcher = this.getPattern().matcher(request.getServletPath());
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
        throw new CommandException("Unknown method invoked.");
    }
  }

  private CommandResult postSubscription(
      Principal principal, String referer, int courseId, Map<String, String[]> queryParams)
      throws CommandException {
    boolean subscribe = CommandUtils.parseOptionalBoolean(queryParams, "subscribe").orElse(false);
    logger.debug("subscribe = {}", subscribe);
    try {
      if (subscribe) {
        courseService.joinToCourse(principal, courseId);
      } else {
        courseService.leaveCourse(principal, courseId);
      }
    } catch (ClientServiceException | ServiceException e) {
      throw new CommandException(e);
    }
    return new CommandResult(referer);
  }
}
