package by.epam.coursira.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
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
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import javax.servlet.http.Part;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserServiceTest {

  @Mock UserDao mockUserDao;

  private User userAnonymous =
      new User.Builder()
          .setId(0)
          .setEmail("email")
          .setFirstName("firstName")
          .setLastName("lastName")
          .setPassword("password")
          .setRole(Role.ANONYMOUS)
          .build();

  private Session session =
      new Session.Builder()
          .setExpDate(Instant.now())
          .setId("abc")
          .setUserId(1)
          .setLanguage(Language.EN)
          .setZoneOffSet(ZoneOffset.UTC)
          .build();

  private User userStudent;
  private UserService userService;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    userService = new UserService(mockUserDao);

    userStudent =
        new User.Builder()
            .setId(1)
            .setEmail("email")
            .setFirstName("firstName")
            .setLastName("lastName")
            .setRole(Role.STUDENT)
            .build();
  }

  @AfterMethod
  public void tearDown() {}

  @Test
  public void testUpdateUserPhotoSucceed()
      throws ClientServiceException, ServiceException, DaoException, IOException {
    // given
    Principal current = new Principal(session, userStudent);
    Part mockPhoto = mock(Part.class);
    InputStream mockInputStream = mock(InputStream.class);
    when(mockPhoto.getInputStream()).thenReturn(mockInputStream);
    doNothing().when(mockInputStream).close();
    // when
    when(mockPhoto.getContentType()).thenReturn("image/png");
    when(mockPhoto.getSize()).thenReturn(100L);

    when(mockUserDao.selectPrincipalBySessionId(session.getId())).thenReturn(Optional.of(current));
    userService.updateUserPhoto(current, mockPhoto);
    // then
    verify(mockUserDao, times(1)).updateUserPhoto(userStudent.getId(), mockInputStream);
    verify(mockUserDao, times(1)).selectPrincipalBySessionId(session.getId());
    verify(mockPhoto, times(1)).getInputStream();
  }

  @Test
  public void testUpdateUserPhotoWhenWrongPhotoTypeThrowClientServiceException() {
    // given
    Principal current = new Principal(session, userStudent);
    Part mockPhoto = mock(Part.class);
    // when
    when(mockPhoto.getContentType()).thenReturn("txt");
    assertThrows(
        ClientServiceException.class, () -> userService.updateUserPhoto(current, mockPhoto));
  }

  @Test
  public void testUpdateUserPhotoWhenWrongRoleThrowServiceException() {
    // given
    Principal current = new Principal(session, userAnonymous);
    Part mockPhoto = mock(Part.class);
    // when
    assertThrows(ServiceException.class, () -> userService.updateUserPhoto(current, mockPhoto));
  }

  @Test
  public void testUpdateUserDataSucceed() throws ClientServiceException, ServiceException {
    // given
    Principal current = new Principal(session, userStudent);
    User updated =
        new User.Builder()
            .setId(userStudent.getId())
            .setEmail(userStudent.getEmail())
            .setRole(userStudent.getRole())
            .setFirstName("uFname")
            .setLastName("uLname")
            .setAge(30)
            .setOrganization("uOrg")
            .setInterests("uInter")
            .build();
    Principal expected = new Principal(session, updated);
    // when
    Principal actual =
        userService.updateUserData(
            current,
            updated.getFirstName(),
            updated.getLastName(),
            updated.getAge(),
            updated.getOrganization(),
            updated.getInterests());

    assertEquals(actual, expected);
  }

  @Test
  public void testUpdateUserDataWhenWrongRoleThrowServiceException() {
    // given
    Principal current = new Principal(session, userAnonymous);
    User updated =
        new User.Builder()
            .setId(userStudent.getId())
            .setEmail(userStudent.getEmail())
            .setRole(userStudent.getRole())
            .setFirstName("uFname")
            .setLastName("uLname")
            .setAge(30)
            .setOrganization("uOrg")
            .setInterests("uInter")
            .build();
    Principal expected = new Principal(session, updated);
    // when
    assertThrows(
        ServiceException.class,
        () ->
            userService.updateUserData(
                current,
                updated.getFirstName(),
                updated.getLastName(),
                updated.getAge(),
                updated.getOrganization(),
                updated.getInterests()));
  }
}
