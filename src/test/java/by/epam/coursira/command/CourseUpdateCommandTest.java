package by.epam.coursira.command;

import by.epam.coursira.dao.CourseDao;
import by.epam.coursira.dao.StudentDao;
import by.epam.coursira.dao.UserDao;
import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Language;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.entity.Session;
import by.epam.coursira.entity.User;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.PoolConnectionException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.mail.MailSender;
import by.epam.coursira.model.CourseUpdateModel;
import by.epam.coursira.pool.ConnectionPool;
import by.epam.coursira.pool.ConnectionPoolImpl;
import by.epam.coursira.service.CourseModificationService;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class CourseUpdateCommandTest {

  private PostgreSQLContainer postgresContainer;
  private CourseUpdateCommand command;
  private CourseDao courseDao;
  @Mock
  HttpServletRequest mockRequest;

  private int courseId = 5;
  private Principal courseOwner = new Principal(
    new Session.Builder()
      .setExpDate(Instant.now())
      .setId("abc")
      .setUserId(9)
      .setLanguage(Language.EN)
      .setZoneOffSet(ZoneOffset.UTC)
      .build(),
    new User.Builder()
      .setId(9)
      .setEmail("email")
      .setFirstName("firstName")
      .setLastName("lastName")
      .setPassword("password")
      .setRole(Role.LECTURER)
      .build()
  );

  @Mock
  MailSender mockMailSender;

  @BeforeEach
  public void setUp() throws PoolConnectionException {
    MockitoAnnotations.initMocks(this);
    final int dbPoolSize = 10;
    when(mockRequest.getServletPath()).thenReturn("/courses/5/update");
    postgresContainer =
      new PostgreSQLContainer<>()
        .withClasspathResourceMapping("testdbscripts", "/testdbscripts", BindMode.READ_WRITE)
        .withDatabaseName("coursiradb")
        .withUsername("coursirauser")
        .withPassword("password")
        .withInitScript("testdbscripts/schema.sql");
    postgresContainer.start();
    String jdbcUrl =
      postgresContainer.getJdbcUrl()
        + "&user="
        + postgresContainer.getUsername()
        + "&password="
        + postgresContainer.getPassword();

    ConnectionPool connectionPool = new ConnectionPoolImpl(dbPoolSize, jdbcUrl);
    courseDao = new CourseDao(connectionPool);
    StudentDao studentDao = new StudentDao(connectionPool);
    UserDao userDao = new UserDao(connectionPool);

    CourseModificationService courseModificationService = new CourseModificationService(courseDao, userDao, mockMailSender);
    CourseService courseService = new CourseService(courseDao, studentDao, userDao);
    UserService userService = new UserService(userDao);
    command = new CourseUpdateCommand(courseModificationService, courseService, userService);

  }

  @AfterEach
  public void tearDown() {
    postgresContainer.stop();
  }

  @Test
  void testExecuteUpdateCourseSucceed() throws CommandException, ClientCommandException, DaoException, ServiceException {
    String afterUpdateTitle = "new title";
    String afterUpdateDescription = "new description";
    int afterUpdateCapacity = 2;

    Map<String, String[]> queryParams = Map.of("updateCourseData", new String[]{"true"},
      "title", new String[]{afterUpdateTitle},
      "capacity", new String[]{String.valueOf(afterUpdateCapacity)},
      "description", new String[]{afterUpdateDescription});
    when(mockRequest.getMethod()).thenReturn("POST");
    when(mockRequest.getServletPath()).thenReturn("/courses/5/update");
    when(mockRequest.getParameterMap()).thenReturn(queryParams);
    command.execute(courseOwner, mockRequest);
    Course afterUpdateCourse =
      courseDao
        .selectCourseById(courseId, 10, 0)
        .orElseThrow(() -> new ServiceException("No Such course in db!"));
    assertTrue(
      afterUpdateCourse.getCapacity() == afterUpdateCapacity
        && afterUpdateCourse.getTitle().equals(afterUpdateTitle)
        && afterUpdateCourse.getDescription().equals(afterUpdateDescription));
  }

  @Test
  void testExecuteInviteLecturerAttemptToAddYourselfFails() throws CommandException, ClientCommandException {
    int lecturerId = 9;
    String expectedErrorMessage = "You can't add yourself";

    Map<String, String[]> queryParams = Map.of("inviteLecturer", new String[]{"true"},
      "lecturerId", new String[]{String.valueOf(lecturerId)});

    when(mockRequest.getMethod()).thenReturn("POST");
    when(mockRequest.getParameterMap()).thenReturn(queryParams);

    CommandResult commandResult = command.execute(courseOwner, mockRequest);
    String actualErrorMessage = ((CourseUpdateModel) commandResult.getJspModel()).getErrorCourseDataMessage();
    assertEquals(expectedErrorMessage, actualErrorMessage);
  }

  @Test
  void testExecuteInviteLecturerSucceed() throws CommandException, ClientCommandException, DaoException, ClientServiceException {
    //given
    int expectedLecturerNumber = 2;
    int lecturerId = 7;
    Map<String, String[]> queryParams = Map.of("inviteLecturer", new String[]{"true"},
      "lecturerId", new String[]{String.valueOf(lecturerId)});
    //when
    when(mockRequest.getMethod()).thenReturn("POST");
    when(mockRequest.getParameterMap()).thenReturn(queryParams);
    command.execute(courseOwner, mockRequest);
    //then
    Course afterInvitationCourse = courseDao.selectCourseById(courseId, 100, 0).orElseThrow(
      () ->
        new ClientServiceException("WRONG_COURSE_ID" + courseId));

    int actualLecturerNumber = afterInvitationCourse.getLecturers().size();
    assertEquals(expectedLecturerNumber, actualLecturerNumber);
  }

}

