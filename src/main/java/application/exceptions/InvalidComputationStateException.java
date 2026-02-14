package application.exceptions;

public class InvalidComputationStateException extends RuntimeException {
    public InvalidComputationStateException() {super("Computation is not active");}
    public InvalidComputationStateException(String message) {
        super(message);
    }
}
