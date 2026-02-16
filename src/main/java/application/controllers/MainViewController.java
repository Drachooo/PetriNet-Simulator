package application.controllers;

import application.logic.*;
import application.repositories.PetriNetRepository;
import application.repositories.UserRepository;
import application.exceptions.UnauthorizedAccessException;
import application.exceptions.EntityNotFoundException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the MainView.fxml (Main Dashboard).
 * Manages the user's active computations and main navigation.
 */
public class MainViewController implements Initializable {

    // --- Services ---
    private ProcessService processService;
    private PetriNetRepository petriNetRepository;
    private SharedResources sharedResources;
    private UserRepository userRepository;

    // --- State ---
    private User currentUser;
    private Stage stage;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private ObservableList<Object> tableData = FXCollections.observableArrayList();

    // --- FXML Components ---
    @FXML private Label welcomeLabel;
    @FXML private Label yourComputationsCountLabel;
    @FXML private Label totalNetsCountLabel;
    @FXML private Label totalUsersCountLabel;
    @FXML private Label tableTitleLabel;
    @FXML private Label errorLabel;
    @FXML private TextField searchTextField;

    // Sidebar Buttons
    @FXML private Button myComputationsButton;
    @FXML private Button exploreNetsButton;
    @FXML private Button adminAreaButton;
    @FXML private Button helpButton;
    @FXML private Button logoutButton;

    // Table Action Buttons
    @FXML private Button viewButton;
    @FXML private Button deleteButton;

    // Table
    @FXML private TableView<Object> mainTableView;
    @FXML private TableColumn<Object, String> column1;
    @FXML private TableColumn<Object, String> column2;
    @FXML private TableColumn<Object, String> column3;
    @FXML private TableColumn<Object, String> column4;

    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundImage;

    @FXML private ComboBox<String> searchTypeComboBox;

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
        this.processService = sharedResources.getProcessService();
        this.petriNetRepository = sharedResources.getPetriNetRepository();
        this.userRepository = sharedResources.getUserRepository();

        mainTableView.setItems(tableData);
        setupComputationColumns();

        // Disable action buttons by default
        viewButton.setDisable(true);
        deleteButton.setDisable(true);

        // Enable buttons only when a row is selected in the table
        mainTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            viewButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        if (errorLabel != null) errorLabel.setText("");

