package application.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PetriNetCoordinates {

    public static class Position {
        public double x;
        public double y;

        public Position() { }  // Costruttore vuoto necessario a Jackson

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private Map<String, Position> placePositions = new HashMap<>();
    private Map<String, Position> transitionPositions = new HashMap<>();

    // metodi get/set

    public void setPlacePosition(String placeId, double x, double y) {
        placePositions.put(placeId, new Position(x, y));
    }

    public Position getPlacePosition(String placeId) {
        return placePositions.get(placeId);
    }

    public void setTransitionPosition(String transitionId, double x, double y) {
        transitionPositions.put(transitionId, new Position(x, y));
    }

    public Position getTransitionPosition(String transitionId) {
        return transitionPositions.get(transitionId);
    }

    public void clear() {
        placePositions.clear();
        transitionPositions.clear();
    }

    // Salvataggio su file JSON
    public void saveToFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(filePath), this);
    }

    // Caricamento da file JSON
    public static PetriNetCoordinates loadFromFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), PetriNetCoordinates.class);
    }
}
