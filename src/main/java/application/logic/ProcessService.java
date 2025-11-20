package application.logic;

// Import per Jackson (JSON) e gestione file
import application.repositories.PetriNetRepository;
import application.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

    /*BUSINESS LOGIC*/


    /**
     * Implements Use Case 6.2.2: Start Computation.
     * Creates a new computation
     *
     * @param userId ID of user that starts the process
     * @param netId ID of PetriNet
     * @return created computation
     * @throws IllegalStateException if rule is violated
     */
    public Computation startNewComputation(String userId, String netId) throws IllegalStateException {
        User user=userRepository.getUserById(userId);
        PetriNet net=petriNetRepository.getPetriNets().get(netId);

        if(user==null) throw new IllegalStateException("User not found");
        if (net==null) throw new IllegalStateException("PetriNet not found");

        if(user.isAdmin() && net.getAdminId().equals(user.getId()))
            throw new IllegalStateException("Admin cannot start computation of his own net");

        boolean hasActive=computations.values().stream().anyMatch(c->c.getUserId().equals(user.getId()) && c.getPetriNetId().equals(netId) && c.isActive());

        if(hasActive) {
            throw new IllegalStateException("User already has an active computation for this net");
        }

        /*6.2.2*/
        Computation newComp= new Computation(netId,userId);

        MarkingData initialMarking=new MarkingData();
        String initialPlaceId=net.getInitialPlaceId();

        if(initialPlaceId==null) {
            throw new IllegalStateException("Could not start net: initialPlace was not defined");
        }
        /*Rule 1.1.3 "exactly one token"*/
        initialMarking.setTokens(initialPlaceId,1);

        ComputationStep initialStep=new ComputationStep(newComp.getId(),null,initialMarking);

        newComp.addStep(initialStep);
        computations.put(newComp.getId(), newComp);
        saveComputationsToFile();

        return newComp;

    }

    /**
     * Implements Use Case 6.2.3: Execute Transition.
     * Fires a transition
     *
     * @param computationId ID of computation to update.
     * @param transitionId ID of transition fo fire.
     * @param userId ID of user that wants to fire.
     * @throws IllegalStateException if any rule is violated.
     */
    public void fireTransition(String computationId, String transitionId, String userId) throws IllegalStateException{
        Computation comp=computations.get(computationId);

        if(comp==null) throw new IllegalStateException("Computation not found");

        User user=userRepository.getUserById(userId);
        if(user==null) throw new IllegalStateException("User not found");

        PetriNet net=petriNetRepository.getPetriNets().get(comp.getPetriNetId());
        if(net==null) throw new IllegalStateException("PetriNet not found");

        Transition transition=net.getTransitions().get(transitionId);
        if(transition==null) throw new IllegalStateException("Transition not found");

        /*Computation must be active*/
        if(!comp.isActive()) throw new IllegalStateException("Computation is not active");

        /*User must have permission (owner or admin of net)*/
        // (Req 5.4: "Users shall be able to delete their own computations")
        // (Req 5.3: "Administrators shall be able to delete any computation related to their Petri nets")
        boolean isOwner=comp.getUserId().equals(userId);
        boolean isAdmin=user.isAdmin() && net.getAdminId().equals(user.getId());

        if(!isOwner && !isAdmin) {
            throw new IllegalStateException("User is not owner or admin");
        }

        checkFirePermissions(user,net,transition);


        MarkingData curr=comp.getLastStep().getMarkingData();

        if(!net.isEnabled(transitionId,curr)){
            throw new IllegalStateException("Transition is not enabled (insuff token)");
        }

        MarkingData newMarking= net.fire(transitionId,curr);

        /*6.2.3: new marking is recorded with timestamp*/
        ComputationStep newStep=new ComputationStep(comp.getId(),transitionId,newMarking);
        comp.addStep(newStep);

        String finalPlaceId=net.getFinalPlaceId();
        if(finalPlaceId!=null && newMarking.getTokens(finalPlaceId)>0) {
            comp.completeComputation();
        }

        saveComputationsToFile();
    }

    /**
     * Implements rule 2.3.
     * Uses strategy pattern.
     * @throws IllegalStateException if user cannot fire transition
     *
     */
    private void checkFirePermissions(User user, PetriNet net, Transition transition) {
        Type transitionType=transition.getType();
        TransitionExecutionStrategy strategy;

        if(transitionType==Type.ADMIN){
            /*2.3: Administrator transitions can only be fired by the administrator who created the Petri net*/
           strategy=new AdminExecutionStrategy();
        }
        /*2.3:  User transitions can only be fired by users (non admin) who have created a computation instance*/
        else{
            strategy=new UserExecutionStrategy();
        }

        strategy.checkPermissions(user,net,transition);
    }

    /**
     *Implements requirements FR4.1, 4.2, 4.3.
     * Gets a list of all transitions that the given user can currently fire
     * in a specific computation.
     *
     * @param computationId The ID of the active computation.
     * @param userId The ID of the user asking.
     * @return A List of Transition objects that are both enabled AND permitted for the user.
     */
    public List<Transition> getAvailableTransitions(String computationId, String userId) {
        Computation comp=computations.get(computationId);
        User user=userRepository.getUserById(userId);

        if(comp==null || user==null || !comp.isActive())
            return new ArrayList<>();

        PetriNet net=petriNetRepository.getPetriNets().get(comp.getPetriNetId());
        if(net==null)
            return new ArrayList<>();

        MarkingData curr=comp.getLastStep().getMarkingData();

        List<Transition> available=new ArrayList<>();

        for(Transition t: net.getTransitions().values()) {
            boolean isEnabled=net.isEnabled(t.getId(),curr);

            if(isEnabled) {
                try{
                    checkFirePermissions(user,net,t);
                    available.add(t);
                }catch(IllegalStateException e){}
            }
        }
        return available;
    }

    /*UI HELPER METHOIDS*/

    /**
     * Implements Use Case 5.3 and 5.4: Delete Computation.
     *
     * @param computationId ID of computation to delete
     * @param userId ID of user that wants to delete computation
     * @throws IllegalStateException if user does not have permissions.
     */

    public void deleteComputation(String computationId, String userId) throws IllegalStateException {
        Computation comp=computations.get(computationId);
        if(comp==null) return;

        User user=userRepository.getUserById(userId);
        if(user==null) throw new IllegalStateException("User not found");

        PetriNet net=petriNetRepository.getPetriNets().get(comp.getPetriNetId());

        //FR 5.4: "Users shall be able to delete their own computations"
        boolean isOwner=comp.getUserId().equals(userId);

        //FR5.3: "Admins shall be able to delete any computation related to their PEtri nets"
        boolean isAdminOfNet= net != null && user.isAdmin();

        if(isOwner || isAdminOfNet) {
            computations.remove(computationId);
            saveComputationsToFile();
        }else{
            throw new IllegalStateException("User is not owner or admin");
        }
    }

    /**
     * Implements Use Case 6.1.2: Manage Computations.
     * Obtains computations of all users on the nets created by an admin.
     * Useful for "Administrator Dashboard".
     */
    public List<Computation> getComputationsForAdmin(String adminId) {
        return computations.values().stream().filter(c->{
            PetriNet net=petriNetRepository.getPetriNets().get(c.getPetriNetId());

            return net!=null && net.getAdminId().equals(adminId);
        }).collect(Collectors.toList());
    }

    /**
     * Implements Use case 6.2.2
     *
     * @param userId L'ID dell'utente.
     * @return Una lista di Computazioni.
     */
    public List<Computation> getComputationsForUser(String userId) {
        return computations.values().stream()
                .filter(c -> c.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Implements Use Case 6.2.1: Subscribe to Process.
     * Obtains nets to which it can subscribe (all except his/her own).
     * Useful for "User Dashboard".
     */
    public List<PetriNet> getAvailableNetsForUser(String userId) {
        User user=userRepository.getUserById(userId);
        if(user == null) return new ArrayList<>();

        //FR 2.1 and 2.2: An admin cannot subscribe to his own net :)
        return petriNetRepository.getPetriNets().values().stream()
                .filter(n->!n.getAdminId().equals(userId)).collect(Collectors.toList());
    }

    /**
     * Obtains computation by its ID
     */
    public Computation getComputationById(String computationId) {
        return computations.get(computationId);
    }






}