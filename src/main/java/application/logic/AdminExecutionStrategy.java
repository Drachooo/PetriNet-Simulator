package application.logic;

/**
 * Concrete Strategy implementing the rules for ADMIN-designated transitions.
 * (Rule 2.3: Only the Net Admin can fire).
 */
public class AdminExecutionStrategy implements TransitionExecutionStrategy {

    @Override
    public void checkPermissions(User user, PetriNet net, Transition transition) {
        boolean isNetAdmin = user.isAdmin() && net.getAdminId().equals(user.getId());

        if (!isNetAdmin) {
            throw new IllegalStateException("You are not authorized to fire this Administrator-designated transition.");
        }
    }
}