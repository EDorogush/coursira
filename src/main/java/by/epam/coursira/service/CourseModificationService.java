package by.epam.coursira.service;

import by.epam.coursira.dao.CourseDao;
import by.epam.coursira.dao.UserDao;
import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecture;
import by.epam.coursira.entity.Lecturer;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.entity.User;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.mail.MailSender;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CourseModificationService {
  private static final Logger logger = LogManager.getLogger();
  private final CourseDao courseDao;
  private final UserDao userDao;
  private final MailSender mailSender;
  private static final String INVITATION_MESSAGE = "You've been invited to course";
  private static final String INVITATION_SUBJECT = "Invitation";

  public CourseModificationService(CourseDao courseDao, UserDao userDao, MailSender mailSender) {
    this.userDao = userDao;
    this.courseDao = courseDao;
    this.mailSender = mailSender;
  }

  /**
   * Creates empty course, without lectures
   *
   * @param principal
   * @param title
   * @param description
   * @param capacity
   * @return
   * @throws ClientServiceException
   * @throws ServiceException
   */
  public int createCourse(Principal principal, String title, String description, int capacity)
      throws ClientServiceException, ServiceException {
    User user = principal.getUser();
    if (user.getRole() != Role.LECTURER) {
      throw new ServiceException("access denied");
    }
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    //    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", currentLocale);
    title = ValidationHelper.validateText(title, currentLocale, "title");
    description = ValidationHelper.validateText(description, currentLocale, "description");
    ValidationHelper.checkCourseCapacity(capacity, currentLocale);

    // note: de do not need lecturer name here
    List<Lecturer> lecturers = new ArrayList<>();
    lecturers.add(new Lecturer(user.getId(), user.getFirstName(), user.getLastName()));
    Course course =
        new Course.Builder()
            .withTitle(title)
            .withDescription(description)
            .withCapacity(capacity)
            .withStudentsAmount(0)
            .withLecturers(lecturers)
            .withLectures(new ArrayList<Lecture>())
            .build();
    try {
      return courseDao.insertCourse(course);
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  /**
   * Method updates title, description, capacity of selected course. this method is avaliable for
   * Lecturers, who reads this course.
   *
   * @param principal
   * @param courseId
   * @param title
   * @param description
   * @param capacity
   * @throws ClientServiceException
   * @throws ServiceException
   */
  public void updateCourse(
      Principal principal, int courseId, String title, String description, int capacity)
      throws ClientServiceException, ServiceException {
    // check permission! must be in PrincipalList
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", currentLocale);
    try {
      if (!courseDao.isExistsCourseInLecturerUpdateList(courseId, principal.getUser().getId())) {
        throw new ServiceException(bundle.getString("UPDATE_ACCESS_DENIED"));
      }
      logger.debug("access confirmed");
      // check values
      title = ValidationHelper.validateText(title, currentLocale, "title");
      description = ValidationHelper.validateText(description, currentLocale, "description");
      ValidationHelper.checkCourseCapacity(capacity, currentLocale);
      Course course =
          new Course.Builder()
              .withId(courseId)
              .withTitle(title)
              .withDescription(description)
              .withCapacity(capacity)
              .build();
      courseDao.updateCourseData(course);
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public void addLecturerToCourse(Principal principal, int courseId, int invitedLecturerId)
      throws ClientServiceException, ServiceException {
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", currentLocale);
    try {
      if (!courseDao.isExistsCourseInLecturerUpdateList(courseId, principal.getUser().getId())) {
        throw new ServiceException(bundle.getString("UPDATE_ACCESS_DENIED"));
      }
      User invitedUser =
          userDao
              .selectUserById(invitedLecturerId)
              .orElseThrow(() -> new ClientServiceException(bundle.getString("WRONG_LECTURER_ID")));
      if (invitedUser.getRole() != Role.LECTURER) {
        throw new ClientServiceException(bundle.getString("WRONG_LECTURER_ID"));
      }
      courseDao.upsertCourseLecturer(courseId, invitedLecturerId);
      try {
        mailSender.sendMail(invitedUser.getEmail(), INVITATION_SUBJECT, INVITATION_MESSAGE);
      } catch (MessagingException e) {
        logger.error(e.getMessage());
        courseDao.deleteCourseLecturer(courseId, invitedLecturerId);
        throw new ClientServiceException(bundle.getString("CANT_SEND_MESSAGE"));
      }

    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public boolean isAllowToUpdateCourse(Principal principal, int courseId) throws ServiceException {
    try {
      return courseDao.isExistsCourseInLecturerUpdateList(courseId, principal.getUser().getId());
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public boolean isAllowToUpdateLecture(Principal principal, int lectureId)
      throws ServiceException {
    try {
      return courseDao.isExistsLectureInLecturerUpdateList(lectureId, principal.getUser().getId());
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public void createLecture(
      Principal principal,
      int courseId,
      String description,
      Instant lectureBegin,
      Instant lectureFinish)
      throws ClientServiceException, ServiceException {
    User user = principal.getUser();
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", currentLocale);
    try {
      if (!courseDao.isExistsCourseInLecturerUpdateList(courseId, user.getId())) {
        throw new ServiceException(bundle.getString("UPDATE_ACCESS_DENIED"));
      }
      logger.debug("access confirmed");
      // check values
      description = ValidationHelper.validateText(description, currentLocale, "description");
      ValidationHelper.checkLectureDatesTimes(lectureBegin, lectureFinish, currentLocale);
      // check crossing within lecturer schedule
      Lecture createdLecture =
          new Lecture.Builder()
              .withDescription(description)
              .withTimeStart(lectureBegin)
              .withTimeEnd(lectureFinish)
              .withLecturer(new Lecturer(user.getId(), user.getFirstName(), user.getLastName()))
              .withCourseId(courseId)
              .build();

      List<Lecture> scheduleLecturer =
          courseDao.selectScheduleByLecturerId(principal.getUser().getId());
      scheduleLecturer.add(createdLecture);
      ValidationHelper.checkScheduleHaveCrossing(scheduleLecturer, currentLocale);
      // check crossing within course schedule
      List<Lecture> courseSchedule =
          courseDao
              .selectCourseById(courseId, Integer.MAX_VALUE, 0)
              .orElseThrow(() -> new ServiceException("No Such course in db!"))
              .getLectures();
      courseSchedule.add(createdLecture);
      ValidationHelper.checkScheduleHaveCrossing(courseSchedule, currentLocale);

      courseDao.insertLecture(createdLecture);
      logger.info("course {} was added to db", courseId);

    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public void updateLecture(
      Principal principal,
      int lectureId,
      int courseId,
      String description,
      Instant lectureBegin,
      Instant lectureFinish)
      throws ClientServiceException, ServiceException {
    User user = principal.getUser();
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", currentLocale);
    try {
      if (!courseDao.isExistsLectureInLecturerUpdateList(lectureId, user.getId())) {
        throw new ServiceException(bundle.getString("UPDATE_ACCESS_DENIED"));
      }
      logger.debug("access confirmed");
      // check values
      description = ValidationHelper.validateText(description, currentLocale, "description");
      ValidationHelper.checkLectureDatesTimes(lectureBegin, lectureFinish, currentLocale);
      // check crossing within lecturer schedule
      Lecture current =
          new Lecture.Builder()
              .withLectureId(lectureId)
              .withDescription(description)
              .withTimeStart(lectureBegin)
              .withTimeEnd(lectureFinish)
              .withLecturer(new Lecturer(user.getId(), user.getFirstName(), user.getLastName()))
              .build();
      List<Lecture> scheduleLecturer =
          courseDao.selectScheduleByLecturerId(principal.getUser().getId());

      scheduleLecturer =
          scheduleLecturer.stream()
              .filter((lecture) -> lecture.getId() != lectureId)
              .collect(Collectors.toList());
      scheduleLecturer.add(current);
      ValidationHelper.checkScheduleHaveCrossing(scheduleLecturer, currentLocale);
      // check crossing within course schedule
      List<Lecture> courseSchedule =
          courseDao
              .selectCourseById(courseId, Integer.MAX_VALUE, 0)
              .orElseThrow(() -> new ServiceException("No Such course in db!"))
              .getLectures();
      courseSchedule =
          courseSchedule.stream()
              .filter((lecture -> lecture.getId() != lectureId))
              .collect(Collectors.toList());

      courseSchedule.add(current);
      ValidationHelper.checkScheduleHaveCrossing(courseSchedule, currentLocale);

      courseDao.updateLecture(current);
      logger.info("Lecture {} updated", lectureId);

    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public void deleteLecture(Principal principal, int lectureId)
      throws ClientServiceException, ServiceException {
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", currentLocale);
    try {
      if (!courseDao.isExistsLectureInLecturerUpdateList(lectureId, principal.getUser().getId())) {
        throw new ClientServiceException(bundle.getString("UPDATE_ACCESS_DENIED"));
      }
      logger.debug("access confirmed");
      courseDao.deleteLecture(lectureId);
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public void activateCourse(Principal principal, int courseId)
      throws ClientServiceException, ServiceException {
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", currentLocale);
    try {
      if (!courseDao.isExistsCourseInLecturerUpdateList(courseId, principal.getUser().getId())) {
        throw new ServiceException(bundle.getString("UPDATE_ACCESS_DENIED"));
      }
      boolean isActivate = courseDao.updateCourseOnSetReady(courseId);
      if (!isActivate) {
        throw new ClientServiceException(bundle.getString("CANT_ACTIVATE COURSE"));
      }

    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }
}
