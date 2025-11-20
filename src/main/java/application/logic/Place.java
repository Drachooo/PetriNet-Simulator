package application.logic;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Place node within a Petri Net.
 * This class holds the structural definition (ID, Name) of a place.
 */
public class Place {
    private String id;
    private String petriNetId;
    private String name;

    /**
     * Default constructor required for deserialization (Jackson).
     */
    public Place() {}

    /**
     * Constructs a new Place element.
     * @param petriNetId The ID of the parent Petri Net.
     * @param name The descriptive name of the place.
     */
    public Place(String petriNetId, String name) {
        this.id = "P" + UUID.randomUUID().toString();
        this.name = Objects.requireNonNull(name);
        this.petriNetId = Objects.requireNonNull(petriNetId);
    }

    /**
     * Gets the descriptive name of the place.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the unique ID of the place.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the ID of the Petri Net this place belongs to.
     */
    public String getPetriNetId() {
        return petriNetId;
    }

    @Override
    public String toString() {
        return "P-" + name;
    }
}