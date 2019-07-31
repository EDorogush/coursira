package by.epam.coursira.servlet;

import by.epam.coursira.command.Command;
import by.epam.coursira.command.CommandAbstract;
import by.epam.coursira.command.CommandFactory;
import by.epam.coursira.command.CommandResult;
import by.epam.coursira.command.CourseCommand;
import by.epam.coursira.command.CourseCreateCommand;
import by.epam.coursira.command.CourseIdCommand;
import by.epam.coursira.command.CourseIdSubscriptionCommand;
import by.epam.coursira.command.CourseUpdateCommand;
import by.epam.coursira.command.IndexCommand;
import by.epam.coursira.command.LanguageCommand;
import by.epam.coursira.command.LoginCommand;
import by.epam.coursira.command.LogoutCommand;
import by.epam.coursira.command.PersonalPageCommand;
import by.epam.coursira.command.PersonalUpdateCommand;
import by.epam.coursira.command.RegistrationCommand;
import by.epam.coursira.command.RegistrationConfirmCommand;
import by.epam.coursira.dao.CourseDao;
import by.epam.coursira.dao.StudentDao;
import by.epam.coursira.dao.UserDao;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.PoolConnectionException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.mail.MailSender;
import by.epam.coursira.model.ErrorModel;
import by.epam.coursira.pool.ConnectionPoolImpl;
import by.epam.coursira.security.BCryptHashMethod;
import by.epam.coursira.service.CourseModificationService;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.service.PrincipalService;
import by.epam.coursira.service.UserService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebServlet(
    urlPatterns = {"/"},
//    initParams = {
//      @WebInitParam(
//          name = "jdbcDriver",
//          value = "jdbc:postgresql://localhost:5432/postgres",
//          description = "database " + "driver's URL"),
//      @WebInitParam(
//          name = "dbPoolSize",
//          value = "5",
//          description = "maximum size of database connections pool"),
//      @WebInitParam(
//          name = "pagingLimit",
//          value = "3",
//          description = "maximum amount of records on one page"),
//      @WebInitParam(
//          name = "cleanerInitialDelay",
//          value = "1",
//          description = "the time (in hours) to delay first cleaner procedure execution"),
//      @WebInitParam(
//          name = "cleanerDelay",
//          value = "1",
//          description = "the delay (in hours) between cleaner procedure execution"),
//    },
    loadOnStartup = 1)
@MultipartConfig
public class CoursiraServlet extends HttpServlet {
  private static final Logger logger = LogManager.getLogger();
  private List<CommandAbstract> commandList = new ArrayList<>();
  private PrincipalService principalService;
  private ConnectionPoolImpl connectionPool;
  private ScheduledExecutorService cleanerThread = Executors.newSingleThreadScheduledExecutor();

  public CoursiraServlet() throws SQLException {
    logger.info("Servlet constructed");
  }

