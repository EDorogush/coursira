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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebServlet(
    urlPatterns = {"/"},
    loadOnStartup = 1)
@MultipartConfig
public class CoursiraServlet extends HttpServlet {
  private static final Logger logger = LogManager.getLogger();
  private List<CommandAbstract> commandList = new ArrayList<>();
  private PrincipalService principalService;
  private ConnectionPoolImpl connectionPool;
  private ScheduledExecutorService cleanerThread = Executors.newSingleThreadScheduledExecutor();

  public CoursiraServlet() {
    logger.info("Servlet constructed");
  }

  @Override
  public void init() throws ServletException {
    ServletContext context = getServletContext();
    // initParams
    final String url = context.getInitParameter("jdbcDriver");
    final int dbPoolSize = Integer.parseInt(context.getInitParameter("dbPoolSize"));
    final int cleanerInitialDelay =
        Integer.parseInt(context.getInitParameter("cleanerInitialDelay"));

    final Duration sessionLoginDuration =
        Duration.ofHours(Integer.parseInt(context.getInitParameter("sessionLoginDurationHours")));
    final Duration sessionAnonymousDuration =
        Duration.ofHours(
            Integer.parseInt(context.getInitParameter("sessionAnonymousDurationHours")));
    final int cleanerProcedureDelay = Integer.parseInt(context.getInitParameter("cleanerDelay"));
    final String gmailPassword = context.getInitParameter("gmailPassword");
    final String gmailAddress = context.getInitParameter("gmailAddress");
    final Properties propSmtp = new Properties();
    propSmtp.put("mail.smtp.host", context.getInitParameter("mail.smtp.host"));
    propSmtp.put("mail.smtp.port", context.getInitParameter("mail.smtp.port"));
    propSmtp.put("mail.smtp.auth", context.getInitParameter("mail.smtp.auth"));
    propSmtp.put(
        "mail.smtp.starttls.enable", context.getInitParameter("mail.smtp.starttls.enable"));
    try {
      connectionPool = new ConnectionPoolImpl(dbPoolSize, url);
      logger.info("pool constructed");
    } catch (PoolConnectionException e) {
      throw new ServletException(e);
    }
    CourseDao courseDao = new CourseDao(connectionPool);
    StudentDao studentDao = new StudentDao(connectionPool);
    UserDao userDao = new UserDao(connectionPool);
    MailSender mailSender = new MailSender(gmailAddress, gmailPassword, propSmtp);
    this.principalService =
        new PrincipalService(userDao, sessionLoginDuration, sessionAnonymousDuration, new BCryptHashMethod(), mailSender);
    CourseService courseService = new CourseService(courseDao, studentDao, userDao);
    UserService userService = new UserService(userDao);
    CourseModificationService courseModificationService =
        new CourseModificationService(courseDao, userDao, mailSender);

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
    logger.info("cleaner thread constructed");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    processRequest(request, response);
  }

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    logger.info("request {}", httpServletRequestToString(request));
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
          response.sendRedirect(request.getContextPath() + result.getPageToRedirect());
        }
      } catch (PageNotFoundException e) {
        logger.error(e);
        ErrorModel model = new ErrorModel();
        model.setPrincipal(principal);
        request.setAttribute("model", model);
        response.sendError(404);
      } catch (ClientCommandException e) {
        ErrorModel model = new ErrorModel();
        logger.error(e);
        model.setPrincipal(principal);
        model.setErrorMessage(e.getMessage());
        request.setAttribute("model", model);
        response.sendError(400);
      }
    } catch (Exception e) {
      logger.error(e);
      response.sendError(500);
    }
  }

  @Override
  public void destroy() {
    cleanerThread.shutdownNow();
    logger.info("cleanerThread destroyed");
    try {
      connectionPool.close();
      logger.info("Servlet destroyed");
    } catch (PoolConnectionException e) {
      logger.error(e);
      logger.info("Servlet doesn't destroyed");
    }
  }

  private String httpServletRequestToString(HttpServletRequest request) {
    String requestInfo =
        "Request Method = ["
            + request.getMethod()
            + "], Request URL Path = ["
            + request.getRequestURL()
            + "], ";
    String headers =
        Collections.list(request.getHeaderNames()).stream()
            .map(
                headerName -> headerName + " : " + Collections.list(request.getHeaders(headerName)))
            .collect(Collectors.joining(", "));

    if (headers.isEmpty()) {
      requestInfo += "Request headers: NONE,";
    } else {
      requestInfo += "Request headers: [" + headers + "],";
    }
    String parameters =
        Collections.list(request.getParameterNames()).stream()
            .map(p -> p + " : " + Arrays.asList(request.getParameterValues(p)))
            .collect(Collectors.joining(", "));

    if (parameters.isEmpty()) {
      requestInfo += "Request parameters: NONE.";
    } else {
      requestInfo += "Request parameters: [" + parameters + "].";
    }
    return requestInfo;
  }
}
