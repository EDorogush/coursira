package by.epam.coursira.exception;

public class ClientCommandException extends Exception {
  public ClientCommandException(Exception cause) {
    super(cause);
  }

  public ClientCommandException(String message) {
    super(message);
  }
}
