package application.logic;

import application.repositories.PetriNetRepository;
import application.repositories.UserRepository;
import application.exceptions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ProcessService} class.
 * Uses Mockito to mock the repository layer and uses dynamically generated UUIDs.
 */
class ProcessServiceTest {

    private UserRepository mockUserRepo;
    private PetriNetRepository mockNetRepo;
    private ProcessService processService;

    private User regularUser;
    private User adminUser;
    private PetriNet testNet;

    // Salviamo gli ID autogenerati per usarli nei test
    private String rUid;
    private String aUid;
    private String netId;

    @BeforeEach
    void setUp() {
        mockUserRepo = mock(UserRepository.class);
        mockNetRepo = mock(PetriNetRepository.class);

        // 1. Creiamo gli oggetti veri coi loro costruttori esatti
        regularUser = new User("user@example.com", "password123", Type.USER);
        adminUser = new User("admin@example.com", "adminpass", Type.ADMIN);

        // 2. Recuperiamo gli ID generati casualmente
        rUid = regularUser.getId();
        aUid = adminUser.getId();

        // 3. Creiamo la rete usando l'ID dell'admin appena generato
        testNet = new PetriNet("TestNet", aUid);
        netId = testNet.getId();

        Place p1 = new Place(netId, "Start");
        testNet.addPlace(p1);
        testNet.setInitial(p1);

        // 4. Istruiamo Mockito: "Quando ti chiedono questo UUID specifico, restituisci questo oggetto"
        when(mockUserRepo.getUserById(rUid)).thenReturn(regularUser);
        when(mockUserRepo.getUserById(aUid)).thenReturn(adminUser);

        Map<String, PetriNet> fakeNetDb = new HashMap<>();
        fakeNetDb.put(netId, testNet);
        when(mockNetRepo.getPetriNets()).thenReturn(fakeNetDb);

        processService = new ProcessService(mockUserRepo, mockNetRepo);
    }

    @AfterEach
    void tearDown() {
        File testFile = new File("data/computations.json");
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    void testStartNewComputation_Success() {
        Computation comp = processService.startNewComputation(rUid, netId);

        assertNotNull(comp, "Computation should be created successfully");
        assertEquals(rUid, comp.getUserId(), "User ID should match");
        assertEquals(netId, comp.getPetriNetId(), "Net ID should match");
        assertTrue(comp.isActive());
        assertEquals(1, comp.getSteps().size(), "Should have the initial marking step");
    }

    @Test
    void testStartNewComputation_AdminCannotStartOwnNet() {
        assertThrows(UnauthorizedAccessException.class,
                () -> processService.startNewComputation(aUid, netId),
                "Admin should not be able to start their own net");
    }

    @Test
    void testStartNewComputation_UserAlreadyHasActiveComputation() {
        processService.startNewComputation(rUid, netId);

        assertThrows(ActiveComputationExistsException.class,
                () -> processService.startNewComputation(rUid, netId),
                "User cannot have two active computations for the same net");
    }

    @Test
    void testStartNewComputation_EntityNotFound() {
        assertThrows(EntityNotFoundException.class,
                () -> processService.startNewComputation("UUID_INVENTATO", netId));

        assertThrows(EntityNotFoundException.class,
                () -> processService.startNewComputation(rUid, "NET_INVENTATA"));
    }

    @Test
    void testGetAvailableNetsForUser() {
        assertEquals(0, processService.getAvailableNetsForUser(aUid).size());
        assertEquals(1, processService.getAvailableNetsForUser(rUid).size());
    }

    @Test
    void testDeleteComputation_ByOwner_Success() {
        Computation comp = processService.startNewComputation(rUid, netId);
        assertNotNull(processService.getComputationById(comp.getId()));

        processService.deleteComputation(comp.getId(), rUid);

        assertNull(processService.getComputationById(comp.getId()));
    }

    @Test
    void testDeleteComputation_ByAdminOfNet_Success() {
        Computation comp = processService.startNewComputation(rUid, netId);

        processService.deleteComputation(comp.getId(), aUid);

        assertNull(processService.getComputationById(comp.getId()));
    }

    @Test
    void testDeleteComputation_Unauthorized() {
        Computation comp = processService.startNewComputation(rUid, netId);

        // Creiamo un nuovo utente "intruso" al volo usando il costruttore reale
        User sneakyUser = new User("intruder@example.com", "password", Type.USER);
        String sUid = sneakyUser.getId();
        when(mockUserRepo.getUserById(sUid)).thenReturn(sneakyUser);

        assertThrows(UnauthorizedAccessException.class,
                () -> processService.deleteComputation(comp.getId(), sUid),
                "Unauthorized user should not be able to delete the computation");
    }
}