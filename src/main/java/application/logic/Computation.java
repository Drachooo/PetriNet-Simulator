package application.logic;

import java.time.LocalDateTime;
import java.util.*;

// Imports needed for Jackson
import com.fasterxml.jackson.annotation.*;

/**
 * Represents a single execution instance of a Petri net by a user.
 * Maps to data model 5.2.5.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Computation {

    /**
     * Defines the runtime status of a Computation.
     */
    public enum ComputationStatus {
        ACTIVE, COMPLETED;

        @JsonIgnore
        public boolean isActive() {
            return this == ACTIVE;
        }
    }

    private final String id;
    private final String petriNetId;
    private final String userId;
    private ComputationStatus status; // Mutable: changes during lifecycle

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private final LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime endTime; // Mutable: set on completion

    // Holds the history of this computation
    private final List<ComputationStep> steps = new ArrayList<>();

    /*Transient does not let jackson save on dislk*/
    private transient List<ComputationObserver> observers = new ArrayList<>();

    /**
     * Business constructor for starting a new computation.
     * @param petriNetId The net being executed.
     * @param userId The user executing the net.
     */
    public Computation(String petriNetId, String userId) {
        this.id = "CO" + UUID.randomUUID().toString();
        this.petriNetId = Objects.requireNonNull(petriNetId, "PetriNet ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.status = ComputationStatus.ACTIVE;
        this.startTime = LocalDateTime.now();
    }

    /**
     * Constructor for Jackson deserialization.
     * @JsonCreator tells Jackson to use this constructor.
     * @JsonProperty maps JSON fields to parameters.
     */
    @JsonCreator
    public Computation(
            @JsonProperty("id") String id,
            @JsonProperty("petriNetId") String petriNetId,
            @JsonProperty("userId") String userId,
            @JsonProperty("status") ComputationStatus status,
            @JsonProperty("startTime") LocalDateTime startTime,
            @JsonProperty("endTime") LocalDateTime endTime,
            @JsonProperty("steps") List<ComputationStep> steps)


    {
        this.id = id;
        this.petriNetId = petriNetId;
        this.userId = userId;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.observers=new ArrayList<>();
        if (steps != null) {
            this.steps.addAll(steps);
        }
    }


    //METHODS FOR PATTERN OBSERVER
    /**
     * Attaches an observer to this subject. The observer will be notified upon state changes.
     */
    public void attach(ComputationObserver observer) {
        if(observers==null) {
            observers = new ArrayList<>();
        }
        observers.add(observer);
    }

    /**
     * Notifies all attached observers that the computation state has changed.
     */
    private void notifyObservers() {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        for (ComputationObserver observer : observers) {
            observer.update(this);
        }
    }

    /**
     * Marks the computation as completed.
     */
    public void completeComputation() {
        if (!status.isActive()) {
            throw new IllegalStateException("Computation has already been completed");
        }
        this.status = ComputationStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        notifyObservers();
    }

    /**
     * Adds a new history step to this computation.
     * @param step The step to add.
     */
    public void addStep(ComputationStep step) {
        Objects.requireNonNull(step, "Computation step cannot be null");
        if (!step.getComputationId().equals(this.id)) {
            throw new IllegalArgumentException("Step does not belong to this computation");
        }
        steps.add(step);
        notifyObservers();
    }

    /**
     * Gets the first step (which contains the initial marking).
     * @return The initial ComputationStep, or null.
     */
    @JsonIgnore
    public ComputationStep getInitialStep() {
        for (ComputationStep step : steps) {
            if (step.getTransitionId() == null) {
                return step;
            }
        }
        return null;
    }

    /**
     * Gets the most recent step (which contains the current marking).
     * @return The last ComputationStep, or null.
     */
    @JsonIgnore
    public ComputationStep getLastStep() {
        return steps.isEmpty() ? null : steps.get(steps.size() - 1);
    }

    /**
     * Returns an unmodifiable view of the computation history.
     */
    public List<ComputationStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getPetriNetId() { return petriNetId; }
    public String getUserId() { return userId; }
    public boolean isActive() { return status==ComputationStatus.ACTIVE; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public ComputationStatus getStatus() { return status; }

    @Override
    public String toString() {
        return String.format(
                "Computation[id='%s', petriNetId='%s', userId='%s', status=%s, startTime=%s, endTime=%s, stepsCount=%d]",
                id, petriNetId, userId, status, startTime, endTime, steps.size()
        );
    }
}