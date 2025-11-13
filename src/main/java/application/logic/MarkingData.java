
package application.logic;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the Marking of a single Petri net computation.
 * This class tracks the distribution of tokens across all places.
 * This object corresponds to the 'MarkingData' (5.2.6) when serialized.
 */
public class MarkingData {

    // Questo è il dato che verrà serializzato in JSON.
    private Map<String, Integer> tokensPerPlace;

    /**
     * Default constructor. Creates an empty marking.
     * Used by Jackson for deserialization.
     */
    public MarkingData() {
        this.tokensPerPlace = new HashMap<>();
    }

    /**
     * Copy Constructor. Creates a deep copy of another marking.
     * This is essential for the 'fire()' method.
     */
    public MarkingData(MarkingData other) {
        this.tokensPerPlace = new HashMap<>(other.tokensPerPlace);
    }

    // --- Getters e Setters per la serializzazione Jackson ---

    /**
     * @return token data map.
     */
    public Map<String, Integer> getTokensPerPlace() {
        return tokensPerPlace;
    }

    /**
     * @param tokensPerPlace The new token data map.
     */
    public void setTokensPerPlace(Map<String, Integer> tokensPerPlace) {
        this.tokensPerPlace = tokensPerPlace;
    }

    /*Business Logic*/

    /**
     * Gets the number of tokens for a specific place.
     */
    public int getTokens(String placeId) {
        return tokensPerPlace.getOrDefault(placeId, 0);
    }

    /**
     * Checks if a place has at least one token.
     */
    public boolean hasTokens(String placeId) {
        return getTokens(placeId) > 0;
    }

    /**
     * Sets the token count for a place to a specific value.
     * Used to set the Initial Marking (M0).
     */
    public void setTokens(String placeId, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Token count cannot be negative.");
        }
        if (count == 0) {
            tokensPerPlace.remove(placeId);
        } else {
            tokensPerPlace.put(placeId, count);
        }
    }

    /**
     * Adds a specific number of tokens to a place (supports weighted arcs).
     */
    public void addTokens(String placeId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive.");
        }
        int newCount = getTokens(placeId) + amount;
        tokensPerPlace.put(placeId, newCount);
    }

    /**
     * Removes a specific number of tokens from a place (supports weighted arcs).
     */
    public void removeTokens(String placeId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to remove must be positive.");
        }
        int currentCount = getTokens(placeId);
        if (currentCount < amount) {
            throw new IllegalStateException(
                    "Cannot remove " + amount + " tokens from place " + placeId +
                            ", which only has " + currentCount + " tokens."
            );
        }

        int newCount = currentCount - amount;
        if (newCount == 0) {
            tokensPerPlace.remove(placeId);
        } else {
            tokensPerPlace.put(placeId, newCount);
        }
    }

    //TODO: altri metodi helper
}