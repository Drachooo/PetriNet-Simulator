package application.exceptions;

/**
 * Thrown when required system services or user context are missing.
 */
public class SystemContextException extends RuntimeException {
    public SystemContextException(String message) {
        super(message);
    }
}