package application.controllers;

import application.logic.User;
import application.logic.SharedResources;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the Help/Documentation view.
 * Manages the initialization of the view, responsive background,
 * and sidebar navigation based on user roles.
 */
public class HelpViewController implements Initializable {

    private User currentUser;
    private SharedResources sharedResources;

    @FXML private Button adminAreaButton;
    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundImage;
    @FXML private ImageView exampleImage;

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param location  The location used to resolve relative paths for the root object.
     * @param resources The resources used to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.sharedResources = SharedResources.getInstance();

        // Makes the background image responsive to the window size
        if (backgroundImage != null && rootStackPane != null) {
            backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());
            backgroundImage.setPreserveRatio(false);
        }
    }

    /**
     * Sets the current logged-in user and configures the UI components
     * according to the user's role and permissions.
     *
     * @param currentUser The user currently logged into the application.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;

        // Hides the Admin Area button if the user is not an administrator
        boolean isAdmin = currentUser.isAdmin();
        if (adminAreaButton != null) {
            adminAreaButton.setVisible(isAdmin);
            adminAreaButton.setManaged(isAdmin);
        }
    }

    // --- SIDEBAR NAVIGATION ---

    /**
     * Navigates to the Main Dashboard view.
     *
     * @param event The action event triggered by clicking the Dashboard button.
     * @throws IOException If the FXML file for the Main View cannot be loaded.
     */
    @FXML
    void goToMainView(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event, "/fxml/MainView.fxml", currentUser);
    }

    /**
     * Navigates to the Explore Nets view.
     *
     * @param event The action event triggered by clicking the Explore Nets button.
     * @throws IOException If the FXML file for the Explore Nets View cannot be loaded.
     */
    @FXML
    void goToExploreNets(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event, "/fxml/ExploreNetsView.fxml", currentUser);
    }

    /**
     * Navigates to the Admin Area view.
     * This button is only accessible to administrators.
     *
     * @param event The action event triggered by clicking the Admin Area button.
     * @throws IOException If the FXML file for the Admin Area View cannot be loaded.
     */
    @FXML
    void goToAdminArea(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event, "/fxml/AdminArea.fxml", currentUser);
    }

    /**
     * Logs out the current user and navigates back to the Login view.
     *
     * @param event The action event triggered by clicking the Logout button.
     * @throws IOException If the FXML file for the Login View cannot be loaded.
     */
    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event, "/fxml/LoginView.fxml");
    }
}