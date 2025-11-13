package application.logic;

// Import per Jackson (JSON) e gestione file
import application.repositories.PetriNetRepository;
import application.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all business logic for running processes.
 */
public class ProcessService {

    // --- Dependencies (Tools) ---
    private final UserRepository userRepository;
    private final PetriNetRepository petriNetRepository;

    // --- Internal State (In-memory repository for all computations) ---
    private final Map<String, Computation> computations = new HashMap<>();

    // --- Persistence Tools ---
    private final ObjectMapper mapper = new ObjectMapper();
    private final File computationFile = new File("src/main/resources/data/computations.json");

    public ProcessService(UserRepository userRepository, PetriNetRepository petriNetRepository) {
        this.userRepository = userRepository;
        this.petriNetRepository = petriNetRepository;

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty-print JSON

        // Load all computations from disk
        loadComputationsFromFile();
    }

    /**
     * Loads the computation map from its JSON file on startup.
     */
    private void loadComputationsFromFile() {
        if(!computationFile.exists() || computationFile.length() == 0) {
            System.out.println("No computation file found. Starting fresh.");
            return;
        }
        try {
            // Read the JSON file and parse it into the map
            Map<String, Computation> loadedMap = mapper.readValue(computationFile,
                    mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Computation.class)
            );
            computations.putAll(loadedMap);
            System.out.println("Loaded computations: " + computations.size()); // User's original text
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the entire computation map to its JSON file.
     * We will call this every time we make a change.
     */
    private void saveComputationsToFile() {
        try{
            // Write the current in-memory map to the file
            mapper.writeValue(computationFile,computations);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }



}