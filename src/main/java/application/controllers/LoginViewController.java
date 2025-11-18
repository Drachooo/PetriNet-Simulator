package application.controllers;

import application.logic.SharedResources;
import application.logic.User;
import application.repositories.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
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
    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Label errorLabel;

    private final Timeline errorClearer = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                // L'azione da fare dopo 3 secondi (cancellare il testo)
                // Sarà gestita nel metodo showError
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

        if(errorLabel != null) {
            errorLabel.setText("");
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
        String password = passwordTextField.getText();

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

        passwordTextField.clear();
    }

    /**
     * Handles the 'Register' button click event.
     * Navigates to the registration view.
     * @param event The button click event.
     * @throws IOException If loading RegisterView.fxml fails.
     */
    @FXML
    private void goToRegisterView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegisterView.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent mainPage = loader.load();

        MainViewController controller = loader.getController();
        // Pass services, stage, and the specific user to the next controller
        controller.setSharedResources(sharedResources);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());
        controller.setCurrentUser(currentUser);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(mainPage));
        stage.show();
    }

    /**
     * Displays an error message in the UI label.
     * @param message The error message to display.
     */
    private void showError(String message){
        if(errorLabel != null) {
            errorLabel.setText(message);
            errorClearer.stop();
            errorClearer.playFromStart();
        }
        else{
            System.err.println("errorLabel not found in FXML file");
        }
    }
}