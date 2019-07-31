package by.epam.coursira.exception;

public class ClientServiceException extends Exception {

  public ClientServiceException(String message) {
    super(message);
  }

  public ClientServiceException(Exception cause) {
    super(cause);
  }
}
