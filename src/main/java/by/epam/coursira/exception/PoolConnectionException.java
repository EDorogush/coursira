package by.epam.coursira.exception;

public class PoolConnectionException extends Exception {
  public PoolConnectionException(String message) {
    super(message);
  }

  public PoolConnectionException(Exception e) {
    super(e);
  }
}
