package by.epam.coursira.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import by.epam.coursira.entity.Language;
import by.epam.coursira.entity.Role;
import by.epam.coursira.entity.Session;
import by.epam.coursira.entity.User;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.PoolConnectionException;
import by.epam.coursira.pool.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserDaoTest {

  @Mock private ConnectionPool mockPool;

  @Mock private Connection mockConnection;
  @Mock private PreparedStatement mockPreparedStmt;
  @Mock private ResultSet mockResultSet;

  private UserDao userDao;
  private User user;
  private Session session;

  @BeforeMethod
  public void setUp() throws SQLException, PoolConnectionException {
    session =
        new Session.Builder()
            .setExpDate(Instant.now())
            .setId("abc")
            .setUserId(1)
            .setLanguage(Language.EN)
            .setZoneOffSet(ZoneOffset.UTC)
            .build();

    user =
        new User.Builder()
            .setId(1)
            .setEmail("email")
            .setFirstName("firstName")
            .setLastName("lastName")
            .setPassword("password")
            .setRegistrationExpDate(Instant.now())
            .setRegistrationCode("code")
            .setOrganization("organization")
            .setInterests("interests")
            .setAge(100)
            .setRole(Role.ANONYMOUS)
            .setBase64Image(null)
            .build();

    MockitoAnnotations.initMocks(this);
    assertNotNull(mockPool);
    when(mockPool.getConnection()).thenReturn(mockConnection);
    doNothing().when(mockConnection).commit();
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStmt);
    //    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStmt);
    doNothing().when(mockPreparedStmt).setString(anyInt(), anyString());
    doNothing().when(mockPreparedStmt).setTimestamp(anyInt(), any(Timestamp.class));
    when(mockPreparedStmt.executeUpdate()).thenReturn(1);

    when(mockPreparedStmt.getUpdateCount()).thenReturn(1);
    when(mockPreparedStmt.executeQuery()).thenReturn(mockResultSet);
    when(mockPreparedStmt.getGeneratedKeys()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.FALSE);

    when(mockResultSet.getString("email")).thenReturn(user.getEmail());
    when(mockResultSet.getString("password")).thenReturn(user.getPassword());
    when(mockResultSet.getString("firstname")).thenReturn(user.getFirstName());
    when(mockResultSet.getString("lastname")).thenReturn(user.getLastName());
    when(mockResultSet.getString("organization")).thenReturn(user.getOrganization());
    when(mockResultSet.getString("interests")).thenReturn(user.getInterests());
    when(mockResultSet.getString("role")).thenReturn(user.getRole().toString());
    when(mockResultSet.getString("registration_code")).thenReturn(user.getRegistrationCode());
    when(mockResultSet.getInt("age")).thenReturn(user.getAge());
    when(mockResultSet.getInt("id")).thenReturn(user.getId());
    when(mockResultSet.getTimestamp("registration_expire_date"))
        .thenReturn(Timestamp.from(user.getRegistrationExpDate()));
    when(mockResultSet.getBytes("photo")).thenReturn(null);

    userDao = new UserDao(mockPool);
  }

  @AfterMethod
  public void tearDown() throws SQLException {}

  @Test
  public void testInsertSessionSucceed()
      throws DaoException, PoolConnectionException, SQLException {
    int expected = 1;
    int actual = userDao.insertSession(session);

    verify(mockPool, times(1)).getConnection();
    verify(mockConnection, times(1)).prepareStatement(anyString());
    verify(mockPreparedStmt, times(2)).setString(anyInt(), anyString());
    verify(mockPreparedStmt, times(2)).setInt(anyInt(), anyInt());
    verify(mockPreparedStmt, times(1)).setTimestamp(anyInt(), any(Timestamp.class));
    verify(mockPreparedStmt, times(1)).executeUpdate();

    assertEquals(actual, expected);

  }

  @Test
  public void testInsertUserSucceed() throws DaoException, PoolConnectionException, SQLException {
    int expected = 1;
    when(mockResultSet.getInt(1)).thenReturn(expected);
    int actual = userDao.insertSession(session);

    verify(mockPool, times(1)).getConnection();
    verify(mockConnection, times(1)).prepareStatement(anyString());
    verify(mockPreparedStmt, times(2)).setString(anyInt(), anyString());
    verify(mockPreparedStmt, times(2)).setInt(anyInt(), anyInt());
    verify(mockPreparedStmt, times(1)).setTimestamp(anyInt(), any(Timestamp.class));
    verify(mockPreparedStmt, times(1)).executeUpdate();

    assertEquals(actual, expected);
  }

  @Test
  public void testInsertSessionWithException() throws SQLException {
    when(mockPreparedStmt.executeUpdate()).thenThrow(new SQLException());
    UserDao userDao = new UserDao(mockPool);

    assertThrows(DaoException.class, () -> userDao.insertSession(session));
  }

  @Test
  public void testDeleteSessionSucceed()
      throws DaoException, PoolConnectionException, SQLException {
    int expected = 1;
    when(mockResultSet.getInt(1)).thenReturn(expected);
    int actual = userDao.deleteSession(Instant.now());

    verify(mockPool, times(1)).getConnection();
    verify(mockConnection, times(1)).prepareStatement(anyString());
    verify(mockPreparedStmt, times(0)).setString(anyInt(), anyString());
    verify(mockPreparedStmt, times(1)).setTimestamp(anyInt(), any(Timestamp.class));
    verify(mockPreparedStmt, times(1)).executeUpdate();

    assertEquals(actual, expected);
  }

  @Test
  public void testUpdateUserSucceed() throws DaoException, SQLException {
    int expected = 1;
    when(mockResultSet.getInt(1)).thenReturn(expected);
    int actual = userDao.updateSession(session);
    assertEquals(actual, expected);
  }

  @Test
  public void testSelectUserByIdSucceed() throws SQLException, DaoException {
    User expected = user;
    User actual = userDao.selectUserById(1).orElseThrow(() -> new DaoException("no User in test"));

    verify(mockResultSet, times(8)).getString(anyString());
    verify(mockResultSet, times(1)).getTimestamp(anyString());
    verify(mockResultSet, times(1)).getBytes(anyString());
    verify(mockResultSet, times(2)).getInt(anyString());
    assertEquals(actual, expected);
  }

  @Test
  public void testSelectUserByIdReturnEmpty() throws SQLException, DaoException {
    when(mockResultSet.next()).thenReturn(false);

    Optional<User> actual = userDao.selectUserById(1);
    assertTrue(actual.isEmpty());
  }
}
