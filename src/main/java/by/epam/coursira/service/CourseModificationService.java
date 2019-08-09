package by.epam.coursira.service;

import by.epam.coursira.dao.CourseDao;
import by.epam.coursira.dao.UserDao;
import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecture;
import by.epam.coursira.entity.Lecturer;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.entity.User;
import by.epam.coursira.exception.AccessDeniedException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.mail.MailSender;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class is intended for procedures are related to study course modification */
public class CourseModificationService {
  private static final Logger logger = LogManager.getLogger();
  private final CourseDao courseDao;
  private final UserDao userDao;
  private final MailSender mailSender;
  private static final String INVITATION_MESSAGE = "You've been invited to course";
  private static final String INVITATION_SUBJECT = "Invitation";
  private static final String DELETING_MESSAGE = "You were deleted from course";
  private static final String DELETING_SUBJECT = "Information";
  private static final String RESOURCE_BUNDLE_ERROR_MESSAGE = "errorMessages";
  private static final String RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED = "ACCESS_DENIED";
  private static final String RESOURCE_BUNDLE_MESSAGE_UPDATE_ACCESS_DENIED = "UPDATE_ACCESS_DENIED";
  private static final String RESOURCE_BUNDLE_MESSAGE_WRONG_LECTURER_ID = "WRONG_LECTURER_ID";

  private static final String COURSE_FIELD_DESCRIPTION = "description";

  public CourseModificationService(CourseDao courseDao, UserDao userDao, MailSender mailSender) {
    this.userDao = userDao;
    this.courseDao = courseDao;
    this.mailSender = mailSender;
  }

