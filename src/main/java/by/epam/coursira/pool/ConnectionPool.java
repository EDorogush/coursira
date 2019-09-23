package by.epam.coursira.pool;

import by.epam.coursira.exception.PoolConnectionException;
import java.sql.Connection;
import javax.sql.DataSource;

public interface ConnectionPool  {
  Connection getConnection() throws PoolConnectionException;

}
