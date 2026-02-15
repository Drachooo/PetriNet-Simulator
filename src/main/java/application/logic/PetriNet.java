package application.logic;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Represents the formal definition and execution engine of a single Petri Net.
 * It stores the net's structure (Places, Transitions, Arcs) and enforces
 * structural validity and operational semantics.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PetriNet {
    private String id;
    private String name;
    private String adminId;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern= "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime dateCreated;

    private String initialPlaceId = null;
    private String finalPlaceId = null;

    private final Map<String, Place> places = new HashMap<>();
    private final Map<String, Transition> transitions = new HashMap<>();
    private final Map<String, Arc> arcs = new HashMap<>();

    /**
     * Default constructor required for deserialization (Jackson).
     */
    public PetriNet() {
        this.id = null;
        this.name = null;
        this.adminId = null;
        this.dateCreated = null;
    }

    /**
     * Constructs a new Petri Net, setting the creator and initial data.
     * @param name The name of the net.
     * @param adminId The ID of the administrator who created the net.
     */
    public PetriNet(String name, String adminId) {
        this.id = "NP" + UUID.randomUUID().toString();
        this.name = Objects.requireNonNull(name);
        this.adminId = Objects.requireNonNull(adminId);
        this.dateCreated = LocalDateTime.now();
    }

    // --- JACKSON SETTERS ---
    // These are required so Jackson can reconstruct the object from JSON.
    // They are kept private/package-private so business logic cannot misuse them.

    @JsonSetter("id")
    private void setId(String id) { this.id = id; }

    @JsonSetter("name")
    private void setName(String name) { this.name = name; }

    @JsonSetter("adminId")
    private void setAdminId(String adminId) { this.adminId = adminId; }

    @JsonSetter("initialPlaceId")
    private void setInitialPlaceId(String initialPlaceId) { this.initialPlaceId = initialPlaceId; }

    @JsonSetter("finalPlaceId")
    private void setFinalPlaceId(String finalPlaceId) { this.finalPlaceId = finalPlaceId; }

    @JsonSetter("dateCreated")
    private void setDateCreated(String dateCreatedStr) {
        if (dateCreatedStr != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            this.dateCreated = LocalDateTime.parse(dateCreatedStr, formatter);
        }
    }

    @JsonSetter("places")
    private void setPlaces(Map<String, Place> parsedPlaces) {
        if (parsedPlaces != null) {
            this.places.clear();
            this.places.putAll(parsedPlaces);
        }
    }

    @JsonSetter("transitions")
    private void setTransitions(Map<String, Transition> parsedTransitions) {
        if (parsedTransitions != null) {
            this.transitions.clear();
            this.transitions.putAll(parsedTransitions);
        }
    }

    @JsonSetter("arcs")
    private void setArcs(Map<String, Arc> parsedArcs) {
        if (parsedArcs != null) {
            this.arcs.clear();
            this.arcs.putAll(parsedArcs);
        }
    }
    // --- END JACKSON SETTERS ---

    /**
     * Adds a Place element to the net definition.
     * @param place The Place object to add.
     * @throws IllegalArgumentException if the place already exists or belongs to another net.
     */
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

    /**
     * Adds a Transition element to the net definition.
     * @param transition The Transition object to add.
     * @throws IllegalArgumentException if the transition already exists or belongs to another net.
     */
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

    /**
     * Adds an Arc element to the net definition.
     * Enforces Petri Net structural rules (bipartite graph, no inverse arcs).
     * @param arc The Arc object to add.
     * @throws IllegalArgumentException if the arc violates structural rules.
     */
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

    /**
     * Designates a Place as the initial starting point (pinit).
     * @param place The Place to be set as initial.
     */
    public void setInitial(Place place) {
        Objects.requireNonNull(place);
        if (!places.containsKey(place.getId())) {
            throw new IllegalArgumentException("Place must be added to the net first");
        }

        // If this place was the final place, it no longer is.
        if (finalPlaceId != null && finalPlaceId.equals(place.getId())) {
            finalPlaceId = null;
        }

        this.initialPlaceId = place.getId();
    }


    /**
     * Designates a Place as the final completion point (pfinal).
     * @param place The Place to be set as final.
     */
    public void setFinal(Place place) {
        Objects.requireNonNull(place);
        if (!places.containsKey(place.getId())) {
            throw new IllegalArgumentException("Place must be added to the net first");
        }

        // If this place was the initial place, it no longer is.
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

    /**
     * Gets the ID of the designated initial place. (Saved to JSON)
     */
    public String getInitialPlaceId() {
        return initialPlaceId;
    }

    /**
     * Gets the designated initial Place object (helper, ignored by JSON serializer).
     */
    @JsonIgnore
    public Place getInitialPlace() {
        if (initialPlaceId == null) {
            return null;
        }
        return places.get(initialPlaceId);
    }

    /**
     * Gets the ID of the designated final place. (Saved to JSON)
     */
    public String getFinalPlaceId() {
        return finalPlaceId;
    }

    /**
     * Gets the designated final Place object (helper, ignored by JSON serializer).
     */
    @JsonIgnore
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

    /**
     * Gets the formatted date of creation for display or serialization.
     */
    @JsonProperty("dateCreated")
    public String getCreationDateFormatted() {
        return dateCreated.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    /**
     * Checks if a Place or Transition exists by its ID.
     */
    private boolean existsElement(String elementId) {
        return places.containsKey(elementId) || transitions.containsKey(elementId);
    }

    /**
     * Validates the structural correctness of the Petri Net (e.g., initial/final place rules).
     * Must be called before saving.
     * @throws IllegalArgumentException if the structure is invalid.
     */
    public void validate() throws IllegalArgumentException {
        validateSingleInitialPlace();
        validateSingleFinalPlace();
    }

    /**
     * Checks if the net contains any elements (helper, ignored by JSON serializer).
     */
    @JsonIgnore
    public boolean isEmpty() throws IllegalArgumentException {
        return arcs.isEmpty() && transitions.isEmpty() && initialPlaceId == null && finalPlaceId == null;
    }

    /**
     * Enforces the rule that there must be exactly one Place with no incoming arcs,
     * and it must be the designated initial place.
     */
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

    /**
     * Enforces the rule that there must be exactly one Place with no outgoing arcs,
     * and it must be the designated final place.
     */
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

    /**
     * Checks arc validity against initial and final place constraints.
     */
    private void verifyInitialFinal(Arc arc) {
        if (arc.getTargetId().equals(initialPlaceId)) {
            throw new IllegalArgumentException("Cannot add incoming arcs to initial place");
        }
        if (arc.getSourceId().equals(finalPlaceId)) {
            throw new IllegalArgumentException("Cannot add outgoing arcs from final place");
        }

    }

    /**
     * Checks if an arc already exists between the given source and target IDs.
     */
    public boolean hasArcBetween(String sourceId, String targetId) {
        for (Arc arc : arcs.values()) {
            if (arc.getSourceId().equals(sourceId) && arc.getTargetId().equals(targetId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a Place from the net definition and manages related structural IDs.
     * @param placeId The ID of the Place to remove.
     */
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

    /**
     * Removes a Transition from the net definition.
     * @param transitionId The ID of the Transition to remove.
     */
    public void removeTransition(String transitionId) {
        if (!transitions.containsKey(transitionId)) {
            throw new IllegalArgumentException("Transition not found");
        }

        removeArcsConnectedTo(transitionId);

        transitions.remove(transitionId);
    }

    /**
     * Removes an Arc from the net definition.
     * @param arcId The ID of the Arc to remove.
     */
    public void removeArc(String arcId) {
        arcs.remove(arcId);
    }

    /**
     * Helper to remove all Arcs connected to a specific Place or Transition ID.
     */
    public void removeArcsConnectedTo(String elementId) {
        arcs.values().removeIf(arc ->
                arc.getSourceId().equals(elementId) || arc.getTargetId().equals(elementId)
        );
    }

    /**
     * Checks if a Transition is enabled based on the current Marking (operational semantics).
     * @param transitionId The ID of the transition to check.
     * @param marking The current token distribution (state).
     * @return true if the transition can fire, false otherwise.
     */
    public boolean isEnabled(String transitionId, MarkingData marking) {
        Objects.requireNonNull(marking, "Marking cannot be null");
        if (!transitions.containsKey(transitionId)) {
            throw new IllegalArgumentException("Transition " + transitionId + " not found in this net.");
        }

        // Check if all input places have enough tokens
        for (Arc arc : arcs.values()) {
            if (arc.getTargetId().equals(transitionId)) {
                // This is an input arc: (Place) -> (Transition)
                String inputPlaceId = arc.getSourceId();
                int weight = arc.getWeight();

                if (marking.getTokens(inputPlaceId) < weight) {
                    return false; // Not enough tokens
                }
            }
        }
        return true; // All requirements met
    }

    /**
     * Implements the "Firing Rule" and calculates the new Marking (M').
     * @param transitionId The ID of the transition to fire.
     * @param currentMarking The state before firing.
     * @return A new Marking object representing the state after firing.
     * @throws IllegalStateException if the transition is not enabled.
     */
    public MarkingData fire(String transitionId, MarkingData currentMarking) {
        if (!isEnabled(transitionId, currentMarking)) {
            throw new IllegalStateException("Transition " + transitionId + " is not enabled.");
        }

        // Create a copy of the state to modify.
        MarkingData newMarking = new MarkingData(currentMarking);

        // Remove tokens from input places (M'(p) = M(p) - W(p, t))
        for (Arc arc : arcs.values()) {
            if (arc.getTargetId().equals(transitionId)) {
                String inputPlaceId = arc.getSourceId();
                int weight = arc.getWeight();
                newMarking.removeTokens(inputPlaceId, weight);
            }
        }

        // Add tokens to output places (M'(p) = ... + W(t, p))
        for (Arc arc : arcs.values()) {
            if (arc.getSourceId().equals(transitionId)) {
                String outputPlaceId = arc.getTargetId();
                int weight = arc.getWeight();
                newMarking.addTokens(outputPlaceId, weight);
            }
        }

        return newMarking; // Return the new state
    }
}