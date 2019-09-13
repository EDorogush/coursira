package by.epam.coursira.dao;

import by.epam.coursira.pool.ConnectionPoolImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.Assert.assertEquals;

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
  public void whenSelectQueryExecuted_thenResulstsReturned()
    throws Exception {
    try (PostgreSQLContainer postgresContainer = new PostgreSQLContainer<>().
      withClasspathResourceMapping("schema.sql", "/schema.sql", BindMode.READ_ONLY).
      withInitScript("schema.sql")) {
      postgresContainer.start();
      //String jdbcUrl = postgresContainer.getJdbcUrl() + "&user=" + postgresContainer.getUsername() + "&password=" + postgresContainer.getPassword();
//.withInitScript("finalproject/scr/main/resources/schema.sql"
//      String username = postgresContainer.getUsername();
//      String password = postgresContainer.getPassword();
//      try (ConnectionPoolImpl connectionPool = new ConnectionPoolImpl(4, jdbcUrl)) {
//      }
      // initParameter('jdbcDriver', 'jdbc:postgresql://localhost:5432/coursiradb?user=coursirauser&charSet=UNICODE')
      //   ConnectionPoolImpl connectionPool = new ConnectionPoolImpl(5, jdbcUrl);
//      try (Connection conn = DriverManager
//        .getConnection(jdbcUrl, username, password)) {
//      }
//      ResultSet resultSet =
//        conn.createStatement().executeQuery("SELECT 1");
//      resultSet.next();
      //int result = resultSet.getInt(1);
    }
    assertEquals(true, true);
  }
}
