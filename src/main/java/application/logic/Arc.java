package application.logic;

import java.util.UUID;
import java.util.Objects;

public class Arc {
    private final String id;
    private final String petriNetId;
    private final String sourceId; // ID di un Place o Transition
    private final String targetId; // ID di un Transition o Place
    private final int weight; // Default 1

    public Arc(String petriNetId, String sourceId, String targetId) {

        if (!isValidConnection(sourceId, targetId)) {
            throw new IllegalArgumentException("You cannot connect elements of the same type!");
        }

        this.id ="A"+UUID.randomUUID().toString();
        this.petriNetId = Objects.requireNonNull(petriNetId);
        this.sourceId = Objects.requireNonNull(sourceId);
        this.targetId = Objects.requireNonNull(targetId);
        this.weight = 1;
    }

    public String getId() {
        return this.id;
    }

    public String getPetriNetId() {
        return this.petriNetId;
    }

    public String getSourceId() {
        return this.sourceId;
    }

    public String getTargetId() {
        return this.targetId;
    }

    public int getWeight() {
        return this.weight;
    }

    private boolean isValidConnection(String sourceId, String targetId) {
        // Deve essere Place→Transition OPPURE Transition→Place
        return (sourceId.startsWith("P") && targetId.startsWith("T")) ||
                (sourceId.startsWith("T") && targetId.startsWith("P"));
    }

    @Override
    public String toString() {
        return String.format(
                "Arc[id=%s, petriNetId=%s, sourceId=%s, targetId=%s, weight=%d]",
                id, petriNetId, sourceId, targetId, weight
        );
    }

}