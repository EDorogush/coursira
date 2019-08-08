package by.epam.coursira.service;

import by.epam.coursira.dao.UserDao;
import by.epam.coursira.entity.Lecturer;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.entity.User;
import by.epam.coursira.exception.AccessDeniedException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.ServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.annotation.Nullable;
import javax.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserService {
  private static final Logger logger = LogManager.getLogger();
  private final UserDao userDao;

  public UserService(UserDao userDao) {
    this.userDao = userDao;
  }

  /**
   * Method updated photo field in user table of current principal user. This method is available
   * for authorised user.
   *
   * @param principal {@link Principal} current principal
   * @param image {@link Part} image. must be of image type.
   * @return {@link Principal} with updated field;
   * @throws ClientServiceException when {@link Part} image is incorrect.
   * @throws ServiceException attempt to update fails.
   * @throws AccessDeniedException when user has role = Role.ANONYMOUS
   */
  public Principal updateUserPhoto(Principal principal, Part image)
      throws ClientServiceException, ServiceException, AccessDeniedException {
    final Principal current;
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    if (principal.getUser().getRole() == Role.ANONYMOUS) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", Locale.getDefault());
      throw new AccessDeniedException(bundle.getString("ACCESS_DENIED"));
    }
    ValidationHelper.checkImage(image, currentLocale);
    try (InputStream imageInputStream = image.getInputStream()) {
      userDao.updateUserPhoto(principal.getUser().getId(), imageInputStream);
      current =
          userDao
              .selectPrincipalBySessionId(principal.getSession().getId())
              .orElseThrow(() -> new ServiceException("No such user"));
    } catch (IOException | DaoException e) {
      throw new ServiceException(e);
    }
    return current;
  }

  /**
   * Method updated user's data in database. This method is available for authorised user. Returns
   * updated {@link Principal} or throws ClientServiceException when input user data is incorrect.
   *
   * @param principal current {@link Principal}.
   * @param firstName {@link String} user's new first name.
   * @param lastName {@link String} user's new lastName.
   * @param age {@link Integer} user's new age.
   * @param organization {@link String} user's new organization.
   * @param interests {@link String} user's new interests.
   * @return updated {@link Principal} object.
   * @throws ClientServiceException when input user data is incorrect.
   * @throws ServiceException when attempt to update record fails.
   * @throws AccessDeniedException when user has role = Role.ANONYMOUS
   */
  public Principal updateUserData(
      Principal principal,
      String firstName,
      String lastName,
      @Nullable Integer age,
      @Nullable String organization,
      @Nullable String interests)
      throws ClientServiceException, ServiceException,AccessDeniedException {
    if (principal.getUser().getRole() == Role.ANONYMOUS) {
      Locale.setDefault(principal.getSession().getLanguage().getLocale());
      ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", Locale.getDefault());
      throw new AccessDeniedException(bundle.getString("ACCESS_DENIED"));
    }
    Locale currentLocale = principal.getSession().getLanguage().getLocale();
    firstName = ValidationHelper.validateText(firstName, currentLocale, "First Name");
    lastName = ValidationHelper.validateText(lastName, currentLocale, "Last Name");
    if (Optional.ofNullable(age).isPresent()) {
      ValidationHelper.checkAge(age, currentLocale);
    }
    if (Optional.ofNullable(organization).isPresent()) {
      organization =
          ValidationHelper.validateTextNullable(organization, currentLocale, "Organisation");
    }
    if (Optional.ofNullable(interests).isPresent()) {
      interests = ValidationHelper.validateTextNullable(interests, currentLocale, "Interests");
    }
    User user = principal.getUser();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setAge(age);
    user.setOrganization(organization);
    user.setInterests(interests);
    logger.info(user.toString());
    try {
      userDao.updateUser(user);
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
    return principal;
  }

  /**
   * Method provide information about all lecturers are registered in the system.
   *
   * @param principal current user's {@link Principal}.
   * @return {@link List}<{@link Lecturer} > the list of registered lecturers.
   * @throws ServiceException if attempt to get data fails.
   */
  public List<Lecturer> findAllLecturersList(Principal principal) throws ServiceException {
    try {
      List<User> users = userDao.selectUsersByRole(Role.LECTURER);
      List<Lecturer> lecturers = new ArrayList<>(users.size());
      for (User user : users) {
        lecturers.add(new Lecturer(user.getId(), user.getFirstName(), user.getLastName()));
      }
      return lecturers;
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  /**
   * Method returns {@link Lecturer} with id specified in argument. Throws ClientServiceException if
   * there is no such lecturer in db or it's registration doesn't finish yet.
   *
   * @param principal current user's {@link Principal}.
   * @param id {@code int} value of lecturer id.
   * @return {@link Lecturer}
   * @throws ClientServiceException if there is no such lecturer in db or it's registration doesn't
   *     finish yet
   * @throws ServiceException if attempt to get data fails.
   */
  public Lecturer defineLecturerNameById(Principal principal, int id)
      throws ClientServiceException, ServiceException {
    Locale locale = principal.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle("errorMessages", locale);
    final Lecturer lecturer;
    try {
      User user =
          userDao
              .selectUserById(id)
              .orElseThrow(() -> new ClientServiceException(bundle.getString("WRONG_LECTURER_ID")));
      if (user.getRole() != Role.LECTURER || user.getRegistrationCode() == null) {
        throw new ClientServiceException(bundle.getString("WRONG_LECTURER_ID"));
      }
      lecturer = new Lecturer(user.getId(), user.getFirstName(), user.getLastName());
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
    return lecturer;
  }
}
