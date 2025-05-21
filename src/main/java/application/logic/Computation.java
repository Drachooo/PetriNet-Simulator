package application.logic;

import java.time.LocalDateTime;
import java.util.*;

public class Computation {

    public enum ComputationStatus {
        ACTIVE, COMPLETED;

        public boolean isActive() {
            return this == ACTIVE;
        }
    }

    private final String id;
    private final String petriNetId;
    private final String userId;
    private ComputationStatus status;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private final List<ComputationStep> steps = new ArrayList<>();

    public Computation(String petriNetId, String userId) {
        this.id = "CO" + UUID.randomUUID().toString();
        this.petriNetId = Objects.requireNonNull(petriNetId, "PetriNet ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.status = ComputationStatus.ACTIVE;
        this.startTime = LocalDateTime.now();
    }

    public void completeComputation() {
        if (!status.isActive()) {
            throw new IllegalStateException("Computation has already been completed");
        }
        this.status = ComputationStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
    }

    public void addStep(ComputationStep step) {
        Objects.requireNonNull(step, "Computation step cannot be null");
        if (!step.getComputationId().equals(this.id)) {
            throw new IllegalArgumentException("Step does not belong to this computation");
        }
        steps.add(step);
    }

    public ComputationStep getInitialStep() {
        for (ComputationStep step : steps) {
            if (step.getTransitionId() == null) {
                return step;
            }
        }
        return null;
    }

    public ComputationStep getLastStep() {
        return steps.isEmpty() ? null : steps.get(steps.size() - 1);
    }

    public List<ComputationStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    // Getters
    public String getId() { return id; }
    public String getPetriNetId() { return petriNetId; }
    public String getUserId() { return userId; }
    public boolean isActive() { return status==ComputationStatus.ACTIVE; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }  // Può restituire null
    public ComputationStatus getStatus() { return status; }

    @Override
    public String toString() {
        return String.format(
                "Computation[id='%s', petriNetId='%s', userId='%s', status=%s, startTime=%s, endTime=%s, stepsCount=%d]",
                id, petriNetId, userId, status, startTime, endTime, steps.size()
        );
    }
}