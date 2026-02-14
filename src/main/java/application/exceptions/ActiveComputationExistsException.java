package application.exceptions;

public class ActiveComputationExistsException extends RuntimeException {
    public ActiveComputationExistsException() {super("User already has an active computation for this net");}
    public ActiveComputationExistsException(String message) {
        super(message);
    }
}
