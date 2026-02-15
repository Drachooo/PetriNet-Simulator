package application.repositories;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores and manages the visual layout coordinates (X and Y positions)
 * for Places and Transitions within a Petri Net.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PetriNetCoordinates {

    /**
     * Represents a 2D coordinate position on the drawing canvas.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Position {
        @JsonProperty("x")
        public double x;

        @JsonProperty("y")
        public double y;

        /**
         * Default empty constructor required for Jackson deserialization.
         */
        public Position() { }

        /**
         * Constructs a Position with specific coordinates.
         *
         * @param x The X coordinate.
         * @param y The Y coordinate.
         */
        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    @JsonProperty("placePositions")
    private Map<String, Position> placePositions = new HashMap<>();

    @JsonProperty("transitionPositions")
    private Map<String, Position> transitionPositions = new HashMap<>();

    /**
     * Gets the map of place coordinates.
     *
     * @return A map linking Place IDs to their positions.
     */
    public Map<String, Position> getPlacePositions() {
        return placePositions;
    }

    /**
     * Sets the map of place coordinates.
     *
     * @param placePositions The new map of place positions.
     */
    public void setPlacePositions(Map<String, Position> placePositions) {
        this.placePositions = placePositions;
    }

    /**
     * Gets the map of transition coordinates.
     *
     * @return A map linking Transition IDs to their positions.
     */
    public Map<String, Position> getTransitionPositions() {
        return transitionPositions;
    }

    /**
     * Sets the map of transition coordinates.
     *
     * @param transitionPositions The new map of transition positions.
     */
    public void setTransitionPositions(Map<String, Position> transitionPositions) {
        this.transitionPositions = transitionPositions;
    }

    /**
     * Sets the coordinate position for a specific Place.
     *
     * @param placeId The unique identifier of the Place.
     * @param x       The X coordinate.
     * @param y       The Y coordinate.
     */
    public void setPlacePosition(String placeId, double x, double y) {
        placePositions.put(placeId, new Position(x, y));
    }

    /**
     * Retrieves the coordinate position of a specific Place.
     *
     * @param placeId The unique identifier of the Place.
     * @return The Position object, or null if not found.
     */
    public Position getPlacePosition(String placeId) {
        return placePositions.get(placeId);
    }

    /**
     * Sets the coordinate position for a specific Transition.
     *
     * @param transitionId The unique identifier of the Transition.
     * @param x            The X coordinate.
     * @param y            The Y coordinate.
     */
    public void setTransitionPosition(String transitionId, double x, double y) {
        transitionPositions.put(transitionId, new Position(x, y));
    }

    /**
     * Retrieves the coordinate position of a specific Transition.
     *
     * @param transitionId The unique identifier of the Transition.
     * @return The Position object, or null if not found.
     */
    public Position getTransitionPosition(String transitionId) {
        return transitionPositions.get(transitionId);
    }

    /**
     * Clears all saved coordinate positions.
     */
    public void clear() {
        placePositions.clear();
        transitionPositions.clear();
    }

    /**
     * Serializes this object and saves it to a JSON file.
     *
     * @param filePath The destination path for the JSON file.
     * @throws IOException If an error occurs during file writing.
     */
    public void saveToFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File f = new File(filePath);
        if (f.getParentFile() != null) {
            f.getParentFile().mkdirs();
        }
        mapper.writeValue(f, this);
    }

    /**
     * Deserializes a PetriNetCoordinates object from a JSON file.
     *
     * @param filePath The path to the JSON file to read.
     * @return The loaded PetriNetCoordinates object.
     * @throws IOException If the file is not found or cannot be parsed.
     */
    public static PetriNetCoordinates loadFromFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), PetriNetCoordinates.class);
    }
}