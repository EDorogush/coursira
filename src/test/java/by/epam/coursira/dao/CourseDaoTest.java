package by.epam.coursira.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecturer;

import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.PoolConnectionException;
import by.epam.coursira.pool.ConnectionPoolImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;

public class CourseDaoTest {
  private final int dbPoolSize = 10;
  private PostgreSQLContainer postgresContainer;

  private ConnectionPoolImpl connectionPool;
  private CourseDao courseDao;

  @BeforeEach
  public void setUp() throws PoolConnectionException {
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

    connectionPool = new ConnectionPoolImpl(dbPoolSize, jdbcUrl);
    courseDao = new CourseDao(connectionPool);
  }

  @AfterEach
  public void tearDown() {
    postgresContainer.stop();
  }

  @Test
  public void testIsExistCourseReadyReturnTrue() throws DaoException {
    int readyCourseId = 1;
    assertTrue(courseDao.isExistCourseReady(readyCourseId));
  }

  @Test
  public void testIsExistCourseReadyReturnFalse() throws DaoException {
    int unreadyCourseId = 5;
    assertFalse(courseDao.isExistCourseReady(unreadyCourseId));
  }

  @Test
  public void testCountLecturerReadyCoursesReturnRightValue() throws DaoException {
    int lecturerId = 9;
    int expected = 1;
    int actual = courseDao.countLecturerReadyCourses(lecturerId);
    assertEquals(expected, actual);
  }

  @Test
  public void testCountCoursesAndLecturesAndLecturersAndStudentsReturnRightValue()
    throws DaoException {
    int expectedReadyCoursesAmount = 4;
    int expectedLecturesAmount = 24;
    int expectedLecturersAmount = 3;
    int expectedStudentsAmount = 6;

    Map<String, Integer> pairs = courseDao.countCoursesAndLecturesAndLecturersAndStudents();
    assertTrue(
      pairs.get("courses").equals(expectedReadyCoursesAmount)
        && pairs.get("lectures").equals(expectedLecturesAmount)
        && pairs.get("lecturers").equals(expectedLecturersAmount)
        && pairs.get("students").equals(expectedStudentsAmount));
  }

  @Test
  public void testInsertCourseWorksFine() throws DaoException {
    int studentCapacity = 10;
    int lecturerId = 7;
    Course course =
      new Course.Builder()
        .withTitle("test")
        .withDescription("description")
        .withCapacity(studentCapacity)
        .withStudentsAmount(0)
        .withLecturers(Arrays.asList(new Lecturer(lecturerId, "1", "1")))
        .withLectures(new ArrayList<>())
        .build();
    int expected = 6;
    int actual = courseDao.insertCourse(course);
    assertEquals(expected, actual);
  }

  @Test
  public void testInsertCourseRollbackWhenLecturerIdWrong() throws DaoException {
    int studentCapacity = 10;
    int lecturerId = 100;

    Course course =
      new Course.Builder()
        .withTitle("test")
        .withDescription("description")
        .withCapacity(studentCapacity)
        .withStudentsAmount(0)
        .withLecturers(Arrays.asList(new Lecturer(lecturerId, "1", "1")))
        .withLectures(new ArrayList<>())
        .build();

    assertThrows(DaoException.class, () -> courseDao.insertCourse(course));
  }
}
