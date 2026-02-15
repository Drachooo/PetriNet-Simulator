package application.logic;

// Imports for Jackson (JSON) and file management
import application.repositories.PetriNetCoordinates;
import application.repositories.PetriNetRepository;
import application.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import application.exceptions.UnauthorizedAccessException;
import application.exceptions.TransitionNotEnabledException;
import application.exceptions.EntityNotFoundException;
import application.exceptions.InvalidComputationStateException;
import application.exceptions.ActiveComputationExistsException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final File computationFile = new File("data/computations.json");

    /**
     * Initializes the service, configures Jackson for serialization,
     * and loads existing computations from disk.
     *
     * @param userRepository     The user repository instance.
     * @param petriNetRepository The Petri net repository instance.
     */
    public ProcessService(UserRepository userRepository, PetriNetRepository petriNetRepository) {
        this.userRepository = userRepository;
        this.petriNetRepository = petriNetRepository;

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

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
            Map<String, Computation> loadedMap = mapper.readValue(computationFile,
                    mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Computation.class)
            );
            computations.putAll(loadedMap);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the entire computation map to its JSON file.
     * Called automatically after every state-changing operation.
     */
    private void saveComputationsToFile() {
        try{
            mapper.writeValue(computationFile,computations);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* --- BUSINESS LOGIC --- */

    /**
     * Implements Use Case 6.2.2: Start Computation.
     * Creates a new computation saving a deep cloned snapshot of the current network.
     *
     * @param userId ID of user that starts the process.
     * @param netId  ID of PetriNet.
     * @return The created computation instance.
     * @throws IllegalStateException if other business rules are violated.
     * @throws UnauthorizedAccessException if the user does not have permissions.
     * @throws EntityNotFoundException if the user or the net is not found.
     * @throws ActiveComputationExistsException if the user already has an active computation for this net.
     * @throws InvalidComputationStateException if the initial place is not defined.
     */
    public Computation startNewComputation(String userId, String netId) throws IllegalStateException {
        User user = userRepository.getUserById(userId);
        PetriNet originalNet = petriNetRepository.getPetriNets().get(netId);

        if(user == null) throw new EntityNotFoundException("User not found");
        if(originalNet == null) throw new EntityNotFoundException("PetriNet not found");

        if(user.isAdmin() && originalNet.getAdminId().equals(user.getId()))
            throw new UnauthorizedAccessException("Admin cannot start computation of his own net");

        boolean hasActive = computations.values().stream()
                .anyMatch(c -> c.getUserId().equals(user.getId()) && c.getPetriNetId().equals(netId) && c.isActive());

        if(hasActive) {
            throw new ActiveComputationExistsException("User already has an active computation for this net");
        }

        // We serialize and deserialize to create a completely detached clone in memory.
        PetriNet clonedNetSnapshot;
        try {
            String jsonFormat = mapper.writeValueAsString(originalNet);
            clonedNetSnapshot = mapper.readValue(jsonFormat, PetriNet.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create a reliable snapshot of the Petri Net.", e);
        }

        // Load the current coordinates to freeze them in the snapshot
        PetriNetCoordinates currentCoords;
        try {
            currentCoords = PetriNetCoordinates.loadFromFile("data/coords/" + netId + "_coords.json");
        } catch (IOException e) {
            currentCoords = new PetriNetCoordinates(); // Fallback if no layout exists
        }

        // Pass the CLONED net and the detached coordinates to the new computation
        Computation newComp = new Computation(clonedNetSnapshot, currentCoords, userId);

        MarkingData initialMarking = new MarkingData();
        String initialPlaceId = clonedNetSnapshot.getInitialPlaceId();

        if(initialPlaceId == null) {
            throw new InvalidComputationStateException("Could not start net: initialPlace was not defined");
        }

        // Rule 1.1.3 "exactly one token" in the initial place
        initialMarking.setTokens(initialPlaceId, 1);

        ComputationStep initialStep = new ComputationStep(newComp.getId(), null, initialMarking);

        newComp.addStep(initialStep);
        computations.put(newComp.getId(), newComp);
        saveComputationsToFile();

        return newComp;
    }

    /**
     * Implements Use Case 6.2.3: Execute Transition.
     * Fires a transition using the snapshot stored in the computation.
     *
     * @param computationId ID of computation to update.
     * @param transitionId  ID of transition to fire.
     * @param userId        ID of user that wants to fire.
     * @throws IllegalStateException if the state does not allow it.
     * @throws UnauthorizedAccessException if the user does not have permissions.
     * @throws TransitionNotEnabledException if the transition does not have enough tokens.
     * @throws EntityNotFoundException if the computation, user, net, or transition is not found.
     * @throws InvalidComputationStateException if the computation is not active.
     */
    public void fireTransition(String computationId, String transitionId, String userId) throws IllegalStateException{
        Computation comp = computations.get(computationId);
        if(comp == null) throw new EntityNotFoundException("Computation not found");

        User user = userRepository.getUserById(userId);
        if(user == null) throw new EntityNotFoundException("User not found");

        // Use the snapshot stored in the computation
        PetriNet net = comp.getPetriNetSnapshot();
        if(net == null) throw new EntityNotFoundException("PetriNet snapshot not found in computation");

        Transition transition = net.getTransitions().get(transitionId);
        if(transition == null) throw new EntityNotFoundException("Transition not found");

        if(!comp.isActive()) throw new InvalidComputationStateException("Computation is not active");

        // (Req 5.4: "Users shall be able to delete their own computations")
        // (Req 5.3: "Administrators shall be able to delete any computation related to their Petri nets")
        boolean isOwner = comp.getUserId().equals(userId);
        boolean isAdmin = user.isAdmin() && net.getAdminId().equals(user.getId());

        if(!isOwner && !isAdmin) {
            throw new UnauthorizedAccessException("User is not owner or admin");
        }

        checkFirePermissions(user, net, transition);

        MarkingData curr = comp.getLastStep().getMarkingData();

        if(!net.isEnabled(transitionId, curr)){
            throw new TransitionNotEnabledException("Transition is not enabled (insufficient tokens)");
        }

        MarkingData newMarking = net.fire(transitionId, curr);

        // 6.2.3: New marking is recorded with a timestamp
        ComputationStep newStep = new ComputationStep(comp.getId(), transitionId, newMarking);
        comp.addStep(newStep);

        String finalPlaceId = net.getFinalPlaceId();
        if(finalPlaceId != null && newMarking.getTokens(finalPlaceId) > 0) {
            comp.completeComputation();
        }

        saveComputationsToFile();
    }

    /**
     * Implements rule 2.3.
     * Uses strategy pattern to verify transition firing permissions.
     *
     * @param user       The user attempting to fire the transition.
     * @param net        The Petri net snapshot being executed.
     * @param transition The transition to be fired.
     * @throws IllegalStateException if user cannot fire transition.
     */
    private void checkFirePermissions(User user, PetriNet net, Transition transition) {
        Type transitionType = transition.getType();
        TransitionExecutionStrategy strategy;

        if(transitionType == Type.ADMIN){
            // 2.3: Administrator transitions can only be fired by the administrator who created the Petri net
            strategy = new AdminExecutionStrategy();
        } else {
            // 2.3: User transitions can only be fired by users (non admin) who have created a computation instance
            strategy = new UserExecutionStrategy();
        }

        strategy.checkPermissions(user, net, transition);
    }

    /**
     * Implements requirements FR4.1, 4.2, 4.3.
     * Gets a list of all transitions that the given user can currently fire
     * in a specific computation.
     *
     * @param computationId The ID of the active computation.
     * @param userId        The ID of the user asking.
     * @return A List of Transition objects that are both enabled AND permitted for the user.
     */
    public List<Transition> getAvailableTransitions(String computationId, String userId) {
        Computation comp = computations.get(computationId);
        User user = userRepository.getUserById(userId);

        if(comp == null || user == null || !comp.isActive())
            return new ArrayList<>();

        //Use the snapshot stored in the computation
        PetriNet net = comp.getPetriNetSnapshot();
        if(net == null)
            return new ArrayList<>();

        MarkingData curr = comp.getLastStep().getMarkingData();

        List<Transition> available = new ArrayList<>();

        for(Transition t: net.getTransitions().values()) {
            boolean isEnabled = net.isEnabled(t.getId(), curr);

            if(isEnabled) {
                try{
                    checkFirePermissions(user, net, t);
                    available.add(t);
                }catch(IllegalStateException | UnauthorizedAccessException e){
                    // Intentionally ignore: transition is enabled mechanically but user lacks permissions
                }
            }
        }
        return available;
    }

    /**
     * Returns ALL transitions mechanically enabled by the current marking,
     * ignoring user permissions. Used to check if there is an ADMIN transition waiting.
     *
     * @param computationId ID of the computation.
     * @return List of enabled transitions.
     */
    public List<Transition> getEnabledTransitions(String computationId) {
        Computation comp = computations.get(computationId);

        if (comp == null || !comp.isActive()) {
            return new ArrayList<>();
        }

        // --- UPDATED: Use the snapshot stored in the computation ---
        PetriNet net = comp.getPetriNetSnapshot();
        if (net == null) {
            return new ArrayList<>();
        }

        MarkingData currentMarking = comp.getLastStep().getMarkingData();

        return net.getTransitions().values().stream()
                .filter(t -> net.isEnabled(t.getId(), currentMarking))
                .collect(Collectors.toList());
    }

    /* --- UI HELPER METHODS --- */

    /**
     * Implements Use Case 5.3 and 5.4: Delete Computation.
     *
     * @param computationId ID of computation to delete.
     * @param userId        ID of user that wants to delete computation.
     * @throws IllegalStateException if the state does not allow it.
     * @throws UnauthorizedAccessException if the user does not have permissions.
     * @throws EntityNotFoundException if the user is not found.
     */
    public void deleteComputation(String computationId, String userId) throws IllegalStateException {
        Computation comp = computations.get(computationId);
        if(comp == null) return;

        User user = userRepository.getUserById(userId);
        if(user == null) throw new EntityNotFoundException("User not found");

        // Here we still check the live repository to see who currently owns the net template
        PetriNet net = petriNetRepository.getPetriNets().get(comp.getPetriNetId());

        // FR 5.4: "Users shall be able to delete their own computations"
        boolean isOwner = comp.getUserId().equals(userId);

        // FR 5.3: "Admins shall be able to delete any computation related to their Petri nets"
        boolean isAdminOfNet = net != null && user.isAdmin() && net.getAdminId().equals(user.getId());

        if(isOwner || isAdminOfNet) {
            computations.remove(computationId);
            saveComputationsToFile();
        } else {
            throw new UnauthorizedAccessException("User is not owner or admin of this computation");
        }
    }

    /**
     * Implements Use Case 6.1.2: Manage Computations.
     * Obtains computations of all users on the nets created by an admin.
     * Useful for "Administrator Dashboard".
     *
     * @param adminId The ID of the administrator.
     * @return A list of computations belonging to the administrator's nets.
     */
    public List<Computation> getComputationsForAdmin(String adminId) {
        return computations.values().stream().filter(c -> {
            PetriNet net = petriNetRepository.getPetriNets().get(c.getPetriNetId());
            return net != null && net.getAdminId().equals(adminId);
        }).collect(Collectors.toList());
    }

    /**
     * Implements Use case 6.2.2.
     *
     * @param userId The user ID.
     * @return A list of computations started by the specified user.
     */
    public List<Computation> getComputationsForUser(String userId) {
        return computations.values().stream()
                .filter(c -> c.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Implements Use Case 6.2.1: Subscribe to Process.
     * Obtains nets to which a user can subscribe (all except his/her own if admin).
     * Useful for "User Dashboard".
     *
     * @param userId The user ID requesting available networks.
     * @return A list of available PetriNet templates.
     */
    public List<PetriNet> getAvailableNetsForUser(String userId) {
        User user = userRepository.getUserById(userId);
        if(user == null) return new ArrayList<>();

        // FR 2.1 and 2.2: An admin cannot subscribe to his own net
        return petriNetRepository.getPetriNets().values().stream()
                .filter(n -> !n.getAdminId().equals(userId)).collect(Collectors.toList());
    }

    /**
     * Obtains a specific computation by its unique ID.
     *
     * @param computationId The ID of the computation to retrieve.
     * @return The Computation object, or null if not found.
     */
    public Computation getComputationById(String computationId) {
        return computations.get(computationId);
    }
}