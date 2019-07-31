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

public class ConnectionPoolImpl implements ConnectionPool, AutoCloseable { // not Closeable{
  private static final Logger logger = LogManager.getLogger();
  private final BlockingQueue<Connection> pool;
  private final Set<Connection> fullSet;

  public ConnectionPoolImpl(int size, String url) throws PoolConnectionException {
    pool = new ArrayBlockingQueue<>(size);
    fullSet = new HashSet<>(size);
    for (int i = 0; i < size; i++) {
      Connection connection = null;
      try {
        connection = DriverManager.getConnection(url);
      } catch (SQLException e) {
        logger.info("SQLException in DriverManager.getConnection(url) ");
        throw new PoolConnectionException(e);
      }
      pool.offer(connection);
      fullSet.add(connection);
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

  public boolean releaseConnection(WrappedConnection connection) {
    // pool is guarantied have free space
    pool.offer(connection.getRealConnection());
    logger.debug("Connection released. Pool size is {}", pool.size());
    return true;
  }

  @Override
  public void close() throws PoolConnectionException {
    // todo: implement gracefull shutdown
    for (Connection connection : fullSet) {
      try {
        connection.close();
      } catch (SQLException e) {
        logger.info("SQLException in attempt to close connection");
        throw new PoolConnectionException(e);
      }
    }
    logger.debug("successfully closed all connections");
  }
}
