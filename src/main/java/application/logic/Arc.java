package application.logic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;
import java.util.Objects;

/**
 * Represents a directed arc within a Petri Net, connecting a Place to a Transition
 * or a Transition to a Place.
 * This class stores the arc's structural data and weight.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Arc {
    private String id;
    private String petriNetId;
    private String sourceId; // ID of a Place or Transition
    private String targetId; // ID of a Transition or Place
    private int weight; // Default 1

    /**
     * Default constructor required for deserialization (Jackson).
     */
    public Arc() {
        this.weight = 1;
    }

    /**
     * Constructs a new Arc, ensuring it adheres to the bipartite graph rules.
     * * @param petriNetId The ID of the parent Petri Net.
     * @param sourceId The ID of the source element (P or T).
     * @param targetId The ID of the target element (T or P).
     * @throws IllegalArgumentException if the connection is invalid (e.g., P->P or T->T).
     */
    public Arc(String petriNetId, String sourceId, String targetId) throws IllegalArgumentException {
        if (!isValidConnection(sourceId, targetId)) {
            throw new IllegalArgumentException("You cannot connect elements of the same type!");
        }

        this.id = "A" + UUID.randomUUID().toString();
        this.petriNetId = Objects.requireNonNull(petriNetId);
        this.sourceId = Objects.requireNonNull(sourceId);
        this.targetId = Objects.requireNonNull(targetId);
        this.weight = 1;
    }

    /**
     * Checks if the connection adheres to the Petri Net bipartite rule (P->T or T->P).
     */
    private boolean isValidConnection(String sourceId, String targetId) {
        return (sourceId.startsWith("P") && targetId.startsWith("T")) ||
                (sourceId.startsWith("T") && targetId.startsWith("P"));
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getPetriNetId() {
        return petriNetId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    /**
     * Checks if the source of the arc is a Place (starts with 'P').
     */
    public boolean isSourcePlace() {
        return sourceId != null && sourceId.startsWith("P");
    }

    /**
     * Checks if the source of the arc is a Transition (starts with 'T').
     */
    public boolean isSourceTransition() {
        return sourceId != null && sourceId.startsWith("T");
    }

    /**
     * Gets the weight of the arc (number of tokens required/produced).
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Sets the weight of the arc.
     * @param weight The new weight value.
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return String.format(
                "Arc[id=%s, petriNetId=%s, sourceId=%s, targetId=%s, weight=%d]",
                id, petriNetId, sourceId, targetId, weight
        );
    }
}