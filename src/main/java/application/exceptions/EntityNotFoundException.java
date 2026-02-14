package application.exceptions;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException() {
        super("Could not find what you were looking for");
    }
    public EntityNotFoundException(String message) {
        super(message);
    }
}
