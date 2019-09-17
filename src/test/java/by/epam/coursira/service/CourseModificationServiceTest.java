package by.epam.coursira.service;

import by.epam.coursira.dao.CourseDao;
import by.epam.coursira.dao.UserDao;
import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Language;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.entity.Session;
import by.epam.coursira.entity.User;
import by.epam.coursira.exception.AccessDeniedException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.PoolConnectionException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.mail.MailSender;
import by.epam.coursira.pool.ConnectionPool;
import by.epam.coursira.pool.ConnectionPoolImpl;

import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CourseModificationServiceTest {
  private PostgreSQLContainer postgresContainer;
    private CourseModificationService service;
  private CourseDao courseDao;

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

  private Principal notCourseOwner = new Principal(
    new Session.Builder()
      .setExpDate(Instant.now())
      .setId("abc")
      .setUserId(3)
      .setLanguage(Language.EN)
      .setZoneOffSet(ZoneOffset.UTC)
      .build(),
    new User.Builder()
      .setId(3)
      .setEmail("email")
      .setFirstName("firstName")
      .setLastName("lastName")
      .setPassword("password")
      .setRole(Role.STUDENT)
      .build()
  );


  @Mock
  MailSender mockMailSender;

  @BeforeEach
  public void setUp() throws PoolConnectionException {
    MockitoAnnotations.initMocks(this);
    int dbPoolSize = 10;
    postgresContainer =
      new PostgreSQLContainer<>()
        .withClasspathResourceMapping("dbscripts", "/dbscripts", BindMode.READ_WRITE)
        .withDatabaseName("coursiradb")
        .withUsername("coursirauser")
        .withPassword("password")
        .withInitScript("dbscripts/schema.sql");
    postgresContainer.start();
    String jdbcUrl =
      postgresContainer.getJdbcUrl()
        + "&user="
        + postgresContainer.getUsername()
        + "&password="
        + postgresContainer.getPassword();

    ConnectionPool connectionPool = new ConnectionPoolImpl(dbPoolSize, jdbcUrl);
    courseDao = new CourseDao(connectionPool);
    UserDao userDao = new UserDao(connectionPool);

    service = new CourseModificationService(courseDao, userDao, mockMailSender);
  }

  @AfterEach
  public void tearDown() {
    postgresContainer.stop();
  }

  @Test
  public void testUpdateCourseSucceed()
    throws ClientServiceException, ServiceException, AccessDeniedException, DaoException {
    String afterUpdateTitle = "new title";
    String afterUpdateDescription = "new description";
    int afterUpdateCapacity = 2;

    service.updateCourse(
      courseOwner, courseId, afterUpdateTitle, afterUpdateDescription, afterUpdateCapacity);
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
  public void testUpdateCourseFailsWithAccessDeniedException() {
    String afterUpdateTitle = "new title";
    String afterUpdateDescription = "new description";
    int afterUpdateCapacity = 2;

    assertThrows(AccessDeniedException.class, () -> service.updateCourse(
      notCourseOwner, courseId, afterUpdateTitle, afterUpdateDescription, afterUpdateCapacity));
  }

  @Test
  public void testActivateCourseThrowsAccessDeniedException() {
    assertThrows(AccessDeniedException.class, () -> service.activateCourse(notCourseOwner, courseId));
  }

  @Test
  public void testActivateCourseThrowsExceptionWhenLecturerHasNoLectures() throws ClientServiceException, ServiceException, AccessDeniedException {
    assertThrows(ClientServiceException.class, () -> service.activateCourse(courseOwner, courseId));
  }

}

