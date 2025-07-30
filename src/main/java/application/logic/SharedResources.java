package application.logic;

import application.repositories.CoordinatesRepository;
import application.repositories.PetriNetRepository;
import application.repositories.UserRepository;

public class SharedResources {
    private final UserRepository userRepository;
    private final PetriNetRepository petriNetRepository;
    private final CoordinatesRepository coordinatesRepository;
    private User currentUser;

    public SharedResources() {
        this.coordinatesRepository = new CoordinatesRepository();
        userRepository=new UserRepository();
        petriNetRepository=new PetriNetRepository();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public PetriNetRepository getPetriNetRepository() {
        return petriNetRepository;
    }

    public CoordinatesRepository getCoordinatesRepository() {return coordinatesRepository;}

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }
}
