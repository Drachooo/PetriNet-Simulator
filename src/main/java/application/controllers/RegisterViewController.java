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

public class RegisterViewController implements Initializable {


    private SharedResources sharedResources;
    private UserRepository userRepository;

    /*FXML COMPONENTS*/
    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordFieldHidden;
    @FXML private TextField passwordTextVisible;

    @FXML
    private PasswordField confirmPasswordFieldHidden;
    @FXML
    private TextField confirmPasswordFieldVisible;

    @FXML private ImageView backgroundImage;
    @FXML private StackPane rootStackPane;

    //Tenere traccia dello stato della password(visibile / non visibile)
    private boolean isMainPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @FXML
    private Label errorLabel;

    //Immagini occhio
    @FXML private ImageView eyeOpenPassword;
    @FXML private ImageView eyeClosedPassword;

    //Immagini occhio CONFIRM
    @FXML private ImageView eyeOpenConfirm;
    @FXML private ImageView eyeClosedConfirm;

    private final Timeline errorClearer = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (errorLabel != null) {
                    errorLabel.setVisible(false); // NASCONDE LA LABEL e Il bottone sotto torna cliccabile
                }
            })
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.sharedResources = SharedResources.getInstance();
        this.userRepository = sharedResources.getUserRepository();

        if(backgroundImage != null && rootStackPane != null) {
            backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());

            backgroundImage.setPreserveRatio(false);
        }

        if (errorLabel != null) {
            errorLabel.setVisible(false); // Parte nascosta
            errorLabel.setText("");
        }

        //BINDING password
        if(passwordFieldHidden != null && passwordTextVisible != null) {
            passwordTextVisible.textProperty().bindBidirectional(passwordFieldHidden.textProperty());
        }

        //BINDING confirm password
        if(confirmPasswordFieldHidden != null && confirmPasswordFieldVisible != null) {
            confirmPasswordFieldVisible.textProperty().bindBidirectional(confirmPasswordFieldHidden.textProperty());
        }

    }

    @FXML
    private void toggleMainPassword(ActionEvent event) {
        isMainPasswordVisible = !isMainPasswordVisible;

        if (isMainPasswordVisible) {
            // MOSTRA
            if(passwordTextVisible != null) passwordTextVisible.setVisible(true);
            if(passwordFieldHidden != null) passwordFieldHidden.setVisible(false);

            // Icone: APERTO visibile, CHIUSO nascosto
            if(eyeOpenPassword != null) eyeOpenPassword.setVisible(true);
            if(eyeClosedPassword != null) eyeClosedPassword.setVisible(false);
        } else {
            // NASCONDI
            if(passwordTextVisible != null) passwordTextVisible.setVisible(false);
            if(passwordFieldHidden != null) passwordFieldHidden.setVisible(true);

            // Icone: APERTO nascosto, CHIUSO visibile
            if(eyeOpenPassword != null) eyeOpenPassword.setVisible(false);
            if(eyeClosedPassword != null) eyeClosedPassword.setVisible(true);
        }
    }

    /**
     * GESTIONE PASSWORD DI CONFERMA (Confirm)
     */
    @FXML
    private void toggleConfirmPassword(ActionEvent event) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            // MOSTRA
            if(confirmPasswordFieldVisible != null) confirmPasswordFieldVisible.setVisible(true);
            if(confirmPasswordFieldHidden != null) confirmPasswordFieldHidden.setVisible(false);

            // Icone: APERTO visibile, CHIUSO nascosto
            if(eyeOpenConfirm != null) eyeOpenConfirm.setVisible(true);
            if(eyeClosedConfirm != null) eyeClosedConfirm.setVisible(false);
        } else {
            // NASCONDI
            if(confirmPasswordFieldVisible != null) confirmPasswordFieldVisible.setVisible(false);
            if(confirmPasswordFieldHidden != null) confirmPasswordFieldHidden.setVisible(true);

            // Icone: APERTO nascosto, CHIUSO visibile
            if(eyeOpenConfirm != null) eyeOpenConfirm.setVisible(false);
            if(eyeClosedConfirm != null) eyeClosedConfirm.setVisible(true);
        }
    }

    /**
     * Handles click on button "BACK TO LOGIN"
     * Goes back to login page
     *
     * @param event
     * @throws Exception
     */
    @FXML
    private void goToLoginView(ActionEvent event) throws Exception {
        //Utilizzo l'helper
        NavigationHelper.navigate(event, "/fxml/LoginView.fxml");
    }



    /**
     * Handles click on button "REGISTER"
     * Validates input and creates new user
     *
     * @param event
     * @throws Exception
     */
    @FXML
    private void handleRegister(ActionEvent event) throws Exception {
        String email = emailTextField.getText();
        String password = passwordFieldHidden.getText();
        String confirmPassword = confirmPasswordFieldHidden.getText();

        // --- VALIDATION ---
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