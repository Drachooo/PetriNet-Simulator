package application.logic;

public class SharedResources {
    private final UserRepository userRepository;
    private final PetriNetRepository petriNetRepository;
    private User currentUser;

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

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }
}
