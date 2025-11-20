package application.logic;

/**
 * Defines the contract for classes that want to observe state changes in a Computation object.
 */
public abstract class ComputationObserver {

    /**
     * Called by the Computation subject when its state (e.g., the steps history) changes.
     * The observer should use this trigger to refresh its display.
     * @param updatedComputation The updated Computation object, allowing the observer to pull the new state.
     */
    public abstract void update(Computation updatedComputation);
}