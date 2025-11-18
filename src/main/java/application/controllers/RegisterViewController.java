    package application.controllers;

    import application.logic.SharedResources;
    import application.logic.Type;
    import application.logic.User;
    import application.repositories.UserRepository;
    import javafx.animation.KeyFrame;
    import javafx.animation.Timeline;
    import javafx.scene.Node;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.*;

    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.fxml.Initializable;
    import javafx.stage.Stage;
    import javafx.util.Duration;

    import java.net.URL;
    import java.util.ResourceBundle;

    public class RegisterViewController implements Initializable {


        private SharedResources sharedResources;
        private UserRepository userRepository;

        /*FXML COMPONENTS*/
        @FXML
        private TextField emailTextField;
        @FXML
        private PasswordField passwordTextField;
        @FXML
        private PasswordField confirmPasswordTextField;
        @FXML
        private Label errorLabel;

        private final Timeline errorClearer = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> {
                    if (errorLabel != null) {
                        errorLabel.setText("");
                    }
                })
        );

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            this.sharedResources = SharedResources.getInstance();
            this.userRepository = sharedResources.getUserRepository();

            if (errorLabel != null) {
                errorLabel.setText("");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
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
            String password = passwordTextField.getText();
            String confirmPassword = confirmPasswordTextField.getText();

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
                errorClearer.stop();
                errorClearer.playFromStart();
            }
            else{
                System.err.println("errorLabel not found in FXML file");
            }
        }
    }