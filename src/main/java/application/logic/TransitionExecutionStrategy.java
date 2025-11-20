package application.logic;

/**
 * Defines the contract for enforcing permission and execution rules for a transition type.
 * This is the Strategy interface.
 */
public interface TransitionExecutionStrategy {

    /**
     * Checks if the given User has the necessary permissions to fire the transition on the specified Net.
     * @param user The User attempting the fire action.
     * @param net The PetriNet definition the computation belongs to.
     * @param transition The Transition being fired.
     * @throws IllegalStateException if permissions are denied.
     */
    void checkPermissions(User user, PetriNet net, Transition transition);
}