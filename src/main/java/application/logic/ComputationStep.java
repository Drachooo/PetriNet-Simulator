package application.logic;

import java.util.UUID;
import java.util.Objects;
import java.time.LocalDateTime;

// Imports needed for Jackson
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Represents a single step (a snapshot) in the history of a Computation.
 * Maps to data model 5.2.6.
 */
public class ComputationStep {
    private final String id;
    private final String computationId;
    private final String transitionId; // null for initial step

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private final LocalDateTime timeStamp;

    private final MarkingData markingData;

    /**
     * Constructor for creating a new step.
     * Automatically generates ID and timestamp.
     * @param computationId The computation this step belongs to.
     * @param transitionId The transition that just fired (null for initial step).
     * @param markingData The resulting marking (state).
     */
    public ComputationStep(String computationId, String transitionId, MarkingData markingData) {
        this.id = "S" + UUID.randomUUID().toString(); // "S" for Step
        this.computationId = Objects.requireNonNull(computationId);
        this.transitionId = transitionId;
        this.markingData = Objects.requireNonNull(markingData);
        this.timeStamp = LocalDateTime.now();
    }

    /**
     * Constructor for Jackson deserialization.
     * @JsonCreator tells Jackson to use this constructor.
     * @JsonProperty maps JSON fields to parameters.
     */
    @JsonCreator
    public ComputationStep(
            @JsonProperty("id") String id,
            @JsonProperty("computationId") String computationId,
            @JsonProperty("transitionId") String transitionId,
            @JsonProperty("timeStamp") LocalDateTime timeStamp,
            @JsonProperty("markingData") MarkingData markingData)
    {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.computationId = Objects.requireNonNull(computationId, "Computation ID cannot be null");
        this.transitionId = transitionId; // Can be null
        this.timeStamp = Objects.requireNonNull(timeStamp, "Timestamp cannot be null");
        this.markingData = Objects.requireNonNull(markingData, "MarkingData cannot be null");
    }

    // --- Getters ---

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