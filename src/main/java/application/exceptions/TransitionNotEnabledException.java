package application.exceptions;

public class TransitionNotEnabledException extends RuntimeException {
    public TransitionNotEnabledException() {
        super("Transition not enabled: insufficient token/s in input Place");
    }
    public TransitionNotEnabledException(String message) {
        super(message);
    }
}