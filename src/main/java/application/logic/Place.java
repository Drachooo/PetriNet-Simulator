package application.logic;

import java.util.Objects;
import java.util.UUID;

public class Place {
    private String id;
    private String petriNetId;
    private String name;
    private int tokens;

    // Costruttore senza argomenti per Jackson
    public Place() {}

    // Costruttore per uso normale
    public Place(String petriNetId, String name) {
        this.id = "P" + UUID.randomUUID().toString();
        this.name = Objects.requireNonNull(name);
        this.petriNetId = Objects.requireNonNull(petriNetId);
        this.tokens = 0;
    }

    public void addToken() {
        tokens++;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public void removeToken() {
        if (tokens <= 0) {
            throw new IllegalStateException("No tokens to remove");
        }
        tokens--;
    }

    public boolean hasTokens() {
        return tokens > 0;
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

    public int getTokens() {
        return tokens;
    }

    @Override
    public String toString() {
        return "P-" + name + ": " + tokens;
    }
}
