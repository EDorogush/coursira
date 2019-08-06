package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.IndexModel;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class is intended to process client's requests to resource corresponding to "/" pattern. */
public class IndexCommand implements Command {
  private static final Logger logger = LogManager.getLogger();
  private static final Pattern resourcePattern = Pattern.compile("/");

  private final CourseService courseService;

  public IndexCommand(CourseService courseService) {
    this.courseService = courseService;
  }

  @Override
  public Pattern urlPattern() {
    return resourcePattern;
  }

  @Override
  public CommandResult execute(Principal principal, HttpServletRequest request)
      throws PageNotFoundException, ClientCommandException, CommandException {
    logger.debug("In IndexCommand");
    switch (request.getMethod()) {
      case "GET":
        return getIndex(principal);
      case "POST":
        throw new PageNotFoundException();
      default:
        throw new ClientCommandException("Unknown method invoked.");
    }
  }

  private CommandResult getIndex(Principal principal) throws CommandException {
    IndexModel indexModel = new IndexModel();
    indexModel.setPrincipal(principal);
    try {
      Map<String, Integer> pairs = courseService.countStatistics();
      indexModel.setCoursesAmount(pairs.get("courses"));
      indexModel.setLecturerAmount(pairs.get("lecturers"));
      indexModel.setStudentsAmount(pairs.get("students"));
    } catch (ServiceException e) {
      logger.info("Service exception was thrown", e);
      throw new CommandException(e);
    }

    return new CommandResult(CoursiraJspPath.INDEX, indexModel);
  }
}
