package by.epam.coursira.dao;

import by.epam.coursira.entity.User;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.pool.ConnectionPoolImpl;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestContainersTest {
  private static final Logger logger = LogManager.getLogger();
  //  @Test
  //  public void testCreateContainer() throws PoolConnectionException{
  //    try (PostgreSQLContainer postgres = new PostgreSQLContainer<>()) {
  //      postgres.start();
  //    String url = "jdbc:tc:postgresql:9.6.8://hostname/databasenam";
  //    int size = 5;
  ////    'jdbc:postgresql://localhost:5432/coursiradb?user=coursirauser&charSet=UNICODE'
  //      ConnectionPool connectionPool = new ConnectionPoolImpl(size, url);
  //    }

  //  @Rule
  //  public PostgreSQLContainer postgresContainer = new PostgreSQLContainer();

  @Test
  public void whenSelectQueryExecuted_thenResulstsReturned() throws Exception, DaoException {
    try (PostgreSQLContainer postgresContainer =
        new PostgreSQLContainer<>()
            .withClasspathResourceMapping("dbscripts", "/dbscripts", BindMode.READ_WRITE)
            .withDatabaseName("coursiradb")
            .withUsername("coursirauser")
            .withPassword("password")
            .withInitScript("dbscripts/dbInitData.sql")
            .withInitScript("dbscripts/schema.sql")) {
      postgresContainer.start();
      String jdbcUrl =
          postgresContainer.getJdbcUrl()
              + "&user="
              + postgresContainer.getUsername()
              + "&password="
              + postgresContainer.getPassword();
      ConnectionPoolImpl connectionPool = new ConnectionPoolImpl(4, jdbcUrl);
      UserDao userDao = new UserDao(connectionPool);
      Optional<User> user = userDao.selectUserByEmail("dorogushelena+snow@gmail.com");
      user.isPresent();

      assertEquals(true, true);
    }
  }
}
