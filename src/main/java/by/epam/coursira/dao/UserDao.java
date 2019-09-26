package by.epam.coursira.dao;

import by.epam.coursira.entity.Language;
import by.epam.coursira.entity.Principal;
import by.epam.coursira.entity.Role;
import by.epam.coursira.entity.Session;
import by.epam.coursira.entity.User;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.PoolConnectionException;
import by.epam.coursira.pool.ConnectionPool;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class UserDao {
  private static final Logger logger = LogManager.getLogger();

  private static final String SQL_INSERT_SESSION =
    "INSERT INTO sessions(id, expire_date, user_id, language, zone_offset_of_total_seconds)"
      + " VALUES (?,?,?,?,?)";

  private static final String SQL_INSERT_USER =
    "INSERT INTO users(email, password, firstname, lastname, role, "
      + "registration_code,registration_expire_date)"
      + " VALUES (?,?,?,?,?,?,?)";

  private static final String SQL_SELECT_PRINCIPAL_BY_SESSION_ID =
    "SELECT users.id, users.email, users.password, users.firstname, users.lastname, users.role, "
      + "users.organization, users.age, users.interests, users.photo, users.registration_code, "
      + "users.registration_expire_date, "
      + "sessions.id AS session_id,  sessions.expire_date, sessions.language,"
      + "sessions.zone_offset_of_total_seconds FROM sessions "
      + "JOIN users ON sessions.user_id = users.id WHERE sessions.id = ?";

  private static final String SQL_SELECT_USER_BY_CODE =
    "SELECT id, email, password, firstname, lastname, role, "
      + "organization, age, interests, photo, registration_code, registration_expire_date "
      + "FROM users WHERE registration_code = ?";

  private static final String SQL_SELECT_USER_BY_ID =
    "SELECT id, email, password, firstname, lastname, role, "
      + "organization, age, interests, photo, registration_code, registration_expire_date "
      + "FROM users WHERE id = ?";

  private static final String SQL_SELECT_USER_BY_ROLE_WITH_NO_REGISTRATION_CODE =
    "SELECT id, email, password, firstname, lastname, role, "
      + "organization, age, interests, photo, registration_code, registration_expire_date "
      + "FROM users WHERE role = ? AND registration_code ISNULL ";

  private static final String SQL_SELECT_USER_BY_EMAIL =
    "SELECT id, email, password, firstname, lastname, role, "
      + "organization, age, interests, photo, registration_code, "
      + "registration_expire_date FROM users  "
      + "WHERE email = ?";

  private static final String SQL_UPDATE_SESSION =
    "UPDATE sessions SET expire_date = ?, user_id = ?, language = ?, "
      + "zone_offset_of_total_seconds = ? "
      + "WHERE id = ?";

  private static final String SQL_UPDATE_USER =
    "UPDATE users "
      + "SET password = ?, firstname = ?, lastname = ?,"
      + "organization = ?, age = ?, interests = ?"
      + "WHERE id = ?";

  private static final String SQL_UPDATE_USER_PHOTO =
    "UPDATE users " + "SET photo = ? " + "WHERE id = ?;";

  private static final String SQL_UPDATE_USER_REGISTRATION =
    "UPDATE users " + "SET registration_code = ?, registration_expire_date = ? " + "WHERE id = ?";

  private static final String SQL_DELETE_WASTE_SESSIONS =
    "DELETE FROM sessions WHERE expire_date < ?";

  private static final String SQL_DELETE_USER_WITH_REGISTRATION_CODE_EXPIRED =
    "DELETE FROM users WHERE registration_expire_date < ?";

  private static final String SQL_DELETE_USER = "DELETE FROM users WHERE id = ?";

  private static final String SQL_EXISTS_USER_EMAIL =
    "" + "SELECT EXISTS(SELECT 1 FROM users WHERE email = ?);";

  private static final String SQL_EXISTS_LECTURER_WITH_ID =
    "SELECT EXISTS(SELECT 1 FROM users WHERE id = ? AND role = 'LECTURER')";
  private final DataSource pool;

  public UserDao(DataSource pool) {
    this.pool = pool;
  }

  /**
   * Insert {@link Session} object to database. Important: session's Id value must be determined
   * before;
   *
   * @param session object which is going to be inserted to table.
   * @return number of affected rows
   * @throws DaoException when attempt fails
   */
  public int insertSession(Session session) throws DaoException {
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_INSERT_SESSION)) {
      ps.setString(1, session.getId());
      ps.setTimestamp(2, Timestamp.from(session.getExpDate()));
      ps.setInt(3, session.getUserId());
      ps.setString(4, session.getLanguage().toString());
      ps.setInt(5, session.getZoneOffset().getTotalSeconds());
      ps.executeUpdate();
      return ps.getUpdateCount();
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  /**
   * Insert {@link User} to database table. Returns generated user's Id.
   *
   * @param user Object which is going to be inserted in database table
   * @return generated by database user's id
   * @throws DaoException when attempt fails
   */
  public int insertUser(User user) throws DaoException {
    final int userId;
    try (Connection connection = pool.getConnection();
         PreparedStatement ps =
           connection.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, user.getEmail());
      ps.setString(2, user.getPassword());
      ps.setString(3, user.getFirstName());
      ps.setString(4, user.getLastName());
      ps.setString(5, user.getRole().toString());
      ps.setString(6, user.getRegistrationCode());
      ps.setTimestamp(7, Timestamp.from(user.getRegistrationExpDate()));
      ps.executeUpdate();
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          userId = generatedKeys.getInt(1);
          logger.debug("new user with id {} inserted", userId);
        } else {
          throw new SQLException("Creating user failed, no ID obtained.");
        }
      }
    } catch (SQLException e) {
      throw new DaoException(e);
    }
    return userId;
  }

  /**
   * Method selects from db one {@link User} record with registration_code specified by code
   * argument.
   *
   * @param code value of registration_code
   * @return {@link Optional}<{@link User}> object
   * @throws DaoException if attempt to select fails
   */
  public Optional<User> selectUserByCode(String code) throws DaoException {
    final User user;
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_SELECT_USER_BY_CODE)) {
      ps.setString(1, code);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }
        user = parseUserFromResultSet(rs);
      }
    } catch (SQLException e) {
      throw new DaoException(e);
    }
    return Optional.of(user);
  }

  /**
   * Method selects {@link Principal} from db with sessionId specified by argument
   *
   * @param sessionId value of session Id according to which {@link Principal} object is going to be
   *     found in db
   * @return {@link Optional}<{@link Principal}> object
   * @throws DaoException when getting data fails
   */
  public Optional<Principal> selectPrincipalBySessionId(String sessionId) throws DaoException {
    final Principal principal;
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_SELECT_PRINCIPAL_BY_SESSION_ID)) {
      ps.setString(1, sessionId);
      // get one line or none
      User user;
      Session session;
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }
        user = parseUserFromResultSet(rs);
        session =
          new Session.Builder()
            .setId(rs.getString("session_id"))
            .setUserId(rs.getInt("id"))
            .setExpDate(rs.getTimestamp("expire_date").toInstant())
            .setLanguage(Language.valueOf(rs.getString("language")))
            .setZoneOffSet(ZoneOffset.ofTotalSeconds(rs.getInt("zone_offset_of_total_seconds")))
            .build();
      }
      principal = new Principal(session, user);
    } catch (SQLException e) {
      throw new DaoException(e);
    }
    return Optional.of(principal);
  }

  /**
   * Method selects {@link User} from db with email specified by argument
   *
   * @param email value of email field according to which {@link User} object is going to be found
   *     in db
   * @return {@link Optional}<{@link User}> object
   * @throws DaoException if attempt to select fails
   */
  public Optional<User> selectUserByEmail(String email) throws DaoException {
    final User user;
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_SELECT_USER_BY_EMAIL)) {
      ps.setString(1, email);
      // get one line or none
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        } else {
          user = parseUserFromResultSet(rs);
        }
      }
    } catch (SQLException e) {
      throw new DaoException(e);
    }
    return Optional.of(user);
  }

  /**
   * Method selects {@link User} from db with userId specified by argument
   *
   * @param id value of userId field according to which {@link User} object is going to be found in
   *     db
   * @return {@link Optional}<{@link User}> object
   * @throws DaoException if attempt to select fails
   */
  public Optional<User> selectUserById(int id) throws DaoException {
    final User user;
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_SELECT_USER_BY_ID)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        } else {
          user = parseUserFromResultSet(rs);
        }
      }
    } catch (SQLException e) {
      throw new DaoException(e);
    }
    return Optional.of(user);
  }

  /**
   * Method selects {@link User} from db with {@link Role} specified by argument and registration
   * Code = null
   *
   * @param role value of {@link Role} according to which {@link User} object is going to be found
   *     in db
   * @return {@link Optional}<{@link User}> object
   * @throws DaoException if attempt to select fails
   */
  public List<User> selectUsersByRole(Role role) throws DaoException {
    List<User> users = new ArrayList<>();
    try (Connection connection = pool.getConnection();
         PreparedStatement ps =
           connection.prepareStatement(SQL_SELECT_USER_BY_ROLE_WITH_NO_REGISTRATION_CODE)) {
      ps.setString(1, role.toString());
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          User user = parseUserFromResultSet(rs);
          users.add(user);
        }
      }

    } catch (SQLException e) {
      throw new DaoException(e);
    }
    return users;
  }

  /**
   * Method updates one record in db with id = session.getId. returns the number of affected raws
   * (must be 1).
   *
   * @param session - new values of session data.
   * @return the number of affected raws (must be 1)
   * @throws DaoException if attempt to update fails
   */
  public int updateSession(Session session) throws DaoException {
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_SESSION)) {
      ps.setTimestamp(1, Timestamp.from(session.getExpDate()));
      ps.setInt(2, session.getUserId());
      ps.setString(3, session.getLanguage().toString());
      ps.setInt(4, session.getZoneOffset().getTotalSeconds());
      ps.setString(5, session.getId());
      ps.executeUpdate();
      return ps.getUpdateCount();
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  /**
   * updates password,firstName, lastName, organization, age, interests fields of current user.
   *
   * @param current - values that are going to be inserted in database instead of previous data
   * @return the number of affected raws (must be 1)
   * @throws DaoException if attempt to update fails
   */
  public int updateUser(User current) throws DaoException {
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_USER)) {
      ps.setString(1, current.getPassword());
      ps.setString(2, current.getFirstName());
      ps.setString(3, current.getLastName());
      ps.setString(4, current.getOrganization());
      ps.setInt(5, current.getAge());
      ps.setString(6, current.getInterests());
      ps.setInt(7, current.getId());
      ps.executeUpdate();
      return ps.getUpdateCount();
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  /**
   * Method updates user's registration_code and registration_ext_date to null.
   *
   * @param userId id of current user.
   * @return the number of affected raws (must be 1)
   * @throws DaoException if attempt to update fails
   */
  public int updateUserRegistrationToNull(int userId) throws DaoException {
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_USER_REGISTRATION)) {
      ps.setNull(1, Types.INTEGER);
      ps.setNull(2, Types.TIMESTAMP);
      ps.setInt(3, userId);
      ps.executeUpdate();
      return ps.getUpdateCount();
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  /**
   * Method updates user's photo.
   *
   * @param userId id of current user
   * @param image {@link InputStream} object
   * @return the number of affected raws (must be 1)
   * @throws DaoException if attempt to update fails
   */
  public int updateUserPhoto(int userId, InputStream image) throws DaoException {
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_USER_PHOTO)) {
      ps.setInt(2, userId);
      ps.setBinaryStream(1, image);
      ps.executeUpdate();
      return ps.getUpdateCount();
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  /**
   * This methods invokes deleting {@link User} data with current id from db. It is not allowed to
   * delete user with id = 0; actual number of deleted raws returned;
   *
   * @param id current user's id.
   * @return the number of deleted raws.
   * @throws DaoException when attempt to delete fails
   */
  public int deleteUser(int id) throws DaoException {
    // can't delete default user
    if (id == 0) {
      throw new DaoException("You are not allowed to delete zeros user");
    }
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_DELETE_USER)) {
      ps.setInt(1, id);
      ps.executeUpdate();
      return ps.getUpdateCount();
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  /**
   * Method invokes deleting of session records which session's expire_date is before timePoint.
   *
   * @param timePoint - {@link Instant} value which determines what records are going to be deleted.
   * @return the number of affected raws
   * @throws DaoException if attempt to delete fails
   */
  public int deleteSession(Instant timePoint) throws DaoException {
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_DELETE_WASTE_SESSIONS)) {
      ps.setTimestamp(1, Timestamp.from(timePoint));
      ps.executeUpdate();
      return ps.getUpdateCount();
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  /**
   * Method invokes deleting of user records which registration_expire_date is before timePoint.
   *
   * @param timePoint - {@link Instant} value which determines what records are going to be deleted.
   * @return the number of affected raws
   * @throws DaoException if attempt to delete fails
   */
  public int deleteUserWithRegistrationCodeExpired(Instant timePoint) throws DaoException {
    try (Connection connection = pool.getConnection();
         PreparedStatement ps =
           connection.prepareStatement(SQL_DELETE_USER_WITH_REGISTRATION_CODE_EXPIRED)) {
      ps.setTimestamp(1, Timestamp.from(timePoint));
      ps.executeUpdate();
      return ps.getUpdateCount();
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  /**
   * Method checks if user with specified email exists in db
   *
   * @param email {@link String} value of email field
   * @return {@code true} if user exists, otherwise returns {@code false}
   * @throws DaoException if attempt to delete fails
   */
  public boolean isExistsEmail(String email) throws DaoException {
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_EXISTS_USER_EMAIL)) {
      ps.setString(1, email);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() && rs.getBoolean("exists");
      }
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  /**
   * Method checks if lecturer with specified id exists in db
   *
   * @param id value of user's id
   * @return {@code true} if lecturer exists, otherwise returns {@code false}
   * @throws DaoException if attempt to delete fails
   */
  public boolean isExistsLecturer(int id) throws DaoException {
    try (Connection connection = pool.getConnection();
         PreparedStatement ps = connection.prepareStatement(SQL_EXISTS_LECTURER_WITH_ID)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() && rs.getBoolean("exists");
      }
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  private User parseUserFromResultSet(ResultSet rs) throws SQLException {
    String base64Image =
      Optional.ofNullable(rs.getBytes("photo"))
        .map(t -> Base64.getEncoder().encodeToString(t))
        .orElse(null);
    Instant registrationExpDate =
      Optional.ofNullable(rs.getTimestamp("registration_expire_date"))
        .map(Timestamp::toInstant)
        .orElse(null);
    return new User.Builder()
      .setId(rs.getInt("id"))
      .setEmail(rs.getString("email"))
      .setPassword(rs.getString("password"))
      .setFirstName(rs.getString("firstname"))
      .setLastName(rs.getString("lastname"))
      .setRole(Role.valueOf(rs.getString("role")))
      .setOrganization(rs.getString("organization"))
      .setAge(rs.getInt("age"))
      .setInterests(rs.getString("interests"))
      .setRegistrationCode(rs.getString("registration_code"))
      .setRegistrationExpDate(registrationExpDate)
      .setBase64Image(base64Image)
      .build();
  }
}
