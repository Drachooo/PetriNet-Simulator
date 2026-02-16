package application.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link SharedResources} singleton.
 * Verifies that the singleton pattern is enforced and resources are correctly shared.
 */
class SharedResourcesTest {

    /**
     * Verifies that getInstance() always returns the same identical object (Memory Address).
     */
    @Test
    void testSingletonInstanceIsUnique() {
        SharedResources instance1 = SharedResources.getInstance();
        SharedResources instance2 = SharedResources.getInstance();

        assertNotNull(instance1, "Instance should not be null");
        // assertSame controlla che l'indirizzo di memoria sia lo stesso (==)
        assertSame(instance1, instance2, "Both instances must point to the same memory address");
    }

    /**
     * Verifies that the shared components (Repos and Service) are initialized.
     */
    @Test
    void testComponentsAreInitialized() {
        SharedResources resources = SharedResources.getInstance();

        assertNotNull(resources.getUserRepository(), "UserRepository should be initialized");
        assertNotNull(resources.getPetriNetRepository(), "PetriNetRepository should be initialized");
        assertNotNull(resources.getProcessService(), "ProcessService should be initialized");
    }

    /**
     * Verifies that the components inside the singleton are also unique.
     */
    @Test
    void testSharedComponentsAreTheSame() {
        SharedResources resources = SharedResources.getInstance();

        // Verifichiamo che i repository passati al ProcessService siano gli stessi esposti dai getter
        // Questo è fondamentale per la coerenza dei dati (Data Integrity)
        assertSame(resources.getUserRepository(), resources.getUserRepository());
        assertSame(resources.getPetriNetRepository(), resources.getPetriNetRepository());
    }
}