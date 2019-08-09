package by.epam.coursira.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;

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
import by.epam.coursira.security.BCryptHashMethod;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import javax.mail.MessagingException;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PrincipalServiceTest {

  @Mock UserDao mockUserDao;

  @Mock BCryptHashMethod mockBCryptHashing;

  @Mock MailSender mockMailSender;

  private User userAnonymous =
      new User.Builder()
          .setId(0)
          .setEmail("email")
          .setFirstName("firstName")
          .setLastName("lastName")
          .setPassword("password")
          .setRole(Role.ANONYMOUS)
          .build();

  private User userStudent;
  private Session session;
  private PrincipalService service;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Duration sessionLoginDuration = Duration.ofHours(2);
    Duration sessionAnonymousDuration = Duration.ofHours(1);


    service =
        new PrincipalService(
            mockUserDao,
            sessionLoginDuration,
            sessionAnonymousDuration,
            mockBCryptHashing,
            mockMailSender);

    userStudent =
        new User.Builder()
            .setId(1)
            .setEmail("email")
            .setFirstName("firstName")
            .setLastName("lastName")
            .setPassword(BCrypt.hashpw("password", BCrypt.gensalt()))
            .setRole(Role.STUDENT)
            .build();

    session =
        new Session.Builder()
            .setExpDate(Instant.now())
            .setId("abc")
            .setUserId(1)
            .setLanguage(Language.EN)
            .setZoneOffSet(ZoneOffset.UTC)
            .build();
  }

  @Test
  public void testVerifyPrincipleBySessionIdAuthenticationSucceed()
      throws DaoException, ServiceException {

    Principal expected = new Principal(session, userStudent);
    expected.getSession().setExpDate(Instant.now().plusSeconds(1000));

    when(mockUserDao.selectPrincipalBySessionId(anyString())).thenReturn(Optional.of(expected));

    Principal actual = service.verifyPrincipleBySessionId(expected.getSession().getId());

    verify(mockUserDao, never()).updateSession(any(Session.class));
    verify(mockUserDao, times(1)).selectPrincipalBySessionId(anyString());
    assertSame(actual.getUser().getRole(), Role.STUDENT);
  }

  @Test
  public void testVerifyPrincipleBySessionIdSessionTimeExpired()
      throws DaoException, ServiceException {
    // given
    session.setExpDate(Instant.now().minusSeconds(100));
    Principal verified = new Principal(session, userStudent);

    Principal expected = new Principal(session, userAnonymous);
    when(mockUserDao.selectPrincipalBySessionId(anyString()))
        .thenReturn(Optional.of(verified))
        .thenReturn(Optional.of(expected));
    // when
    Principal actual = service.verifyPrincipleBySessionId(verified.getSession().getId());
    // then
    verify(mockUserDao, times(1)).updateSession(any(Session.class));
    verify(mockUserDao, times(2)).selectPrincipalBySessionId(anyString());

    assertEquals(actual, expected);
  }

  @Test
  public void testVerifyPrincipleBySessionIdSessionNotFound()
      throws DaoException, ServiceException {
    // given
    Principal expected = new Principal(session, userAnonymous);
    // when
    when(mockUserDao.selectPrincipalBySessionId(anyString())).thenReturn(Optional.empty());
    when(mockUserDao.selectUserById(0)).thenReturn(Optional.of(userAnonymous));
    Principal actual = service.verifyPrincipleBySessionId(expected.getSession().getId());
    // then
    verify(mockUserDao, never()).updateSession(any(Session.class));
    verify(mockUserDao, times(1)).insertSession(any(Session.class));

    assertSame(actual.getUser().getRole(), Role.ANONYMOUS);
  }

  @Test
  public void testVerifyPrincipleBySessionIdThrowsException() throws DaoException {
    // when
    when(mockUserDao.selectPrincipalBySessionId(anyString())).thenThrow(DaoException.class);
    // then
    assertThrows(ServiceException.class, () -> service.verifyPrincipleBySessionId(session.getId()));
  }

  @Test
  public void testVerifyPrincipleByPassEmailAndPassMatch()
      throws DaoException, ServiceException, ClientServiceException {
    // given
    Principal previous = new Principal(session, userAnonymous);
    Principal expected = new Principal(session, userStudent);
    // when
    when(mockUserDao.selectUserByEmail(anyString())).thenReturn(Optional.of(userStudent));
    when(mockBCryptHashing.verify(anyString(), anyString())).thenReturn(true);
    Principal actual =
        service.verifyPrincipleByPass(previous, userStudent.getEmail(), userStudent.getPassword());
    // then
    verify(mockUserDao, times(1)).updateSession(any(Session.class));
    assertEquals(actual, expected);
  }

  @Test
  public void testVerifyPrincipleByPassWhenPassDoesntMatchThrowClientServiceException()
      throws DaoException {
    // given
    Principal previous = new Principal(session, userAnonymous);
    // when
    when(mockUserDao.selectUserByEmail(anyString())).thenReturn(Optional.of(userStudent));
    when(mockBCryptHashing.verify(anyString(), anyString())).thenReturn(false);
    // then
    assertThrows(
        ClientServiceException.class,
        () ->
            service.verifyPrincipleByPass(
                previous, userStudent.getEmail(), userStudent.getPassword()));
  }

  @Test
  public void testVerifyPrincipleByPassWhenUserDoesntExistThrowClientServiceException()
      throws DaoException {
    // given
    Principal previous = new Principal(session, userAnonymous);
    // when
    when(mockUserDao.selectUserByEmail(anyString())).thenReturn(Optional.empty());
    // then
    assertThrows(
        ClientServiceException.class,
        () ->
            service.verifyPrincipleByPass(
                previous, userStudent.getEmail(), userStudent.getPassword()));
  }

  @Test
  public void testVerifyPrincipleByPassWhenRegistrationExpiredThrowClientServiceException()
      throws DaoException, ServiceException {
    // given
    userStudent.setRegistrationExpDate(Instant.now().minusSeconds(100));
    Principal previous = new Principal(session, userAnonymous);
    // when
    when(mockUserDao.selectUserByEmail(anyString())).thenReturn(Optional.of(userStudent));
    when(mockBCryptHashing.verify(anyString(), anyString())).thenReturn(true);
    // then
    try {
      service.verifyPrincipleByPass(previous, userStudent.getEmail(), userStudent.getPassword());
    } catch (ClientServiceException e) {
      verify(mockUserDao, times(1)).deleteUser(userStudent.getId());
    }
    assertThrows(
        ClientServiceException.class,
        () ->
            service.verifyPrincipleByPass(
                previous, userStudent.getEmail(), userStudent.getPassword()));
  }

  @Test
  public void testLogoutSucceed() throws ServiceException, DaoException {
    // given
    Principal previous = new Principal(session, userStudent);
    Principal expected = new Principal(session, userAnonymous);
    // when
    when(mockUserDao.selectUserById(userAnonymous.getId())).thenReturn(Optional.of(userAnonymous));
    Principal actual = service.logout(previous);
    // then
    verify(mockUserDao, times(1)).updateSession(any(Session.class));
    assertEquals(actual, expected);
  }

  @Test
  public void testRegisterUserSucceed()
      throws ClientServiceException, ServiceException, DaoException, MessagingException {
    // given
    Principal previous = new Principal(session, userAnonymous);

    // when
    when(mockMailSender.sendMail(anyString(), anyString(), anyString())).thenReturn(true);
    when(mockBCryptHashing.toHash(anyString())).thenReturn(anyString());
    service.registerUser(
        previous,
        "",
        userStudent.getEmail(),
        userStudent.getPassword(),
        userStudent.getPassword(),
        userStudent.getFirstName(),
        userStudent.getLastName(),
        userStudent.getRole());
    // then
    verify(mockUserDao, times(1)).insertUser(any(User.class));
    verify(mockMailSender, times(1)).sendMail(anyString(), anyString(), anyString());
  }

  @Test
  public void testRegisterUserWhenEmailExistsThrowClientServiceException()
      throws DaoException, MessagingException {
    // given
    Principal previous = new Principal(session, userAnonymous);
    // when
    when(mockMailSender.sendMail(anyString(), anyString(), anyString())).thenReturn(true);
    when(mockBCryptHashing.toHash(anyString())).thenReturn(anyString());
    when(mockUserDao.isExistsEmail(userStudent.getEmail())).thenReturn(true);
    // then
    assertThrows(
        ClientServiceException.class,
        () ->
            service.registerUser(
                previous,
                "",
                userStudent.getEmail(),
                userStudent.getPassword(),
                userStudent.getPassword(),
                userStudent.getFirstName(),
                userStudent.getFirstName(),
                userStudent.getRole()));
  }

  @Test
  public void testActivateRegistrationSucceed()
      throws ClientServiceException, ServiceException, DaoException {
    // given
    userStudent.setRegistrationExpDate(Instant.now().plusSeconds(100));
    Principal previous = new Principal(session, userAnonymous);
    String code = "abc";
    // when
    when(mockUserDao.selectUserByCode(code)).thenReturn(Optional.of(userStudent));
    service.activateRegistration(previous, code);
    // then
    verify(mockUserDao, times(1)).updateUserRegistrationToNull(userStudent.getId());
  }

  @Test
  public void testActivateRegistrationWhenTimeExpiredThenThrowClientServiceException()
      throws DaoException {
    // given
    userStudent.setRegistrationExpDate(Instant.now().minusSeconds(100));
    Principal previous = new Principal(session, userAnonymous);
    String code = "abc";
    // when
    when(mockUserDao.selectUserByCode(code)).thenReturn(Optional.of(userStudent));
    // then
    assertThrows(ClientServiceException.class, () -> service.activateRegistration(previous, code));
    verify(mockUserDao, times(1)).deleteUser(userStudent.getId());
  }
}
