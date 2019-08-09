package by.epam.coursira.service;

import by.epam.coursira.dao.UserDao;
import by.epam.coursira.entity.Language;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.entity.Session;
import by.epam.coursira.entity.User;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.mail.MailSender;
import by.epam.coursira.security.HashMethod;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class PrincipalService {
  private static final Logger logger = LogManager.getLogger();
  private static final int DEFAULT_PRINCIPAL_USER_ID = 0;
  private static final Language DEFAULT_PRINCIPAL_LANGUAGE = Language.EN;
  private static final ZoneOffset DEFAULT_PRINCIPAL_ZONE_OFFSET = ZoneOffset.ofTotalSeconds(0);
  private static final Duration REGISTRATION_LINK_VALID_DURATION = Duration.ofHours(1);
  private static final String RESOURCE_BUNDLE_ERROR_MESSAGE = "errorMessages";
  private static final String RESOURCE_BUNDLE_MESSAGE_WRONG_LOGIN_AND_PASSWORD =
      "WRONG_LOGIN_AND_PASSWORD";
  private static final String REGISTRATION_CONFIRM_SUBJECT = "Registration Confirm";
  private static final String REGISTRATION_MESSAGE_PATTERN =
      "Dear %s, \n"
          + "Thank you for registering to CoursIra.\n"
          + "Follow link placed below to confirm your registration.\n"
          + "Attention: Current link is active for %d hours. If you don't use it, your registration will be canceled:\n"
          + " %s";
  private final Duration sessionLoginDuration;
  private final Duration sessionAnonymousDuration;
  private final UserDao userDao;
  private final HashMethod hashMethod;
  private final MailSender mailSender;

  public PrincipalService(
      UserDao dao,
      Duration sessionLoginDuration,
      Duration sessionAnonymousDuration,
      HashMethod hashMethod,
      MailSender mailSender) {
    this.userDao = dao;
    this.hashMethod = hashMethod;
    this.mailSender = mailSender;
    this.sessionAnonymousDuration = sessionAnonymousDuration;
    this.sessionLoginDuration = sessionLoginDuration;
  }

  /**
   * Method invokes user authentification via session by session's id value specified by argument.
   * All active sessions are kept in database with the purpose to provide stateless application.
   * When {@link User} is defined, his session time doesn't update. If Session record wasn't found
   * in db, method creates new session record in db and links current session to {@link Role}
   * Anonymous with session time duration {@code sessionAnonymousDuration}. Returns current {@link
   * Principal}
   *
   * @param sessionId value of current session's Id.
   * @return {@link Principal} principal
   * @throws ServiceException when verification fails
   */
  public Principal verifyPrincipleBySessionId(String sessionId) throws ServiceException {
    final Principal principal;
    try {
      Optional<Principal> principalOptional = userDao.selectPrincipalBySessionId(sessionId);
      // check if session record was found in db
      if (principalOptional.isPresent()) {
        logger.debug(
            "Session record was found in db: {}.", principalOptional.get().getUser().toString());
        // session record exists so check if session is not expired
        Session session = principalOptional.get().getSession();
        if (session.getExpDate().isBefore(Instant.now())) {
          // session expired so need to update record - link it to default user)
          session.setUserId(DEFAULT_PRINCIPAL_USER_ID);
          session.setExpDate(Instant.now().plus(sessionAnonymousDuration));
          userDao.updateSession(session);
          logger.debug("Session expired. Session record was linked to ANONYMOUS user");
          principal =
              userDao
                  .selectPrincipalBySessionId(sessionId)
                  .orElseThrow(() -> new ServiceException("Can't read principal from db"));
        } else {
          principal = principalOptional.get();
        }
      } else {
        logger.debug("session {} record wasn't found in db", sessionId);
        User user =
            userDao
                .selectUserById(DEFAULT_PRINCIPAL_USER_ID)
                .orElseThrow(() -> new ServiceException("Can't read default user"));
        Session session =
            new Session.Builder()
                .setId(sessionId)
                .setExpDate(Instant.now().plus(sessionAnonymousDuration))
                .setZoneOffSet(DEFAULT_PRINCIPAL_ZONE_OFFSET)
                .setLanguage(DEFAULT_PRINCIPAL_LANGUAGE)
                .setUserId(DEFAULT_PRINCIPAL_USER_ID)
                .build();
        principal = new Principal(session, user);
        userDao.insertSession(session);
        logger.debug("session record inserted to db");
      }
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
    return principal;
  }

  /**
   * Method invokes user authentification via email and password values specified in arguments. User
   * data is kept in database with the purpose to provide stateless application. Method also check
   * if registration time to be null or to be not expired. Returns authorised {@link Principal} or
   * throws {@code ClientServiceException} then password or login don't match ones in database, or
   * registration time is not {@code null}.
   *
   * @param email {@link String} user email.
   * @param password {@link String} user password
   * @return authorised {@link Principal}
   * @throws ClientServiceException then password or login don't match ones in database, or
   *     registration time is not {@code null}.
   * @throws ServiceException when method fails
   */
  public Principal verifyPrincipleByPass(
      @NotNull Principal previous, @NotNull String email, @NotNull String password)
      throws ServiceException, ClientServiceException {
    final Principal principal;
    ResourceBundle bundle =
        ResourceBundle.getBundle(
            RESOURCE_BUNDLE_ERROR_MESSAGE, previous.getSession().getLanguage().getLocale());
    // check if user authorized
    if (previous.getUser().getRole() != Role.ANONYMOUS) {
      logout(previous);
    }
    // verity if principal is in db. if so, check registration code existence. add record to
    // sessions table
    try {
      User user =
          userDao
              .selectUserByEmail(email)
              .orElseThrow(
                  () ->
                      new ClientServiceException(
                          bundle.getString(RESOURCE_BUNDLE_MESSAGE_WRONG_LOGIN_AND_PASSWORD)));
      logger.debug("user {} was found in db", user.toString());
      if (user.getRegistrationExpDate() != null) {
        if (user.getRegistrationExpDate().isBefore(Instant.now())) {
          logger.debug("registration time expired. user will be deleted.");
          userDao.deleteUser(user.getId());
          throw new ClientServiceException(
              bundle.getString(RESOURCE_BUNDLE_MESSAGE_WRONG_LOGIN_AND_PASSWORD));
        } else throw new ClientServiceException(bundle.getString("DOESNT_FINISH_REGISTRATION"));
      }

      if (!hashMethod.verify(password, user.getPassword())) {
        logger.debug("Password didn't fit the db record.");
        throw new ClientServiceException(
            bundle.getString(RESOURCE_BUNDLE_MESSAGE_WRONG_LOGIN_AND_PASSWORD));
      }
      // login and pass checked. increase session time.
      logger.debug("Password checked.");
      Session session = previous.getSession();
      session.setUserId(user.getId());
      session.setExpDate(Instant.now().plus(sessionLoginDuration));
      userDao.updateSession(session);
      logger.debug("Session record was linked to {} user and increase session time.", email);
      principal = new Principal(session, user);
    } catch (DaoException e) {
      throw new ServiceException(e.getMessage());
    }
    return principal;
  }

  /**
   * Method updates currant session record in database by linking it to default Anonymous user.
   * Returns {@link Principal} with default anonymous {@link Role}.
   *
   * @param current {@link Principal} current User's principal.
   * @return updated {@link Principal} with default anonymous {@link Role}.
   * @throws ServiceException when attempt to logout fails.
   */
  public Principal logout(Principal current) throws ServiceException {
    Session session = current.getSession();
    session.setUserId(DEFAULT_PRINCIPAL_USER_ID);
    session.setExpDate(Instant.now().plus(sessionAnonymousDuration));

    try {
      userDao.updateSession(session);
      User user =
          userDao
              .selectUserById(DEFAULT_PRINCIPAL_USER_ID)
              .orElseThrow(() -> new ServiceException("Can't read default user"));
      return new Principal(session, user);
    } catch (DaoException e) {
      throw new ServiceException(e.getMessage());
    }
  }

  /**
   * Method invokes creation new record in database for user with data specified in arguments.
   * Method also created random Registration code that is going to kept in user record for {@code
   * REGISTRATION_LINK_VALID_DURATION} time. When record is inserted to database, confirming message
   * is sent to user's E-mail. Throws ClientServiceException when user's data is incorrect.
   *
   * @param previous {@link Principal} principal value before registration. if role wasn't
   *     Anonuymous, logout procedure invokes.
   * @param urlToGoFromEmail {@link String} URL which is going to be sent via email to user to
   *     finish registration.
   * @param email {@link String} user's email.
   * @param passwordFirst {@link String} user's password.
   * @param passwordSecond {@link String} user's password needed to confirm password offered by
   *     user.
   * @param firstName {@link String} user's first name
   * @param lastName {@link String} user's last name
   * @param role {@link Role} user's role
   * @throws ClientServiceException when user's data is incorrect.
   * @throws ServiceException when attempt to create user record fails
   */
  public void registerUser(
      Principal previous,
      String urlToGoFromEmail,
      String email,
      String passwordFirst,
      String passwordSecond,
      String firstName,
      String lastName,
      Role role)
      throws ClientServiceException, ServiceException {

    Locale currentLocale = previous.getSession().getLanguage().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_ERROR_MESSAGE, currentLocale);
    // check if user authorised
    if (previous.getUser().getRole() != Role.ANONYMOUS) {
      logout(previous);
    }
    // 1. validation
    email = ValidationHelper.validateText(email, currentLocale, "email");
    passwordFirst = ValidationHelper.validateText(passwordFirst, currentLocale, "passwordFirst");
    passwordSecond = ValidationHelper.validateText(passwordSecond, currentLocale, "passwordSecond");
    firstName = ValidationHelper.validateText(firstName, currentLocale, "firstName");
    lastName = ValidationHelper.validateText(lastName, currentLocale, "lastName");
    if (role == Role.ANONYMOUS) {
      throw new ClientServiceException(bundle.getString("YOU_NEED_CHOSE_ROLE"));
    }
    // 2. check if such email is in DB
    try {
      if (userDao.isExistsEmail(email)) {
        logger.debug("user exists");
        throw new ClientServiceException(bundle.getString("USER_ALREADY_EXISTS"));
      }

      if (!passwordFirst.equals(passwordSecond)) {
        throw new ClientServiceException(bundle.getString("PASSWORDS_DO_NOT_MATCH"));
      }
      final String registrationCode = UUID.randomUUID().toString();
      final Instant registrationExpDate = Instant.now().plus(REGISTRATION_LINK_VALID_DURATION);
      User user =
          new User.Builder()
              .setEmail(email)
              .setPassword(BCrypt.hashpw(passwordFirst, BCrypt.gensalt()))
              .setFirstName(firstName.trim())
              .setLastName(lastName)
              .setRole(role)
              .setRegistrationCode(registrationCode)
              .setRegistrationExpDate(registrationExpDate)
              .build();
      int userId = userDao.insertUser(user);
      logger.debug("User record was added to db with userId {}", userId);
      try {
        String registrationLink = urlToGoFromEmail + "?code=" + registrationCode;
        String registrationMessage =
            String.format(
                REGISTRATION_MESSAGE_PATTERN,
                user.getFirstName() + user.getLastName(),
                REGISTRATION_LINK_VALID_DURATION.toHours(),
                registrationLink);
        mailSender.sendMail(email, REGISTRATION_CONFIRM_SUBJECT, registrationMessage);
      } catch (MessagingException e) {
        logger.debug(e.getMessage());
        logger.debug("delete user record from db");
        userDao.deleteUser(userId);
        throw new ClientServiceException(bundle.getString("CANT_SEND_MESSAGE"));
      }
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  /**
   * Method checks user's registration expire time ane updates user's registration code by setting
   * it to null if registration time doesn't expire. If so, user's record will be deleted.
   *
   * @param principal current {@link Principal} princpal of user in application. if role isn't
   *     Anonymous, logout procedure invokes.
   * @param code {@link String} registration code
   * @throws ServiceException when attempt to activate registration fails.
   * @throws ClientServiceException when registration time expired
   */
  public void activateRegistration(Principal principal, String code)
      throws ServiceException, ClientServiceException {
    ResourceBundle bundle =
        ResourceBundle.getBundle(
            RESOURCE_BUNDLE_ERROR_MESSAGE, principal.getSession().getLanguage().getLocale());
    if (!principal.getUser().getRole().equals(Role.ANONYMOUS)) {
      logout(principal);
    }
    try {
      User user =
          userDao
              .selectUserByCode(code)
              .orElseThrow(
                  () -> new ClientServiceException(bundle.getString("WRONG_REGISTRATION_CODE")));
      // check date
      if (user.getRegistrationExpDate().isBefore(Instant.now())) {
        logger.debug("registration time expired.");
        userDao.deleteUser(user.getId());
        throw new ClientServiceException(bundle.getString("REGISTRATION_TIME_EXPIRED"));
      }
      userDao.updateUserRegistrationToNull(user.getId());
      logger.debug("registration confirmed");
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
  }

  /**
   * Method updates session record in db by changing language to one's specified in argument.
   *
   * @param principal current principal
   * @param current {@link Language}
   * @return updated {@link Principal}
   * @throws ServiceException when attempt to update session fails.
   */
  public Principal changeLanguage(Principal principal, Language current) throws ServiceException {
    Session session = principal.getSession();
    session.setLanguage(current);
    try {
      userDao.updateSession(session);
    } catch (DaoException e) {
      throw new ServiceException(e);
    }
    return principal;
  }

  /**
   * Method invokes deleting all expired session records from db.
   *
   * @throws ServiceException when attempt to delete records fails.
   */
  public void cleanFromExpiredSessions() throws ServiceException {
    try {

      int count = userDao.deleteSession(Instant.now());
      logger.info("Session records cleaned. {} records was deleted", count);
    } catch (DaoException e) {
      logger.info("can't delete waste sessions");
      throw new ServiceException(e);
    }
  }

  /**
   * Method invokes deleting from db all user records with expired registration time.
   *
   * @throws ServiceException when attempt to delete records fails.
   */
  public void cleanFromExpiredRegistrationCode() throws ServiceException {
    try {
      int count = userDao.deleteUserWithRegistrationCodeExpired(Instant.now());
      logger.info("User table cleaned. {} expired records was deleted", count);
    } catch (DaoException e) {
      logger.info("can't delete waste users");
      throw new ServiceException(e);
    }
  }
}
