package application.logic;

/**
 * Concrete Strategy implementing the rules for USER-designated transitions.
 * (Rule 2.1: Net Admin cannot act as User on own net).
 */
public class UserExecutionStrategy implements TransitionExecutionStrategy {

    @Override
    public void checkPermissions(User user, PetriNet net, Transition transition) {
        boolean isNetAdmin = user.isAdmin() && net.getAdminId().equals(user.getId());

        if (isNetAdmin) {
            throw new IllegalStateException("Administrator cannot execute user transitions on their own net.");
        }
    }
}