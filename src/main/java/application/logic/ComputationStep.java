package application.logic;

import java.util.UUID;
import java.util.Objects;
import java.time.LocalDateTime;

public class ComputationStep {
    private final String id;
    private final String computationId;
    private final String transitionId; // null per iniziale
    private final LocalDateTime timeStamp;
    private final MarkingData markingData;

    public ComputationStep(String computationId, String transitionId, LocalDateTime timeStamp, MarkingData markingData) {
        Objects.requireNonNull(computationId, "Computation ID cannot be null");
        Objects.requireNonNull(timeStamp, "Timestamp cannot be null");
        Objects.requireNonNull(markingData, "MarkingData cannot be null");

        this.id = "S" + UUID.randomUUID().toString();
        this.computationId = computationId;
        this.transitionId = transitionId; // Può essere null
        this.timeStamp = timeStamp;
        this.markingData = markingData;
    }

    // Getter
    public String getId() { return id; }
    public String getComputationId() { return computationId; }
    public String getTransitionId() { return transitionId; }
    public LocalDateTime getTimeStamp() { return timeStamp; }
    public MarkingData getMarkingData() { return markingData; }

    @Override
    public String toString() {
        return String.format(
                "ComputationStep[id=%s, computationId=%s, transitionId=%s, timeStamp=%s, markingData=%s]",
                id, computationId, transitionId, timeStamp, markingData
        );
    }
}