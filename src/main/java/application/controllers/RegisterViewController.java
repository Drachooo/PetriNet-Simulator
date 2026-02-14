package application.controllers;

import application.logic.SharedResources;
import application.logic.Type;
import application.logic.User;
import application.repositories.UserRepository;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the user registration view.
 * Handles user input validation, password visibility toggling, and new user creation.
 */
public class RegisterViewController implements Initializable {

    private SharedResources sharedResources;
    private UserRepository userRepository;

    // FXML Components
    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField passwordFieldHidden;
    @FXML
    private TextField passwordTextVisible;
    @FXML
    private PasswordField confirmPasswordFieldHidden;
    @FXML
    private TextField confirmPasswordFieldVisible;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private StackPane rootStackPane;
    @FXML
    private Label errorLabel;

    // Eye icons for main password field
    @FXML
    private ImageView eyeOpenPassword;
    @FXML
    private ImageView eyeClosedPassword;

    // Eye icons for confirm password field
    @FXML
    private ImageView eyeOpenConfirm;
    @FXML
    private ImageView eyeClosedConfirm;

    // Track password visibility state
    private boolean isMainPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // Timeline to auto-hide error messages after 3 seconds
    private final Timeline errorClearer = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (errorLabel != null) {
                    errorLabel.setVisible(false); // Hides the label and button below becomes clickable again
                }
            })
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.sharedResources = SharedResources.getInstance();
        this.userRepository = sharedResources.getUserRepository();

        if (backgroundImage != null && rootStackPane != null) {
            backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());
            backgroundImage.setPreserveRatio(false);
        }

        if (errorLabel != null) {
            errorLabel.setVisible(false); // Starts hidden
            errorLabel.setText("");
        }

        // Bind visible and hidden password fields for main password
        if (passwordFieldHidden != null && passwordTextVisible != null) {
            passwordTextVisible.textProperty().bindBidirectional(passwordFieldHidden.textProperty());
        }

        // Bind visible and hidden password fields for confirm password
        if (confirmPasswordFieldHidden != null && confirmPasswordFieldVisible != null) {
            confirmPasswordFieldVisible.textProperty().bindBidirectional(confirmPasswordFieldHidden.textProperty());
        }
    }

    /**
     * Toggles visibility of the main password field.
     * Switches between PasswordField (hidden) and TextField (visible).
     *
     * @param event The action event
     */
    @FXML
    private void toggleMainPassword(ActionEvent event) {
        isMainPasswordVisible = !isMainPasswordVisible;

        if (isMainPasswordVisible) {
            // Show password
            if (passwordTextVisible != null) passwordTextVisible.setVisible(true);
            if (passwordFieldHidden != null) passwordFieldHidden.setVisible(false);

            // Icons: open visible, closed hidden
            if (eyeOpenPassword != null) eyeOpenPassword.setVisible(true);
            if (eyeClosedPassword != null) eyeClosedPassword.setVisible(false);
        } else {
            // Hide password
            if (passwordTextVisible != null) passwordTextVisible.setVisible(false);
            if (passwordFieldHidden != null) passwordFieldHidden.setVisible(true);

            // Icons: open hidden, closed visible
            if (eyeOpenPassword != null) eyeOpenPassword.setVisible(false);
            if (eyeClosedPassword != null) eyeClosedPassword.setVisible(true);
        }
    }

    /**
     * Toggles visibility of the confirm password field.
     * Switches between PasswordField (hidden) and TextField (visible).
     *
     * @param event The action event
     */
    @FXML
    private void toggleConfirmPassword(ActionEvent event) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            // Show password
            if (confirmPasswordFieldVisible != null) confirmPasswordFieldVisible.setVisible(true);
            if (confirmPasswordFieldHidden != null) confirmPasswordFieldHidden.setVisible(false);

            // Icons: open visible, closed hidden
            if (eyeOpenConfirm != null) eyeOpenConfirm.setVisible(true);
            if (eyeClosedConfirm != null) eyeClosedConfirm.setVisible(false);
        } else {
            // Hide password
            if (confirmPasswordFieldVisible != null) confirmPasswordFieldVisible.setVisible(false);
            if (confirmPasswordFieldHidden != null) confirmPasswordFieldHidden.setVisible(true);

            // Icons: open hidden, closed visible
            if (eyeOpenConfirm != null) eyeOpenConfirm.setVisible(false);
            if (eyeClosedConfirm != null) eyeClosedConfirm.setVisible(true);
        }
    }

    /**
     * Navigates back to the login view.
     *
     * @param event The action event
     * @throws Exception if navigation fails
     */
    @FXML
    private void goToLoginView(ActionEvent event) throws Exception {
        NavigationHelper.navigate(event, "/fxml/LoginView.fxml");
    }

    /**
     * Handles user registration.
     * Validates input fields, checks password match, verifies email availability,
     * and creates a new user account.
     *
     * @param event The action event
     * @throws Exception if navigation fails after successful registration
     */
    @FXML
    private void handleRegister(ActionEvent event) throws Exception {
        String email = emailTextField.getText();
        String password = passwordFieldHidden.getText();
        String confirmPassword = confirmPasswordFieldHidden.getText();

        // Validation
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!userRepository.isEmailValid(email)) {
            showError("Insert a valid email address.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        if (!userRepository.isEmailAvailable(email)) {
            showError("This email address is already in use.");
            return;
        }

        User newUser = new User(email, password, Type.USER);
        userRepository.saveUser(newUser);

        goToLoginView(event);
    }

    /**
     * Displays an error message in the UI label.
     * The message auto-hides after 3 seconds.
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);

            errorClearer.stop();
            errorClearer.playFromStart();
        } else {
            System.err.println("errorLabel not found in FXML file");
        }
    }
}