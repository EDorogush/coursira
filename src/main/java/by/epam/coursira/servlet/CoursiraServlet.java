package by.epam.coursira.servlet;

import by.epam.coursira.command.Command;
import by.epam.coursira.command.CommandFactory;
import by.epam.coursira.command.CommandResult;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.PoolConnectionException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.ErrorModel;
import by.epam.coursira.pool.ConnectionPoolImpl;
import by.epam.coursira.service.PrincipalService;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;


@WebServlet(
  urlPatterns = {"/"},
  loadOnStartup = 1)
@MultipartConfig
public class CoursiraServlet extends HttpServlet {
  private static final Logger logger = LogManager.getLogger();
  private static List<Command> commandList = new ArrayList<>();
  private static PrincipalService principalService;
  private static ConnectionPoolImpl connectionPool;
  private ScheduledFuture<?> scheduledFuture;
  private static ScheduledExecutorService cleanerThread =
    Executors.newSingleThreadScheduledExecutor();

  public CoursiraServlet() {
    logger.info("Servlet constructed");
  }

  @Override
  public void init() throws ServletException {
    //https://stackoverflow.com/questions/496711/adding-a-pre-constructed-bean-to-a-spring-application-context
    //create parent BeanFactory
    DefaultListableBeanFactory parentBeanFactory = new DefaultListableBeanFactory();
    //register your pre-fabricated object in it
    parentBeanFactory.registerSingleton("servletContext", getServletContext());
    //wrap BeanFactory inside ApplicationContext
    GenericApplicationContext parentContext =
      new GenericApplicationContext(parentBeanFactory);
    parentContext.refresh(); //as suggested "itzgeoff", to overcome a warning about events
    // create your "child" ApplicationContext that contains the beans from "beans.xml"
    //note that we are passing previously made parent ApplicationContext as parent
    ApplicationContext springContext = new ClassPathXmlApplicationContext(
      new String[]{"/spring.xml"}, parentContext);

    ServletContext context = springContext.getBean(ServletContext.class);

    // initParams
    final int cleanerInitialDelay =
      Integer.parseInt(context.getInitParameter("cleanerInitialDelay"));
    final int cleanerProcedureDelay = Integer.parseInt(context.getInitParameter("cleanerDelay"));
    //connectionPool = springContext.getBean(ConnectionPoolImpl.class);
//    try {
//      Class.forName("org.postgresql.Driver");
//      //connectionPool = new ConnectionPoolImpl(dbPoolSize, url);
//      connectionPool = springContext.getBean(ConnectionPoolImpl.class);
//      logger.info("pool constructed");
//    } catch (PoolConnectionException | ClassNotFoundException e) {
//      throw new ServletException(e);
//    }
//
    principalService = springContext.getBean(PrincipalService.class);
    commandList = springContext.getBean("commandList", List.class);
    logger.info("commandList constructed ");

    scheduledFuture =
      cleanerThread.scheduleWithFixedDelay(
        () -> {
          try {
            principalService.cleanFromExpiredSessions();
            principalService.cleanFromExpiredRegistrationCode();
          } catch (ServiceException e) {
            logger.error("cleaner procedure init fails: {}", e);
          }
        },
        cleanerInitialDelay,
        cleanerProcedureDelay,
        TimeUnit.HOURS);
    logger.info("cleaner thread constructed");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      processRequest(request, response);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      processRequest(request, response);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    logger.debug("request {}", () -> httpServletRequestToString(request));
    String sessionId = request.getSession().getId();
    String modelName = "model";
    try {
      Principal principal = principalService.verifyPrincipleBySessionId(sessionId);
      logger.debug("principal verified: {}", principal);
      try {

        Command command = new CommandFactory(commandList).getCommand(request.getServletPath());
        CommandResult result = command.execute(principal, request);
        if (result.isForward()) {
          request.setAttribute(modelName, result.getJspModel());
          getServletContext().getRequestDispatcher(result.getJsp()).forward(request, response);
        } else {
          response.sendRedirect(request.getContextPath() + result.getPageToRedirect());
        }
      } catch (PageNotFoundException e) {
        logger.debug(e);
        ErrorModel model = new ErrorModel();
        model.setPrincipal(principal);
        request.setAttribute(modelName, model);
        response.sendError(404);
      } catch (ClientCommandException e) {
        ErrorModel model = new ErrorModel();
        logger.warn(e);
        model.setPrincipal(principal);
        model.setErrorMessage(e.getMessage());
        request.setAttribute(modelName, model);
        response.sendError(400);
      }
    } catch (Exception e) {
      logger.error(e);
      response.sendError(500);
    }
  }

  @Override
  public void destroy() {
    scheduledFuture.cancel(true);
    cleanerThread.shutdownNow();
    logger.info("cleanerThread destroyed");
    try {
      connectionPool.close();
      logger.info("Servlet destroyed");
    } catch (PoolConnectionException e) {
      logger.error("Servlet can't be destroyed correctly  {}", e);
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
