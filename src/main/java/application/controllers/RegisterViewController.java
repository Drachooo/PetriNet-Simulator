    package application.controllers;

    import application.logic.SharedResources;
    import application.logic.Type;
    import application.logic.User;
    import application.repositories.UserRepository;
    import javafx.scene.Node;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.*;

    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.fxml.Initializable;
    import javafx.stage.Stage;

    import java.net.URL;
    import java.util.ResourceBundle;

    public class RegisterViewController implements Initializable {


        private SharedResources sharedResources;
        @FXML
        private TextField emailTextField;

        @FXML
        private PasswordField passwordTextField;

        @FXML
        private PasswordField confirmPasswordTextField;

        private Stage stage;

        private UserRepository userRepository;

        public void setStage(Stage stage) {
            this.stage = stage;
        }

        public void setSharedResources(SharedResources sharedResources ) {
            this.sharedResources = sharedResources;
            this.userRepository = sharedResources.getUserRepository();
        }

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
        }

        @FXML
        private void goToLoginView(ActionEvent event) throws Exception{

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            LoginViewController controller = loader.getController();

            controller.setSharedResources(sharedResources);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Crea la nuova scena
            Scene scene = new Scene(root);

            // Applica la nuova scena
            stage.setScene(scene);

            // Mostra la finestra con tutto già sistemato
            stage.show();
        }

        @FXML
        private void handleRegister(ActionEvent event) throws Exception {
            String email=emailTextField.getText();
            String password=passwordTextField.getText();
            String confirmPassword=confirmPasswordTextField.getText();

            if(!password.equals(confirmPassword) || password.isEmpty()) {
                invalidPasswordPopUp(event);
                emailTextField.clear();
                passwordTextField.clear();
                confirmPasswordTextField.clear();
                return;
            }

            /*Controllo credenziali, se giusta-> goToMainView, altrimenti chiamo il Popup*/
            if(userRepository.isEmailAvailable(email) && userRepository.isEmailValid(email)){
                userRepository.saveUser(new User(email,password, Type.USER));
                registerSuccessfulPopUp(event);
                goToLoginView(event);
            }
            else{
                invalidMailPopUp(event);
            }

            emailTextField.clear();
            passwordTextField.clear();
            confirmPasswordTextField.clear();
        }

        @FXML
        private void invalidMailPopUp(ActionEvent event) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Register Error");
            alert.setHeaderText(null);
            alert.setContentText("Email format is not valid or email is already in use!");
            alert.showAndWait();
        }

        private void invalidPasswordPopUp(ActionEvent event) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Register Error");
            alert.setHeaderText(null);
            alert.setContentText("Passwords do not match!\nREMINDER: passwords cannot be empty!");
            alert.showAndWait();
        }

        private void registerSuccessfulPopUp(ActionEvent event) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Register Success!");
            alert.setHeaderText(null);
            alert.setContentText("Registration was successful!");
            alert.showAndWait();
        }
    }