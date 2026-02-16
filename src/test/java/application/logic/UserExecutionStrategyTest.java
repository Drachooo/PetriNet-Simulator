package application.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserExecutionStrategy}.
 * Verifies that the strategy correctly blocks or allows execution based on user roles and net ownership.
 */
class UserExecutionStrategyTest {

    private UserExecutionStrategy strategy;
    private PetriNet sampleNet;
    private Transition userTransition;

    @BeforeEach
    void setUp() {
        strategy = new UserExecutionStrategy();
        // The admin of the net will be "ADMIN_01"
        sampleNet = new PetriNet("TestNet", "ADMIN_01");
        userTransition = new Transition(sampleNet.getId(), "UserTask", Type.USER);
    }

    /**
     * Verifies that a regular user (not the admin) can execute the transition.
     */
    @Test
    void testCheckPermissions_RegularUserSuccess() {
        User regularUser = new User("user@test.com", "password", Type.USER);

        // Should not throw any exception
        assertDoesNotThrow(() -> strategy.checkPermissions(regularUser, sampleNet, userTransition));
    }

    /**
     * Verifies that an Admin can execute user transitions on nets they DO NOT own.
     */
    @Test
    void testCheckPermissions_DifferentAdminSuccess() {
        User otherAdmin = new User("other@admin.com", "password", Type.ADMIN);

        // otherAdmin (ID generated via constructor) is not "ADMIN_01"
        assertDoesNotThrow(() -> strategy.checkPermissions(otherAdmin, sampleNet, userTransition));
    }

    /**
     * Verifies that the net owner (Admin) is blocked from executing user transitions on their own net.
     */
    @Test
    void testCheckPermissions_NetOwnerBlocked() {
        // We need an admin user whose ID matches the net's adminId ("ADMIN_01")
        // Since we can't set the ID, we create the admin first and then the net
        User owner = new User("owner@admin.com", "password", Type.ADMIN);
        PetriNet ownedNet = new PetriNet("OwnerNet", owner.getId());

        assertThrows(IllegalStateException.class,
                () -> strategy.checkPermissions(owner, ownedNet, userTransition),
                "Owner should be blocked from executing user transitions on their own net");
    }
}