  @Override
  public void init() throws ServletException {
    logger.info(getServletContext().getInitParameter("gmailAddress"));
    //logger.info(config.getInitParameterNames().nextElement());
//    String url = config.getInitParameter("jdbcDriver");
//    int dbPoolSize = Integer.parseInt(config.getInitParameter("dbPoolSize"));
//    int cleanerInitialDelay = Integer.parseInt(config.getInitParameter("cleanerInitialDelay"));
//    int cleanerProcedureDelay = Integer.parseInt(config.getInitParameter("cleanerDelay"));
    String url = "jdbc:postgresql://localhost:5432/postgres";
    int dbPoolSize = 5;
    int cleanerInitialDelay = 1;
    int cleanerProcedureDelay = 1;
    String gmailPassword ="";
    String gmailAddress ="";
//    String gmailPassword = config.getInitParameter("gmailPassword");
//    String gmailAddress = config.getInitParameter("gmailAddress");
//    logger.info("gmail:{}", gmailAddress);
//    logger.info("pass:{}", gmailPassword);
    try {
      connectionPool = new ConnectionPoolImpl(dbPoolSize, url);
    } catch (PoolConnectionException e) {
      throw new ServletException(e);
    }
    CourseDao courseDao = new CourseDao(connectionPool);
    StudentDao studentDao = new StudentDao(connectionPool);
    UserDao userDao = new UserDao(connectionPool);
    MailSender mailSender = new MailSender(gmailAddress, gmailPassword);
    this.principalService = new PrincipalService(userDao, new BCryptHashMethod(), mailSender);
    CourseService courseService = new CourseService(courseDao, studentDao, userDao);
    UserService userService = new UserService(userDao);
    CourseModificationService courseModificationService =
        new CourseModificationService(courseDao, userDao, mailSender);
    logger.info("pool constructed");
    commandList.add(new CourseCommand(courseService, userService));
    commandList.add(new IndexCommand(courseService));
    commandList.add(new LanguageCommand(principalService));
    commandList.add(new LoginCommand(principalService));
    commandList.add(new LogoutCommand(principalService));
    commandList.add(new CourseIdCommand(courseService));
    commandList.add(new CourseIdSubscriptionCommand(courseService));
    commandList.add(new PersonalPageCommand(courseService, userService));
    commandList.add(new PersonalUpdateCommand(courseService, userService));
    commandList.add(new RegistrationCommand(principalService));
    commandList.add(new RegistrationConfirmCommand(principalService));
    commandList.add(new CourseCreateCommand(courseModificationService));
    commandList.add(new CourseUpdateCommand(courseModificationService, courseService, userService));
    logger.info("commandList constructed ");

    cleanerThread.scheduleWithFixedDelay(
        () -> {
          try {
            principalService.cleanFromExpiredSessions();
            principalService.cleanFromExpiredRegistrationCode();
          } catch (ServiceException e) {
            logger.error("cleaner procedure fails: {}", e.getMessage());
          }
        },
        cleanerInitialDelay,
        cleanerProcedureDelay,
        TimeUnit.HOURS);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    logServletRequestDetails(request);
    String sessionId = request.getSession().getId();
    try {
      Principal principal = principalService.verifyPrincipleBySessionId(sessionId);
      logger.info("principal verified: {}", principal.toString());
      try {
        Command command = new CommandFactory(commandList).getCommand(request.getServletPath());
        CommandResult result = command.execute(principal, request);
        if (result.isForward()) {
          request.setAttribute("model", result.getJspModel());
          getServletContext().getRequestDispatcher(result.getJsp()).forward(request, response);
        } else {
          response.sendRedirect(result.getPageToRedirect());
        }
      } catch (PageNotFoundException e) {
        logger.error(e);
        ErrorModel model = new ErrorModel();
        model.setPrincipal(principal);
        request.setAttribute("model", model);
        getServletContext()
            .getRequestDispatcher(CoursiraJspPath.PAGE_NOT_FOUND)
            .forward(request, response);
      } catch (ClientCommandException e) {
        ErrorModel model = new ErrorModel();
        logger.error(e);
        model.setPrincipal(principal);
        model.setErrorMessage(e.getMessage());
        request.setAttribute("model", model);
        getServletContext()
            .getRequestDispatcher(CoursiraJspPath.CLIENT_ERROR)
            .forward(request, response);
      }
    } catch (Exception e) {
      logger.error(e);
      getServletContext().getRequestDispatcher(CoursiraJspPath.ERROR).forward(request, response);
    }
  }

  @Override
  public void destroy() {
    cleanerThread.shutdownNow();
    logger.info("cleanerThread destroyed");
    try {
      connectionPool.close();
    } catch (PoolConnectionException e) {
      logger.error(e);
      // todo: //what next?
    }
    logger.info("Servlet destroyed");
  }

  private void logServletRequestDetails(HttpServletRequest request) {
    String method = request.getMethod();
    String contextPath = request.getContextPath();
    String pathInfo = request.getPathInfo();
    String requestURI = request.getRequestURI();
    String servletPath = request.getServletPath();
    String sessionId = request.getSession().getId();
    int maxInactiveInterval = request.getSession().getMaxInactiveInterval();

    logger.debug("request {}", httpServletRequestToString(request));
    logger.debug("method '{}'", method);
    logger.debug("contextPath '{}'", contextPath);
    logger.debug("pathInfo '{}'", pathInfo);
    logger.debug("requestURI '{}'", requestURI);
    logger.debug("servletPath '{}'", servletPath);
    logger.debug("sessionId {}", sessionId);
    logger.debug("maxInactiveInterval {}", maxInactiveInterval);
  }

  private String httpServletRequestToString(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();

    sb.append("Request Method = [" + request.getMethod() + "], ");
    sb.append("Request URL Path = [" + request.getRequestURL() + "], ");

    String headers =
        Collections.list(request.getHeaderNames()).stream()
            .map(
                headerName -> headerName + " : " + Collections.list(request.getHeaders(headerName)))
            .collect(Collectors.joining(", "));

    if (headers.isEmpty()) {
      sb.append("Request headers: NONE,");
    } else {
      sb.append("Request headers: [" + headers + "],");
    }

    String parameters =
        Collections.list(request.getParameterNames()).stream()
            .map(p -> p + " : " + Arrays.asList(request.getParameterValues(p)))
            .collect(Collectors.joining(", "));

    if (parameters.isEmpty()) {
      sb.append("Request parameters: NONE.");
    } else {
      sb.append("Request parameters: [" + parameters + "].");
    }

    return sb.toString();
  }
}