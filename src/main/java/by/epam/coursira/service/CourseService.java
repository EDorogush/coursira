package by.epam.coursira.service;

import by.epam.coursira.dao.CourseDao;
import by.epam.coursira.dao.StudentDao;
import by.epam.coursira.dao.UserDao;
import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecture;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.exception.AccessDeniedException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.ServiceException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CourseService {
  private static final Logger logger = LogManager.getLogger();
  private static final String RESOURCE_BUNDLE_ERROR_MESSAGE = "errorMessages";
  private static final String RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED = "ACCESS_DENIED";
  private static final String RESOURCE_BUNDLE_MESSAGE_WRONG_COURSE_ID = "WRONG_COURSE_ID_VALUE";

  private final CourseDao courseDao;
  private final StudentDao studentDao;
  private final UserDao userDao;

  public CourseService(CourseDao courseDao, StudentDao studentDao, UserDao userDao) {
    this.courseDao = courseDao;
    this.studentDao = studentDao;
    this.userDao = userDao;
  }

  /**
   * Method counts amount of courses, lectures, lecturers, students are registered in application.
   *
   * @return {@link Map}<{@link String}, {@link Integer}> the map with keys: "courses", "lectures",
   *     "lecturers","students".
   * @throws ServiceException if attempt to get information fails.
   */
  public Map<String, Integer> countStatistics() throws ServiceException {
    logger.debug("count statistics");
    final Map<String, Integer> stats;
    try {
      stats = courseDao.countCoursesAndLecturesAndLecturersAndStudents();
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
    return stats;
  }

  /**
   * For lecturers this method returns the number of ready courses on which current lecturer reads
   * lectures. For students this method returns the number of courses on which current student
   * subscribed.
   *
   * @param principal current {@link Principal}. must be not principal with {@link Role} Anonymous
   * @return {@code int} the number of courses.
   * @throws ServiceException when attempt to get data fails.
   * @throws AccessDeniedException when principal's role is Anonymous.
   */
  public int countCourses(Principal principal) throws ServiceException, AccessDeniedException {
    final int count;
    try {
      switch (principal.getUser().getRole()) {
        case STUDENT:
          count = studentDao.countStudentCourses(principal.getUser().getId());
          break;
        case LECTURER:
          count = courseDao.countLecturerReadyCourses(principal.getUser().getId());
          break;
        default:
          Locale.setDefault(principal.getSession().getLanguage().getLocale());
          ResourceBundle bundle =
              ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, Locale.getDefault());
          throw new AccessDeniedException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
      }
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
    logger.debug("courses Amount {}", count);
    return count;
  }

  /**
   * Method provides information about all courses in application.
   *
   * @param principal current {@link Principal}
   * @param limit the number of courses in returned list
   * @param offset the number of offset in full course list in database
   * @return {@link List}<{@link Course}>
   * @throws ServiceException if attempt to get information fails.
   * @throws ClientServiceException if values of offset or limit incorrect.
   */
  public List<Course> viewCourses(Principal principal, int limit, int offset)
      throws ServiceException, ClientServiceException {
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ValidationHelper.checkLimit(limit, currentLocale);
    ValidationHelper.checkOffSet(offset, currentLocale);
    final List<Course> courses;
    try {
      courses = courseDao.selectCoursesAllReady(limit, offset);
      logger.debug("selectCoursesAllReady {} records", courses.size());
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
    return courses;
  }

  /**
   * Method provides information about courses available for current principal.
   *
   * @param principal current {@link Principal}
   * @param limit the number of courses in returned list
   * @param offset the number of offset in full course list in database
   * @return {@link List}<{@link Course}>
   * @throws ServiceException if attempt to get information fails.
   * @throws ClientServiceException if values of offset or limit incorrect.
   * @throws AccessDeniedException when principal's role is Anonymous.
   */
  public List<Course> viewCoursesPersonal(Principal principal, int limit, int offset)
      throws ServiceException, ClientServiceException, AccessDeniedException {
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ValidationHelper.checkLimit(limit, currentLocale);
    ValidationHelper.checkOffSet(offset, currentLocale);
    final List<Course> courses;
    try {
      switch (principal.getUser().getRole()) {
        case STUDENT:
          courses = studentDao.selectCoursesByStudentId(principal.getUser().getId(), limit, offset);
          logger.debug("selectCoursesByStudentId {} records", courses.size());
          break;
        case LECTURER:
          courses =
              courseDao.selectAllCoursesByLecturerId(principal.getUser().getId(), limit, offset);
          logger.debug("selectAllCoursesByLecturerId  {} records", courses.size());
          break;
        default:
          ResourceBundle bundle =
              ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, Locale.getDefault());
          throw new AccessDeniedException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
      }
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
    return courses;
  }

  /**
   * Method provides data about all ready lecturer's courses specified by argument. Throws
   * ClientServiceException is such lecturer doesn't exists
   *
   * @param principal current {@link Principal}
   * @param limit the number of courses in returned list
   * @param offset the number of offset in full course list in database
   * @return {@link List}<{@link Course}>
   * @throws ServiceException if attempt to get information fails.
   * @throws ClientServiceException if values of offset or limit incorrect, or lecturer doesn't
   *     exist.
   */
  public List<Course> viewCoursesByLectureId(
      Principal principal, int lecturerId, int limit, int offset)
      throws ServiceException, ClientServiceException {
    Locale locale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, locale);
    final List<Course> courses;
    try {
      if (!userDao.isExistsLecturer(lecturerId)) {
        throw new ClientServiceException(bundle.getString("WRONG_LECTURER_ID"));
      }
      Locale currentLocale = principal.getSession().getLanguage().getLocale();
      ValidationHelper.checkLimit(limit, currentLocale);
      ValidationHelper.checkOffSet(offset, currentLocale);
      courses = courseDao.selectReadyCoursesByLecturerId(lecturerId, limit, offset);
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
    logger.debug("select {} records", courses.size());
    return courses;
  }

  /**
   * Method provides detailed data (with schedule) about course with courseId
   *
   * @param principal current {@link Principal}
   * @param courseId current id of course
   * @param limit the number of courses in returned list
   * @param offset the number of offset in full course list in database
   * @return {@link Course}
   * @throws ClientServiceException if course with current id doesn't exists or if values of offset
   *     or limit incorrect
   * @throws ServiceException if attempt to get data failed.
   */
  public Course viewCourseDetails(Principal principal, int courseId, int limit, int offset)
      throws ClientServiceException, ServiceException {
    final Course course;
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, currentLocale);
    try {
      ValidationHelper.checkLimit(limit, currentLocale);
      ValidationHelper.checkOffSet(offset, currentLocale);
      course =
          courseDao
              .selectCourseById(courseId, limit, offset)
              .orElseThrow(
                  () ->
                      new ClientServiceException(
                          String.format(bundle.getString("WRONG_COURSE_ID") + " %d", courseId)));
      logger.debug("course is ready = {}",course.isReady());
      if (!course.isReady() && principal.getUser().getRole() != Role.LECTURER) {
        throw new ClientServiceException(
            String.format(bundle.getString("WRONG_COURSE_ID") + " %d", courseId));
      }
      if (!course.isReady()
          && !courseDao.isExistsCourseInLecturerUpdateList(courseId, principal.getUser().getId())) {
        throw new ClientServiceException(
            String.format(bundle.getString("WRONG_COURSE_ID") + " %d", courseId));
      }
    } catch (DaoException e) {
      throw new ServiceException(e);
    }

    return course;
  }

  /**
   * Method provides information about user's schedule.
   *
   * @param principal current {@link Principal}
   * @param limit the number of courses in returned list
   * @param offset the number of offset in full course list in database
   * @return {@link List}<{@link Lecture}>
   * @throws ServiceException if attempt to get data fails principal's role == anonymous
   * @throws ClientServiceException if values of offset or limit incorrect
   * @throws AccessDeniedException when principal's role is Anonymous.
   */
  public List<Lecture> viewSchedule(Principal principal, int limit, int offset)
      throws ServiceException, ClientServiceException, AccessDeniedException {
    logger.debug("view schedule");
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ValidationHelper.checkLimit(limit, currentLocale);
    ValidationHelper.checkOffSet(offset, currentLocale);
    final List<Lecture> schedule;
    try {
      switch (principal.getUser().getRole()) {
        case LECTURER:
          schedule =
              courseDao.selectScheduleByLecturerId(principal.getUser().getId(), limit, offset);
          break;
        case STUDENT:
          schedule =
              studentDao.selectScheduleByStudentId(principal.getUser().getId(), limit, offset);
          break;
        default:
          ResourceBundle bundle =
              ResourceBundle.getBundle(
                  RESOURCE_BUNDLE_ERROR_MESSAGE, principal.getSession().getLanguage().getLocale());
          throw new AccessDeniedException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
      }
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
    return schedule;
  }

  public boolean isStudentScheduleCross(Principal principal, int courseId)
      throws ClientServiceException, ServiceException {
    if (principal.getUser().getRole() != Role.STUDENT) {
      // no exception thrown. just denied
      return false;
    }
    Locale locale = principal.getSession().getLanguage().getLocale();
    try {
      if (!courseDao.isExistCourseReady(courseId)) {
        ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, locale);
        throw new ClientServiceException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_WRONG_COURSE_ID));
      }
      List<Lecture> scheduleStudent =
          studentDao.selectScheduleByStudentId(principal.getUser().getId(), Integer.MAX_VALUE, 0);
      List<Lecture> scheduleCourse =
          courseDao
              .selectCourseById(courseId, Integer.MAX_VALUE, 0)
              .orElseThrow(() -> new ServiceException("No Such course in db!"))
              .getLectures();
      scheduleStudent.addAll(scheduleCourse);
      try {
        return ValidationHelper.checkScheduleHaveCrossing(scheduleStudent, locale);
      } catch (ClientServiceException e) {
        logger.debug(e); // found crossing
        return true;
      }
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  /**
   * Method carries out joining procedure to course. This method is available for principal with
   * Role.STUDENT only. Otherwise AccessDeniedException will be thrown.
   *
   * @param principal current principal
   * @param courseId current curse's Id
   * @throws ClientServiceException when courseId value is wrong.
   * @throws ServiceException when request to DAO fails.
   * @throws AccessDeniedException then principal role is not STUDENT.
   */
  public void joinToCourse(Principal principal, int courseId)
      throws ClientServiceException, ServiceException, AccessDeniedException {
    Locale locale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, locale);
    if (principal.getUser().getRole() != Role.STUDENT) {
      throw new AccessDeniedException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
    }
    try {
      if (!courseDao.isExistCourseReady(courseId)) {
        throw new ClientServiceException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_WRONG_COURSE_ID));
      }
      // check if schedule have crossing
      List<Lecture> scheduleStudent =
          studentDao.selectScheduleByStudentId(principal.getUser().getId(), Integer.MAX_VALUE, 0);
      List<Lecture> scheduleCourse =
          courseDao
              .selectCourseById(courseId, Integer.MAX_VALUE, 0)
              .orElseThrow(() -> new ServiceException("No Such course in db!"))
              .getLectures();
      scheduleStudent.addAll(scheduleCourse);

      ValidationHelper.checkScheduleHaveCrossing(scheduleStudent, locale);
      logger.debug("try to update student {}, course {} ", principal.getUser().getId(), courseId);
      boolean isUpdated = studentDao.updateCourseStudent(principal.getUser().getId(), courseId);
      if (!isUpdated) {
        throw new ClientServiceException("can't add student {} to course {} : course has filled.");
      }
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public boolean leaveCourse(Principal principal, int courseId)
      throws ClientServiceException, ServiceException, AccessDeniedException {
    if (principal.getUser().getRole() != Role.STUDENT) {
      ResourceBundle bundle =
          ResourceBundle.getBundle(
              RESOURCE_BUNDLE_ERROR_MESSAGE, principal.getSession().getLanguage().getLocale());
      throw new AccessDeniedException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
    }
    try {
      if (!courseDao.isExistCourseReady(courseId)) {
        ResourceBundle bundle =
            ResourceBundle.getBundle(
                RESOURCE_BUNDLE_ERROR_MESSAGE, principal.getSession().getLanguage().getLocale());
        throw new ClientServiceException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_WRONG_COURSE_ID));
      }
      int updateRows =
          studentDao.updateOnRemoveCourseFromStudentSchedule(principal.getUser().getId(), courseId);
      logger.debug("updateRows = {}", updateRows);
      if (updateRows > 1) {
        ResourceBundle bundle =
            ResourceBundle.getBundle(
                RESOURCE_BUNDLE_ERROR_MESSAGE, principal.getSession().getLanguage().getLocale());
        throw new ServiceException(
            String.format(bundle.getString("UNEXPECTED_RESULT") + " %d ", updateRows));
      }
      return updateRows == 1;

    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public boolean isInUserListCourse(Principal principal, int courseId)
      throws ClientServiceException, ServiceException {
    final boolean isInPersonalList;
    try {
//      if (!courseDao.isExistCourseReady(courseId)) {
//        ResourceBundle bundle =
//            ResourceBundle.getBundle(
//                RESOURCE_BUNDLE_ERROR_MESSAGE, principal.getSession().getLanguage().getLocale());
//        throw new ClientServiceException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_WRONG_COURSE_ID));
//      }
      switch (principal.getUser().getRole()) {
        case STUDENT:
          {
            isInPersonalList =
                studentDao.isExistsCourseInStudentList(courseId, principal.getUser().getId());
            logger.debug("in Student List: {}", isInPersonalList);
            break;
          }
        case LECTURER:
          {
            isInPersonalList =
                courseDao.isExistsCourseInLecturerUpdateList(courseId, principal.getUser().getId());
            logger.debug("in Lecturer List: {}", isInPersonalList);
            break;
          }
        default:
          {
            // Role.ANONYMOUS
            isInPersonalList = false;
          }
      }
    } catch (DaoException e) {
      throw new ServiceException(e);
    }

    return isInPersonalList;
  }
}
