package application.logic;

public class TransitionNotEnabledException extends RuntimeException {
    public TransitionNotEnabledException() {
        super("Transizione non abilitata: token insufficienti nei posti di input");
    }
    public TransitionNotEnabledException(String message) {
        super(message);
    }
}