        if(backgroundImage != null && rootStackPane != null) {
            backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());
            backgroundImage.setPreserveRatio(false);
        }

        // Search listener for filtering nets
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTable(newValue);
        });

        // Re-filter if the search mode changes while there is input text
        searchTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterTable(searchTextField.getText());
        });

        searchTypeComboBox.setItems(FXCollections.observableArrayList("Net Name", "Creator Name"));
        searchTypeComboBox.getSelectionModel().selectFirst();

        // Set combo box text color to white
        searchTypeComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(javafx.scene.paint.Color.WHITE);
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
    }

    /**
     * Initializes the controller with shared resources if not already set.
     * * @param sharedResources The shared resources instance.
     */
    public void setSharedResources(SharedResources sharedResources) {
        if (this.sharedResources == null) { this.initialize(null, null); }
    }

    /**
     * Sets the currently logged-in user and updates the UI accordingly.
     * * @param currentUser The user currently logged in.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        initializeUIComponents();
        refreshDashboardData();
    }

    private void initializeUIComponents() {
        welcomeLabel.setText("Welcome, " + currentUser.getUsername());
        boolean isAdmin = currentUser.isAdmin();
        adminAreaButton.setVisible(isAdmin);
        adminAreaButton.setManaged(isAdmin);
    }

    /**
     * Configures table columns and binds cell values for Computations.
     */
    private void setupComputationColumns() {
        tableTitleLabel.setText("My Computations");
        column1.setText("Net Name");
        column2.setText("Net Creator");
        column3.setText("Date Started");
        column4.setText("Status");

        column1.setCellValueFactory(cell -> {
            Computation comp = (Computation) cell.getValue();
            PetriNet net = petriNetRepository.getPetriNets().get(comp.getPetriNetId());
            return new SimpleStringProperty(net != null ? net.getName() : "Unknown Net");
        });

        column2.setCellValueFactory(cell -> {
            Computation comp = (Computation) cell.getValue();
            PetriNet net = petriNetRepository.getPetriNets().get(comp.getPetriNetId());
            User admin = (net != null) ? userRepository.getUserById(net.getAdminId()) : null;
            return new SimpleStringProperty(admin != null ? admin.getEmail() : "Unknown");
        });

        column3.setCellValueFactory(cell -> {
            Computation comp = (Computation) cell.getValue();
            return new SimpleStringProperty(comp.getStartTime().format(formatter));
        });

        column4.setCellValueFactory(cell -> {
            Computation comp = (Computation) cell.getValue();
            return new SimpleStringProperty(comp.getStatus().toString());
        });

        column4.setCellFactory(column -> new TableCell<Object, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("COMPLETED".equalsIgnoreCase(status)) {
                        setStyle("-fx-text-fill: #ff5555; -fx-font-weight: bold;");
                    } else if ("ACTIVE".equalsIgnoreCase(status) || "RUNNING".equalsIgnoreCase(status)) {
                        setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                    } else {
                        setTextFill(javafx.scene.paint.Color.WHITE);
                        setStyle("");
                    }
                }
            }
        });
    }

    /**
     * Refreshes dashboard statistics and the table data.
     */
    private void refreshDashboardData() {
        if(currentUser == null){
            return;
        }

        if (errorLabel != null) errorLabel.setText("");

        int yourComps = processService.getComputationsForUser(currentUser.getId()).size();
        yourComputationsCountLabel.setText(String.valueOf(yourComps));

        int totalNets = processService.getAvailableNetsForUser(currentUser.getId()).size();
        totalNetsCountLabel.setText(String.valueOf(totalNets));

        int totalUsers = userRepository.getAllUsers().size();
        totalUsersCountLabel.setText(String.valueOf(totalUsers));

        List<Computation> userComputations = processService.getComputationsForUser(currentUser.getId());
        userComputations.sort((c1, c2) -> {
            LocalDateTime t1 = c1.getStartTime();
            LocalDateTime t2 = c2.getStartTime();
            if(t1 == null) return 1;
            if(t2 == null) return -1;
            return t2.compareTo(t1);
        });

        tableData.setAll(userComputations);
    }

    /**
     * Filters the table data based on the provided search key and selected mode.
     * * @param searchKey The text to filter by.
     */
    private void filterTable(String searchKey){
        if(searchKey == null || searchKey.isEmpty()){
            refreshDashboardData();
            return;
        }

        String lowerCaseFilter = searchKey.toLowerCase();
        String searchMode = searchTypeComboBox.getValue();

        List<Computation> userComputations = processService.getComputationsForUser(currentUser.getId());

        List<Computation> filteredList = userComputations.stream()
                .filter(comp -> {
                    PetriNet net = petriNetRepository.getPetriNets().get(comp.getPetriNetId());
                    if (net == null) return false;

                    if ("Net Name".equals(searchMode)) {
                        return net.getName().toLowerCase().contains(lowerCaseFilter);
                    } else {
                        User creator = userRepository.getUserById(net.getAdminId());
                        String creatorName = (creator != null) ? creator.getUsername().toLowerCase() : "";
                        String creatorEmail = (creator != null) ? creator.getEmail().toLowerCase() : "";
                        return creatorName.contains(lowerCaseFilter) || creatorEmail.contains(lowerCaseFilter);
                    }
                })
                .toList();

        tableData.setAll(filteredList);
    }

    // --- EVENT HANDLERS ---

    @FXML
    void goToExploreNets(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event,"/fxml/ExploreNetsView.fxml",currentUser);
    }

    @FXML
    void goToAdminArea(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event,"/fxml/AdminArea.fxml",currentUser);
    }

    /**
     * Navigates to the Help/Documentation view.
     * Passes the current user to maintain session state and role-based access.
     *
     * @param event The action event triggered by clicking the Help button.
     * @throws IOException If the FXML file for the Help View cannot be loaded.
     */
    @FXML
    void goToHelp(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event, "/fxml/HelpView.fxml", currentUser);
    }

    @FXML
    void handleView(ActionEvent event) throws IOException {
        Object selectedItem = mainTableView.getSelectionModel().getSelectedItem();
        // Fallback check, although the button should be disabled if nothing is selected
        if(!(selectedItem instanceof Computation)){
            showError("Please select a computation to view.");
            return;
        }

        Computation computation = (Computation) selectedItem;


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewPetriNet.fxml"));
        Parent root = loader.load();
        ViewPetriNetController controller = loader.getController();
        controller.loadComputation(this.currentUser, computation);

        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.getScene().setRoot(root);
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Object selectedItem = mainTableView.getSelectionModel().getSelectedItem();
        // Fallback check, although the button should be disabled if nothing is selected
        if(!(selectedItem instanceof Computation)){
            showError("Please select a computation to delete.");
            return;
        }

        Computation selectedComp = (Computation) selectedItem;
        try {
            processService.deleteComputation(selectedComp.getId(), currentUser.getId());
            refreshDashboardData();
        } catch (UnauthorizedAccessException | EntityNotFoundException | IllegalStateException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    void handleEditProfile(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Change Username or Password");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setText(currentUser.getUsername());
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Repeat new password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == saveButtonType) {
            boolean isChanged = false;
            String newPass = passwordField.getText();
            String confirmPass = confirmPasswordField.getText();
            String newUsername = usernameField.getText();

            if (!newPass.isEmpty()) {
                if (!newPass.equals(confirmPass)) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Password Error");
                    errorAlert.setHeaderText("Passwords do not match");
                    errorAlert.showAndWait();
                    return;
                } else {
                    currentUser.setPassword(newPass);
                    isChanged = true;
                }
            }
            if (!newUsername.isEmpty() && !newUsername.equals(currentUser.getUsername())) {
                currentUser.setUsername(newUsername);
                isChanged = true;
            }

            if (isChanged) {
                userRepository.updateUser(currentUser);
                welcomeLabel.setText("Welcome, " + currentUser.getUsername());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Profile updated successfully!");
                alert.showAndWait();
            }
        }
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event, "/fxml/LoginView.fxml");
    }

    private void showError(String error) {
        if(errorLabel != null) {
            errorLabel.setText(error);
            errorClearer.stop();
            errorClearer.playFromStart();
        } else {
            System.err.println(error);
        }
    }


}