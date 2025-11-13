package application.logic;

// DA MODIFICARE PERCHE ADMIN POSSONO DEFINIRE INITIAL E FINAL PLACES

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PetriNet {
    private  String id;
    private  String name;
    private  String adminId;


    /*Dico a Jackson di serializzare la stringa in questo modo*/

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern= "dd-MM-yyyy HH:mm:ss")
    private  LocalDateTime dateCreated;

    private String initialPlaceId = null;
    private String finalPlaceId = null;

    private final Map<String, Place> places = new HashMap<>(); // Per i place non dovrebbe servire l'ordine di inserimento
    private final Map<String, Transition> transitions = new HashMap<>(); // Neanche per le transizioni
    private final Map<String, Arc> arcs = new HashMap<>();

    /*per Jackson (Michael)*/
    public PetriNet() {
        this.id = null;
        this.name = null;
        this.adminId = null;
        this.dateCreated = null;
    }
    public PetriNet(String name, String adminId) {
        this.id = "NP" + UUID.randomUUID().toString();
        this.name = Objects.requireNonNull(name);
        this.adminId = Objects.requireNonNull(adminId);
        this.dateCreated = LocalDateTime.now();
    }

    public void addPlace(Place place) {
        Objects.requireNonNull(place);
        if (!place.getPetriNetId().equals(this.id)) {
            throw new IllegalArgumentException("Place belongs to another Petri net");
        }
        if (places.containsKey(place.getId())) {
            throw new IllegalArgumentException("Place with this ID already exists");
        }
        places.put(place.getId(), place);
    }

    public void addTransition(Transition transition) {
        Objects.requireNonNull(transition);
        if (!transition.getPetriNetId().equals(this.id)) {
            throw new IllegalArgumentException("Transition belongs to another Petri net");
        }
        if (transitions.containsKey(transition.getId())) {
            throw new IllegalArgumentException("Transition with this ID already exists");
        }
        transitions.put(transition.getId(), transition);
    }

    public void addArc(Arc arc) {
        Objects.requireNonNull(arc);

        if (!arc.getPetriNetId().equals(this.id)) {
            throw new IllegalArgumentException("Arc belongs to another Petri net");
        }

        if (!existsElement(arc.getSourceId()) || !existsElement(arc.getTargetId())) {
            throw new IllegalArgumentException("Invalid source or target ID");
        }

        if (arcs.containsKey(arc.getId())) {
            throw new IllegalArgumentException("Arc with this ID already exists");
        }

        if (hasArcBetween(arc.getTargetId(), arc.getSourceId())) {
            throw new IllegalArgumentException("Cannot add arc: inverse arc already exists");
        }

        verifyInitialFinal(arc);
        arcs.put(arc.getId(), arc);
    }

    public void setInitial(Place place) {
        Objects.requireNonNull(place);
        if (!places.containsKey(place.getId())) {
            throw new IllegalArgumentException("Place must be added to the net first");
        }

        // Se questo posto era il posto finale, ora non lo è più.
        if (finalPlaceId != null && finalPlaceId.equals(place.getId())) {
            finalPlaceId = null;
        }

        this.initialPlaceId = place.getId();
    }



    public void setFinal(Place place) {
        Objects.requireNonNull(place);
        if (!places.containsKey(place.getId())) {
            throw new IllegalArgumentException("Place must be added to the net first");
        }

        // Se questo posto era il posto iniziale, ora non lo è più.
        if (initialPlaceId != null && initialPlaceId.equals(place.getId())) {
            initialPlaceId = null;
        }

        this.finalPlaceId = place.getId();
    }


    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getInitialPlaceId() {
        return initialPlaceId;
    }

    public Place getInitialPlace() {
        if (initialPlaceId == null) {
            return null;
        }
        return places.get(initialPlaceId);
    }

    public String getFinalPlaceId() {
        return finalPlaceId;
    }

    public Place getFinalPlace() {
        if (finalPlaceId == null) {
            return null;
        }
        return places.get(finalPlaceId);
    }

    public Map<String, Place> getPlaces() {
        return Collections.unmodifiableMap(places);
    }

    public Map<String, Transition> getTransitions() {
        return Collections.unmodifiableMap(transitions);
    }

    public Map<String, Arc> getArcs() {
        return Collections.unmodifiableMap(arcs);
    }

    @JsonProperty("dateCreated")
    public String getCreationDateFormatted() {
        return dateCreated.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    private boolean existsElement(String elementId) {
        return places.containsKey(elementId) || transitions.containsKey(elementId);
    }

    public void validate() throws IllegalArgumentException {
        validateSingleInitialPlace();
        validateSingleFinalPlace();
    }

    public boolean isEmpty() throws IllegalArgumentException {
        return arcs.isEmpty() && transitions.isEmpty() && initialPlaceId == null && finalPlaceId == null;
    }

    private void validateSingleInitialPlace() {
        if (initialPlaceId == null) {
            throw new IllegalArgumentException("initialPlace must be defined");
        }

        List<Place> candidates = new ArrayList<>();

        for (Place place : places.values()) {
            boolean hasIncomingArc = false;

            for (Arc arc : arcs.values()) {
                if (arc.getTargetId().equals(place.getId())) {
                    hasIncomingArc = true;
                    break;
                }
            }

            if (!hasIncomingArc) {
                candidates.add(place);
            }
        }

        if (candidates.size() != 1) {
            throw new IllegalStateException("There must be exactly one initial place (Place with no incoming arcs), found: " + candidates.size());
        }

        if (!candidates.getFirst().getId().equals(initialPlaceId)) {
            throw new IllegalStateException("The DEFINED initial place does not match the initial place of the net you designed.");
        }
    }

    private void validateSingleFinalPlace() {
        if (finalPlaceId == null) {
            throw new IllegalArgumentException("finalPlace must be defined");
        }

        List<Place> candidates = new ArrayList<>();

        for (Place place : places.values()) {
            boolean hasOutgoingArc = false;

            for (Arc arc : arcs.values()) {
                if (arc.getSourceId().equals(place.getId())) {
                    hasOutgoingArc = true;
                    break;
                }
            }

            if (!hasOutgoingArc) {
                candidates.add(place);
            }
        }

        if (candidates.size() != 1) {
            throw new IllegalStateException("There must be exactly one final place, found: " + candidates.size());
        }

        if (!candidates.getFirst().getId().equals(finalPlaceId)) {
            throw new IllegalStateException("The DEFINED final place does not match the the final place of the net you designed.");
        }
    }

    private void verifyInitialFinal(Arc arc) {
        if (arc.getTargetId().equals(initialPlaceId)) {
            throw new IllegalArgumentException("Cannot add incoming arcs to initial place");
        }
        if (arc.getSourceId().equals(finalPlaceId)) {
            throw new IllegalArgumentException("Cannot add outgoing arcs from final place");
        }

    }

    public boolean hasArcBetween(String sourceId, String targetId) {
        for (Arc arc : arcs.values()) {
            if (arc.getSourceId().equals(sourceId) && arc.getTargetId().equals(targetId)) {
                return true;
            }
        }
        return false;
    }

    public void removePlace(String placeId) {
        if (!places.containsKey(placeId)) {
            throw new IllegalArgumentException("Place not found");
        }

        removeArcsConnectedTo(placeId);

        if (initialPlaceId != null && initialPlaceId.equals(placeId)) {
            initialPlaceId = null;
        }
        if (finalPlaceId != null && finalPlaceId.equals(placeId)) {
            finalPlaceId = null;
        }

        places.remove(placeId);
    }

    public void removeTransition(String transitionId) {
        if (!transitions.containsKey(transitionId)) {
            throw new IllegalArgumentException("Transition not found");
        }

        removeArcsConnectedTo(transitionId);

        transitions.remove(transitionId);
    }


    public void removeArc(String arcId) {
        arcs.remove(arcId);
    }

    public void removeArcsConnectedTo(String elementId) {
        arcs.values().removeIf(arc ->
                arc.getSourceId().equals(elementId) || arc.getTargetId().equals(elementId)
        );
    }

}