package application.logic;

public class SharedResources {
    private final UserRepository userRepository;
    private final PetriNetRepository petriNetRepository;

    public SharedResources() {
        userRepository=new UserRepository();
        petriNetRepository=new PetriNetRepository();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public PetriNetRepository getPetriNetRepository() {
        return petriNetRepository;
    }
}
