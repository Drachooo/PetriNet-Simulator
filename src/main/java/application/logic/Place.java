package application.logic;

import java.util.Objects;
import java.util.UUID;

public class Place {
    private String id;
    private String petriNetId;
    private String name;

    // Costruttore senza argomenti per Jackson
    public Place() {}

    // Costruttore per uso normale
    public Place(String petriNetId, String name) {
        this.id = "P" + UUID.randomUUID().toString();
        this.name = Objects.requireNonNull(name);
        this.petriNetId = Objects.requireNonNull(petriNetId);
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getPetriNetId() {
        return petriNetId;
    }

    @Override
    public String toString() {
        return "P-" + name;
    }
}
