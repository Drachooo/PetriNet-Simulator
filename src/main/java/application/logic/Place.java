package application.logic;

import java.util.Objects;
import java.util.UUID;

/*UUID: A class that represents an immutable universally unique identifier (UUID). A UUID represents a 128-bit value*/
//Da creare i commenti JavaDoc

public class Place {
    private final String id;
    private final String petriNetId;
    private final String name;
    private int tokens;

    public Place(String name, String petriNetId) {
        this.id = "P"+UUID.randomUUID().toString(); /*creo un UUID random e lo converto a stringa*/
        this.name = Objects.requireNonNull(name);
        this.petriNetId = Objects.requireNonNull(petriNetId); /*Lancia NullPointerException se viene passato null*/
        this.tokens = 0;
    }

    /*Aggiungo tokens*/
    /*TODO: AGGIUNGERE SINCRONIZZAZIONE SE NECESSARIA, ora non pensiamoci*/
    public void addToken() {
        tokens++;
    }

    public void setTokens(int tokens) {this.tokens = tokens;}

    /*Decremento tokens: se <=0, lancio IllegalStateException*/
    /*TODO: AGGIUNGERE SINCRONIZZAZIONE SE NECESSARIA*/
    public void removeToken() {
        if (tokens <= 0) {
            throw new IllegalStateException("No tokens to remove");
        }
        tokens--;
    }

    /*True se tokens > 0*/
    public boolean hasTokens() {
        return tokens > 0;
    }

    //Getters
    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public String getPetriNetId() {
        return this.petriNetId;
    }

    public int getTokens() {
        return this.tokens;
    }

    @Override
    public String toString() {
        return "P-"+this.name+": "+this.tokens;
    }
}