package application.logic;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class MarkingData {
    private Map<String, Integer> marking = new HashMap<>();

    // Costruttore che estrae il marking da una rete di Petri
    public MarkingData(PetriNet net) {
        for (Place place : net.getPlaces().values()) {
            marking.put(place.getId(), place.getTokens());
        }
    }

    // Costruttore vuoto richiesto da Jackson
    public MarkingData() {}

    // Getter per ottenere il marking
    public Map<String, Integer> getMarking() {
        return marking;
    }

    // Setter usato da Jackson in fase di deserializzazione
    @JsonProperty("marking")
    public void setMarking(Map<String, Integer> marking) {
        this.marking = marking;
    }

    // Metodo per convertire l'oggetto in JSON
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    // Metodo statico per creare MarkingData da JSON
    public static MarkingData fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, MarkingData.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return toJson();
    }
}
