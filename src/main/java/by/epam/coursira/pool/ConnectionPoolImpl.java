package by.epam.coursira.pool;

import by.epam.coursira.exception.PoolConnectionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionPoolImpl implements ConnectionPool, AutoCloseable {
  private static final Logger logger = LogManager.getLogger();
  private final BlockingQueue<Connection> pool;
  private final Set<Connection> fullSet;

  public ConnectionPoolImpl(int size, String url) throws PoolConnectionException {
    pool = new ArrayBlockingQueue<>(size);
    fullSet = new HashSet<>(size);
    for (int i = 0; i < size; i++) {
      try {
        Connection connection = DriverManager.getConnection(url);
        if (!pool.offer(connection)) {
          // should not happen. Pool is expected to have free space
          throw new PoolConnectionException(
              "Can't init Connection pool. Can't add Connection to Pool");
        }
        fullSet.add(connection);
      } catch (SQLException e) {
        throw new PoolConnectionException(e);
      }
    }
  }

  @Override
  public Connection getConnection() throws PoolConnectionException {
    final Connection connection;
    try {
      connection = pool.take();
    } catch (InterruptedException e) {
      logger.info("Attempt to take connection from pool was interrupted");
      Thread.currentThread().interrupt();
      throw new PoolConnectionException(e);
    }
    logger.debug("Connection acquired. Pool size is {}", pool.size());
    return new WrappedConnection(connection, this);
  }
  // package-private
  boolean releaseConnection(WrappedConnection connection) throws PoolConnectionException {

    if (!pool.offer(connection.getRealConnection())) {
      // pool is guarantied have free space
      throw new PoolConnectionException("Can't return connection to pool");
    }
    logger.debug("Connection released. Pool size is {}", pool.size());
    return true;
  }

  @Override
  public void close() throws PoolConnectionException {
    for (Connection connection : fullSet) {
      try {
        connection.close();
      } catch (SQLException e) {
        logger.info("SQLException in attempt to close connection");
        throw new PoolConnectionException(e);
      }
    }
    logger.info("successfully closed all connections");
  }
}