  /**
   * Method for creating new course
   *
   * @param principal current principal
   * @param title new course's title
   * @param description new course's description
   * @param capacity new course's capacity
   * @return new course's id.
   * @throws ClientServiceException when input parameters incorrect
   * @throws ServiceException when server errors occurs
   * @throws AccessDeniedException then principal's role is not LECTURER
   */
  public int createCourse(Principal principal, String title, String description, int capacity)
      throws ClientServiceException, ServiceException, AccessDeniedException {
    User user = principal.getUser();
    if (user.getRole() != Role.LECTURER) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle =
          ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, Locale.getDefault());
      throw new AccessDeniedException(bundle.getString(RESOURCE_BUNDLE_MESSAGE_ACCESS_DENIED));
    }
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    title = ValidationHelper.validateText(title, currentLocale, "title");
    description =
        ValidationHelper.validateText(description, currentLocale, COURSE_FIELD_DESCRIPTION);
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
            .withLectures(new ArrayList<>())
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
   * @param principal current principal
   * @param courseId current course's id
   * @param title new title
   * @param description new description value
   * @param capacity new capacity
   * @throws ClientServiceException when input parameters incorrect
   * @throws ServiceException when server errors occurs
   * @throws AccessDeniedException then user is not allowed to update current course
   */
  public void updateCourse(
      Principal principal, int courseId, String title, String description, int capacity)
      throws ClientServiceException, ServiceException, AccessDeniedException {
    // check permission! must be in PrincipalList
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, currentLocale);
    try {
      if (!courseDao.isExistsCourseInLecturerUpdateList(courseId, principal.getUser().getId())) {
        throw new AccessDeniedException(
            bundle.getString(RESOURCE_BUNDLE_MESSAGE_UPDATE_ACCESS_DENIED));
      }
      logger.debug(
          "access to update course {} by {}  confirmed", courseId, principal.getUser().getId());
      // check values
      title = ValidationHelper.validateText(title, currentLocale, "title");
      description =
          ValidationHelper.validateText(description, currentLocale, COURSE_FIELD_DESCRIPTION);
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
      throws ClientServiceException, ServiceException, AccessDeniedException {
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, currentLocale);
    try {
      if (!courseDao.isExistsCourseInLecturerUpdateList(courseId, principal.getUser().getId())) {
        throw new AccessDeniedException(
            bundle.getString(RESOURCE_BUNDLE_MESSAGE_UPDATE_ACCESS_DENIED));
      }
      if (principal.getUser().getId() == invitedLecturerId) {
        // lecturer can't add himself
        throw new ClientServiceException(bundle.getString("CANT_ADD_YOURSELF"));
      }
      User invitedUser =
          userDao
              .selectUserById(invitedLecturerId)
              .orElseThrow(
                  () ->
                      new ClientServiceException(
                          bundle.getString(RESOURCE_BUNDLE_MESSAGE_WRONG_LECTURER_ID)));
      if (invitedUser.getRole() != Role.LECTURER) {
        throw new ClientServiceException(
            bundle.getString(RESOURCE_BUNDLE_MESSAGE_WRONG_LECTURER_ID));
      }
      courseDao.upsertCourseLecturer(courseId, invitedLecturerId);
      try {
        mailSender.sendMail(invitedUser.getEmail(), INVITATION_SUBJECT, INVITATION_MESSAGE);
      } catch (MessagingException e) {
        logger.warn(e.getMessage());
        courseDao.deleteCourseLecturer(courseId, invitedLecturerId);
        throw new ClientServiceException(bundle.getString("CANT_SEND_MESSAGE"));
      }

    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public void deleteLecturerFromCourse(Principal principal, int courseId, int lecturerId)
      throws ClientServiceException, ServiceException, AccessDeniedException {
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, currentLocale);
    try {
      // check principal access
      if (!courseDao.isExistsCourseInLecturerUpdateList(courseId, principal.getUser().getId())) {
        throw new AccessDeniedException(
            bundle.getString(RESOURCE_BUNDLE_MESSAGE_UPDATE_ACCESS_DENIED));
      }
      if (principal.getUser().getId() == lecturerId) {
        // lecturer can't delete himself
        throw new ClientServiceException(bundle.getString("CANT_DELETE_YOURSELF"));
      }
      // check if current course is in deleted lecturer list
      if (!courseDao.isExistsCourseInLecturerUpdateList(courseId, lecturerId)) {
        throw new ClientServiceException(
            bundle.getString(RESOURCE_BUNDLE_MESSAGE_WRONG_LECTURER_ID));
      }
      // try to delete.
      if (courseDao.deleteCourseLecturer(courseId, lecturerId) == 0) {
        throw new ClientServiceException(bundle.getString("CANT_DELETE_LECTURER"));
      }
      // send email to lecturer
      try {
        User invitedUser =
            userDao
                .selectUserById(lecturerId)
                .orElseThrow(
                    () ->
                        new ClientServiceException(
                            bundle.getString(RESOURCE_BUNDLE_MESSAGE_WRONG_LECTURER_ID)));
        mailSender.sendMail(invitedUser.getEmail(), DELETING_SUBJECT, DELETING_MESSAGE);
      } catch (MessagingException e) {
        logger.warn(e.getMessage());
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

  public void createLecture(
      Principal principal,
      int courseId,
      String description,
      Instant lectureBegin,
      Instant lectureFinish)
      throws ClientServiceException, ServiceException, AccessDeniedException {
    User user = principal.getUser();
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, currentLocale);
    try {
      if (!courseDao.isExistsCourseInLecturerUpdateList(courseId, user.getId())) {
        throw new AccessDeniedException(
            bundle.getString(RESOURCE_BUNDLE_MESSAGE_UPDATE_ACCESS_DENIED));
      }
      logger.debug(
          "access to create lecture in course {} by lecturer {} confirmed",
          courseId,
          principal.getUser().getId());
      // check values
      description =
          ValidationHelper.validateText(description, currentLocale, COURSE_FIELD_DESCRIPTION);
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
          courseDao.selectScheduleByLecturerId(principal.getUser().getId(), Integer.MAX_VALUE, 0);
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
      logger.debug("course {} was added to db", courseId);

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
      throws ClientServiceException, ServiceException, AccessDeniedException {
    User user = principal.getUser();
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, currentLocale);
    try {
      if (!courseDao.isExistsLectureInLecturerUpdateList(lectureId, user.getId())) {
        throw new AccessDeniedException(
            bundle.getString(RESOURCE_BUNDLE_MESSAGE_UPDATE_ACCESS_DENIED));
      }
      logger.debug("access confirmed");
      // check values
      description =
          ValidationHelper.validateText(description, currentLocale, COURSE_FIELD_DESCRIPTION);
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
          courseDao.selectScheduleByLecturerId(principal.getUser().getId(), Integer.MAX_VALUE, 0);

      scheduleLecturer =
          scheduleLecturer.stream()
              .filter(lecture -> lecture.getId() != lectureId)
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
              .filter(lecture -> lecture.getId() != lectureId)
              .collect(Collectors.toList());

      courseSchedule.add(current);
      ValidationHelper.checkScheduleHaveCrossing(courseSchedule, currentLocale);

      courseDao.updateLecture(current);
      logger.debug("Lecture {} updated", lectureId);

    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public void deleteLecture(Principal principal, int lectureId)
      throws ServiceException, AccessDeniedException {
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, currentLocale);
    try {
      if (!courseDao.isExistsLectureInLecturerUpdateList(lectureId, principal.getUser().getId())) {
        throw new AccessDeniedException(
            bundle.getString(RESOURCE_BUNDLE_MESSAGE_UPDATE_ACCESS_DENIED));
      }
      logger.debug("access confirmed");
      courseDao.deleteLecture(lectureId);
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  public void activateCourse(Principal principal, int courseId)
      throws ClientServiceException, ServiceException, AccessDeniedException {
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, currentLocale);
    try {
      if (!courseDao.isExistsCourseInLecturerUpdateList(courseId, principal.getUser().getId())) {
        throw new AccessDeniedException(
            bundle.getString(RESOURCE_BUNDLE_MESSAGE_UPDATE_ACCESS_DENIED));
      }
      // check if all lecturers has at least one lecture
      Map<Lecturer, Integer> lecturerMap = courseDao.countLecturesInCourse(courseId);
      for (Map.Entry<Lecturer, Integer> entry : lecturerMap.entrySet()) {
        if (entry.getValue() == 0) {
          throw new ClientServiceException(
              String.format(
                  bundle.getString("LECTURER_HAS_NO_LECTURES"),
                  entry.getKey().getFirstName(),
                  entry.getKey().getLastName()));
        }
      }
      // if we here means we can try to activate
      boolean isActivate = courseDao.updateCourseOnSetReady(courseId);
      if (!isActivate) {
        throw new ClientServiceException(bundle.getString("CANT_ACTIVATE COURSE"));
      }
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }
}
