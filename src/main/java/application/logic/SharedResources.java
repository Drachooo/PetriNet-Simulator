package application.logic;

import application.repositories.PetriNetRepository;
import application.repositories.UserRepository;

/**
 * Provides global access to repositories.
 *
 */
public class SharedResources {

    // 1. The static instance of this class
    private static final SharedResources instance = new SharedResources();

    private final UserRepository userRepository;
    private final PetriNetRepository petriNetRepository;
    private final ProcessService processService;

    private SharedResources() {
        // Instantiated only once at the start of the application.
        this.userRepository = new UserRepository();
        this.petriNetRepository = new PetriNetRepository();
        this.processService = new ProcessService();
    }

    /**
     * 3. The public static method to get the single instance.
     */
    public static SharedResources getInstance() {
        return instance;
    }

    // --- Getters for the shared resources ---

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public PetriNetRepository getPetriNetRepository() {
        return petriNetRepository;
    }
}

}