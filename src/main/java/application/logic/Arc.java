package application.logic;

import java.util.UUID;
import java.util.Objects;

public class Arc {
    private String id;
    private String petriNetId;
    private String sourceId; // ID di un Place o Transition
    private String targetId; // ID di un Transition o Place
    private int weight; // Default 1

    // Costruttore vuoto per Jackson
    public Arc() {
        this.weight = 1; // Default anche quando deserializzi
    }

    // Costruttore normale
    public Arc(String petriNetId, String sourceId, String targetId) {
        if (!isValidConnection(sourceId, targetId)) {
            throw new IllegalArgumentException("You cannot connect elements of the same type!");
        }

        this.id = "A" + UUID.randomUUID().toString();
        this.petriNetId = Objects.requireNonNull(petriNetId);
        this.sourceId = Objects.requireNonNull(sourceId);
        this.targetId = Objects.requireNonNull(targetId);
        this.weight = 1;
    }

    private boolean isValidConnection(String sourceId, String targetId) {
        return (sourceId.startsWith("P") && targetId.startsWith("T")) ||
                (sourceId.startsWith("T") && targetId.startsWith("P"));
    }

    // Getters e setters (necessari per Jackson)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPetriNetId() {
        return petriNetId;
    }

    public void setPetriNetId(String petriNetId) {
        this.petriNetId = petriNetId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public int getWeight() {
        return weight;
    }

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
