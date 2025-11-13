package application.logic;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents the **definition (blueprint)** of a transition in a Petri net.
 * This class is a POJO (Plain Old Java Object) and does not contain
 * any execution logic or state. It maps to section 5.2.3 of the data model.
 */
public class Transition {

    private String id;
    private String name;
    private String petriNetId;
    private Type type; // Assuming Type is an Enum (e.g., USER, ADMIN)


    public Transition() {
        // Default constructor required by Jackson for deserialization
    }


    /**
     * Constructs a new Transition definition.
     * @param petriNetId ID of the Petri net this transition belongs to
     * @param name The name of the transition
     * @param type The execution role type (USER or ADMIN)
     */
    public Transition(String petriNetId, String name, Type type) {
        this.id = "T" + UUID.randomUUID().toString();
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.petriNetId = Objects.requireNonNull(petriNetId, "PetriNet ID cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
    }


    // --- Standard Getters and Setters ---

    /** @return the name of the transition */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** @return the unique ID of the transition */
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** @return the ID of the Petri net this transition belongs to */
    public String getPetriNetId() {
        return this.petriNetId;
    }

    public void setPetriNetId(String petriNetId) {
        this.petriNetId = petriNetId;
    }

    /** @return the execution role type of the transition */
    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
    }

    /**
     * Toggles the type of the transition (from USER to ADMIN or vice-versa).
     * This is "design" logic (used by the Admin), not "execution" logic,
     * so it is correct for it to be here.
     * @return The new type
     */
    public Type toggleType() {
        this.type = (this.type == Type.USER) ? Type.ADMIN : Type.USER;
        return this.type;
    }

    @Override
    public String toString() {
        return String.format("Transition[id=%s, name=%s, type=%s]", id, name, type);
    }
}