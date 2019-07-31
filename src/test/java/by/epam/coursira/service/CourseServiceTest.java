package by.epam.coursira.service;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import by.epam.coursira.dao.CourseDao;
import by.epam.coursira.dao.StudentDao;
import by.epam.coursira.dao.UserDao;
import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Language;
import by.epam.coursira.entity.Lecture;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.entity.Session;
import by.epam.coursira.entity.User;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.ServiceException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CourseServiceTest {

  private User userStudent =
      new User.Builder().setId(1).setEmail("email").setRole(Role.STUDENT).build();

  private User userLecturer =
      new User.Builder().setId(1).setEmail("email").setRole(Role.LECTURER).build();

  private User userAnonymous =
      new User.Builder().setId(1).setEmail("email").setRole(Role.ANONYMOUS).build();

  private Session session =
      new Session.Builder()
          .setExpDate(Instant.now())
          .setId("abc")
          .setUserId(1)
          .setLanguage(Language.EN)
          .setZoneOffSet(ZoneOffset.UTC)
          .build();

  @Mock CourseDao mockCourseDao;

  @Mock StudentDao mockStudentDao;

  @Mock UserDao mockUserDao;

  private CourseService courseService;
  private Principal principal;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    courseService = new CourseService(mockCourseDao, mockStudentDao, mockUserDao);
  }

  @AfterMethod
  public void tearDown() {}

  @Test
  public void testViewCoursesPersonalSucceed()
      throws ServiceException, ClientServiceException, DaoException {
    // given
    principal = new Principal(session, userStudent);
    List<Course> expected = new ArrayList<>();
    // when
    int limit = 10;
    int offset = 10;
    when(mockStudentDao.selectCoursesByStudentId(anyInt(), anyInt(), anyInt()))
        .thenReturn(expected);
    List<Course> actual = courseService.viewCoursesPersonal(principal, limit, offset);
    // then
    assertEquals(actual, expected);
  }

  @Test
  public void testViewCoursesPersonalWhenLimitIncorrectThrowsClientServiceException() {
    // given
    principal = new Principal(session, userStudent);
    int offset = 10;
    // when
    int limit = -10;
    // then
    assertThrows(
        ClientServiceException.class,
        () -> courseService.viewCoursesPersonal(principal, limit, offset));
  }

  @Test
  public void testViewCoursesPersonalWhenWrongRoleThrowsServiceException() {
    // given
    principal = new Principal(session, userAnonymous);
    int offset = 10;
    int limit = 10;
    assertThrows(
        ServiceException.class, () -> courseService.viewCoursesPersonal(principal, limit, offset));
  }

  @Test
  public void testIsScheduleCrossWhenDontHaveCrossingReturnFalse()
      throws DaoException, ClientServiceException, ServiceException {
    Principal principal = new Principal(session, userStudent);
    List<Lecture> studentSchedule = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      Lecture l1 =
          new Lecture.Builder().withTimeStart(Instant.now()).withTimeEnd(Instant.now()).build();
      studentSchedule.add(l1);
    }
    List<Lecture> courseSchedule = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      Lecture l1 =
          new Lecture.Builder().withTimeStart(Instant.now()).withTimeEnd(Instant.now()).build();
      studentSchedule.add(l1);
    }
    Course course = new Course.Builder().withLectures(courseSchedule).build();
    // when
    when(mockStudentDao.selectScheduleByStudentId(anyInt(), anyInt(), anyInt()))
        .thenReturn(studentSchedule);
    when(mockCourseDao.selectCourseById(anyInt(), anyInt(), anyInt()))
        .thenReturn(Optional.of(course));
    when(mockCourseDao.isExistsCourse(anyInt())).thenReturn(true);
    // then
    assertFalse(courseService.isScheduleCross(principal, 1));
  }

  @Test
  public void testIsScheduleCrossWhenHaveCrossingReturnTrue()
      throws DaoException, ClientServiceException, ServiceException {
    Principal principal = new Principal(session, userStudent);
    List<Lecture> studentSchedule = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      Lecture l1 =
          new Lecture.Builder().withTimeStart(Instant.now()).withTimeEnd(Instant.now()).build();
      studentSchedule.add(l1);
    }
    List<Lecture> courseSchedule = new ArrayList<>();
    Lecture lecture =
        new Lecture.Builder()
            .withTimeStart(studentSchedule.get(1).getEndTime())
            .withTimeEnd(studentSchedule.get(2).getStartTime())
            .build();
    courseSchedule.add(lecture);
    Course course = new Course.Builder().withLectures(courseSchedule).build();
    // when
    when(mockStudentDao.selectScheduleByStudentId(anyInt(), anyInt(), anyInt()))
        .thenReturn(studentSchedule);
    when(mockCourseDao.selectCourseById(anyInt(), anyInt(), anyInt()))
        .thenReturn(Optional.of(course));
    when(mockCourseDao.isExistsCourse(anyInt())).thenReturn(true);
    // then
    assertTrue(courseService.isScheduleCross(principal, 1));
  }

  @Test
  public void testIsScheduleCrossWhenCourseDoesntExistThrowClientServiceException()
    throws DaoException, ClientServiceException, ServiceException {
    Principal principal = new Principal(session, userStudent);
    List<Lecture> studentSchedule = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      Lecture l1 =
        new Lecture.Builder().withTimeStart(Instant.now()).withTimeEnd(Instant.now()).build();
      studentSchedule.add(l1);
    }
    // when
    when(mockCourseDao.isExistsCourse(anyInt())).thenReturn(false);
    // then
    assertThrows(ClientServiceException.class, ()-> courseService.isScheduleCross(principal, 1));
  }


  @Test
  public void testJoinToCourse() {}

  @Test
  public void testLeaveCourse() {}

  @Test
  public void testIsInUserListCourse() {}
}
