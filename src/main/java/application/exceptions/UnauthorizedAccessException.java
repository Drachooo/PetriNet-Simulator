package application.exceptions;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException() {
        super("You do not have permission to perform this action");
    }
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}