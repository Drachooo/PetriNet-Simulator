package application.logic;

import java.util.*;
import java.util.Objects;


/**
 * Rappresenta una transizione in una rete di Petri.
 */
public class Transition {

    private String id;
    private String name;
    private String petriNetId;
    private Type type;


    /** Insiemi dei posti di input e output */
    private Map<String, Place> inputPlaces;
    private Map<String,Place> outputPlaces;


    public Transition() {
        // Costruttore richiesto da Jackson
    }


    /**
     * Costruttore di una transizione.
     * @param petriNetId ID della rete di Petri
     * @param name nome della transizione
     * @param type tipo della transizione
     */
    public Transition(String petriNetId, String name, Type type) {
        this.id = "T"+UUID.randomUUID().toString();
        this.type = Objects.requireNonNull(type);
        this.petriNetId = Objects.requireNonNull(petriNetId);
        this.name = Objects.requireNonNull(name);
        this.inputPlaces = new HashMap<>();
        this.outputPlaces = new HashMap<>();
    }

    /**
     * Aggiunge un posto di input.
     * @param place Place
     * @return insieme aggiornato dei posti di input
     */

    public void addInputPlace(Place place) {
        Objects.requireNonNull(place, "Place cannot be null");
        if (!place.getId().startsWith("P")) {
            throw new IllegalArgumentException("Place ID must start with 'P'");
        }
        if (!place.getPetriNetId().equals(this.petriNetId)) {
            throw new IllegalArgumentException("Place belongs to different Petri net");
        }
        inputPlaces.put(place.getId(), place);
    }

    /**
     * Aggiunge un posto di output.
     * @param place Place
     */
    public void addOutputPlace(Place place) {
        if (!place.getId().startsWith("P"))
            throw new IllegalArgumentException("Id must be of a Place");
        outputPlaces.put(place.getId(), place);
    }

    /**
     * Verifica se la transizione è abilitata (ossia se tutti gli input hanno token).
     * @param places mappa di posti
     * @return true se abilitata
     */
    public boolean isEnabled(Map<String, Place> places) {
        for (String placeId : inputPlaces.keySet()) {
            if (!places.containsKey(placeId) || !places.get(placeId).hasTokens()) {
                return false;
            }
        }
        return true;
    }


    public void fire() {
        if (!isEnabled(inputPlaces)) {
            throw new IllegalStateException(
                    String.format("Transition %s is not enabled", this.name));
        }
        for (String placeId : inputPlaces.keySet()) {
            inputPlaces.get(placeId).removeToken();
        }
        for (String placeId : outputPlaces.keySet()) {
            outputPlaces.get(placeId).addToken();
        }
    }

    /** @return nome della transizione */
    public String getName() {
        return this.name;
    }

    /** @return ID della transizione */
    public String getId() {
        return this.id;
    }

    /** @return ID della rete di Petri */
    public String getPetriNetId() {
        return this.petriNetId;
    }

    /** @return tipo della transizione */
    public Type getType() {
        return this.type;
    }

    public Map<String,Place> getInputPlaces() {
        return Collections.unmodifiableMap(inputPlaces);
    }

    public Map<String,Place> getOutputPlaces() {
        return Collections.unmodifiableMap(outputPlaces);
    }

    @Override
    public String toString() {
        return String.format("Transition[id=%s, name=%s, type=%s]", id, name, type);
    }
}