package application.controllers;

import application.logic.SharedResources;
import application.logic.User;
import application.repositories.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.animation.KeyFrame;
import javafx.scene.image.ImageView;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the login-view.fxml screen.
 * Handles user authentication and navigation to the main app or registration.
 */
public class LoginViewController implements Initializable {

    private SharedResources sharedResources;
    private UserRepository userRepository;

    // FXML Components
    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordFieldHidden;
    @FXML private TextField passwordTextVisible;

    @FXML private Label errorLabel;

    // Keep track of password state (visible / not visible)
    private boolean isPasswordVisible = false;

    // Eye images
    @FXML private ImageView eyeOpen;
    @FXML private ImageView eyeClosed;

    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundImage;

    // The timer hides the label when it finishes
    private final Timeline errorClearer = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (errorLabel != null) {
                    errorLabel.setVisible(false); // HIDES THE LABEL and the button below becomes clickable again
                }
            })
    );

    /**
     * Called by JavaFX when the FXML is loaded.
     * Initializes services and controllers.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.sharedResources = SharedResources.getInstance();
        this.userRepository = sharedResources.getUserRepository();

        if(backgroundImage != null && rootStackPane != null) {
            backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());

            backgroundImage.setPreserveRatio(false);
        }


        if(errorLabel != null) {
            errorLabel.setVisible(false); // Starts hidden
            errorLabel.setText("");
        }

        // To be able to write in 2 fields simultaneously, visible password and invisible password
        if(passwordFieldHidden != null && passwordTextVisible != null) {
            passwordTextVisible.textProperty().bindBidirectional(passwordFieldHidden.textProperty());
        }
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if(isPasswordVisible){
            // Shows the visible password text field
            passwordTextVisible.setVisible(true);
            passwordFieldHidden.setVisible(false);

            if(eyeOpen != null){
                eyeOpen.setVisible(true);
            }
            if(eyeClosed != null){
                eyeClosed.setVisible(false);
            }
        }else {
            // Hides the visible password text field
            passwordTextVisible.setVisible(false);
            passwordFieldHidden.setVisible(true);

            if(eyeOpen != null){
                eyeOpen.setVisible(false);
            }
            if(eyeClosed != null){
                eyeClosed.setVisible(true);
            }
        }
    }



    /**
     * Handles the 'Login' button click event.
     * Validates credentials and navigates to the main view if success.
     * @param event The button click event.
     * @throws Exception If navigation to the main view fails.
     */
    @FXML
    private void handleLogin(ActionEvent event) throws Exception {
        String email = emailTextField.getText();
        String password = passwordFieldHidden.getText();

        if(email.isEmpty() || password.isEmpty()) {
            showError("Email and password cannot be empty");
            return;
        }

        if (userRepository.isEmailAvailable(email)) {
            showError("This email address is not registered");
            return;
        }

        if (userRepository.checkCorrectCredentials(email, password)) {
            // Success: Get the user and pass it to the next view
            User user = userRepository.getUserByEmail(email);
            goToMainView(event, user);
        } else {
            // Failure
            showError("Incorrect email or password");
        }

        passwordFieldHidden.clear();
    }

    /**
     * Handles the 'Register' button click event.
     * Navigates to the registration view.
     * @param event The button click event.
     * @throws IOException If loading RegisterView.fxml fails.
     */
    @FXML
    private void goToRegisterView(ActionEvent event) throws IOException {
        // Uses the helper
        NavigationHelper.navigate(event, "/fxml/RegisterView.fxml");
    }

    /**
     * Navigates to the MainView after a successful login.
     * This method passes the authenticated User object to the MainViewController.
     *
     * @param event The original login button event.
     * @param currentUser The authenticated User object (NOT from a global singleton).
     * @throws Exception If loading MainView.fxml fails.
     */
    @FXML
    private void goToMainView(ActionEvent event, User currentUser) throws Exception {
        NavigationHelper.navigate(event,"/fxml/MainView.fxml",currentUser);
    }

    /**
     * Displays an error message in the UI label.
     * @param message The error message to display.
     */
    private void showError(String message){
        if(errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);

            errorClearer.stop();
            errorClearer.playFromStart();
        }
        else{
            System.err.println("errorLabel not found in FXML file");
        }
    }
}