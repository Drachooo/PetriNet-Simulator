package application.exceptions;

public class UnauthorizedAccesException extends RuntimeException {
  public UnauthorizedAccesException(String message) {
    super(message);
  }
}
