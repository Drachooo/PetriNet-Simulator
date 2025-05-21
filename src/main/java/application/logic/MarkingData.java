package application.logic;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MarkingData {
    private Map<String, Integer> marking = new HashMap<>();

    public MarkingData(PetriNet net) {
        for (Place place : net.getPlaces().values()) {
            marking.put(place.getId(), place.getTokens());
        }
    }

    // Getter per ottenere il marking
    public Map<String, Integer> getMarking() {
        return marking;
    }

    // Metodo per convertire l'oggetto in JSON
    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    // Metodo statico per creare MarkingData da JSON
    public static MarkingData fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, MarkingData.class);
    }

    @Override
    public String toString() {
        return toJson();
    }
}