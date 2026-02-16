package application.logic;

import application.exceptions.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AdminExecutionStrategy.
 * Verifies that only the creator of the net can execute ADMIN-type transitions.
 */
class AdminExecutionStrategyTest {

    private AdminExecutionStrategy strategy;
    private PetriNet net;
    private Transition adminTransition;
    private User creatorAdmin;

    @BeforeEach
    void setUp() {
        strategy = new AdminExecutionStrategy();

        // Create the creator admin
        creatorAdmin = new User("creator@admin.com", "password", Type.ADMIN);

        // Create a net owned by this admin
        net = new PetriNet("SystemNet", creatorAdmin.getId());

        // Create an ADMIN type transition
        adminTransition = new Transition(net.getId(), "CriticalTask", Type.ADMIN);
    }

    /**
     * Rule: The creator of the net can fire their own ADMIN transitions.
     */
    @Test
    void testCheckPermissions_CreatorSuccess() {
        assertDoesNotThrow(() -> strategy.checkPermissions(creatorAdmin, net, adminTransition));
    }

    /**
     * Rule: A regular user cannot fire an ADMIN transition.
     */
    @Test
    void testCheckPermissions_RegularUserBlocked() {
        User regularUser = new User("user@test.com", "password", Type.USER);

        assertThrows(IllegalStateException.class,
                () -> strategy.checkPermissions(regularUser, net, adminTransition),
                "Regular users should be blocked from ADMIN transitions");
    }

    /**
     * Rule: A different admin (not the creator) cannot fire this net's ADMIN transitions.
     */
    @Test
    void testCheckPermissions_DifferentAdminBlocked() {
        User otherAdmin = new User("other@admin.com", "password", Type.ADMIN);

        assertThrows(IllegalStateException.class,
                () -> strategy.checkPermissions(otherAdmin, net, adminTransition),
                "Only the specific net creator should be able to fire ADMIN transitions");
    }
}