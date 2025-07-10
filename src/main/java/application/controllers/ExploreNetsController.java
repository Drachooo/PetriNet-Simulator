package application.controllers;

import application.logic.PetriNet;
import application.logic.SharedResources;
import application.ui.graphics.PetriNetTableRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ExploreNetsController implements Initializable {
    @FXML private TableView<PetriNetTableRow> tableView;
    @FXML private TableColumn<PetriNetTableRow, String> netNameColumn;
    @FXML private TableColumn<PetriNetTableRow, String> creatorColumn;
    @FXML private TableColumn<PetriNetTableRow, String> dateCreatedColumn;
   // @FXML private TableColumn<PetriNetTableRow, String> statusColumn;
    private SharedResources sharedResources;
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        netNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        creatorColumn.setCellValueFactory(cellData -> cellData.getValue().creatorProperty());
        dateCreatedColumn.setCellValueFactory(cellData -> cellData.getValue().dateCreatedProperty());
    }







    public void populateTable() {
        if (sharedResources == null) return;

        Map<String, PetriNet> allPetriNets = sharedResources.getPetriNetRepository().getPetriNets();
        ObservableList<PetriNetTableRow> tableRows = FXCollections.observableArrayList();

        for (PetriNet net : allPetriNets.values()) {
            String creatorEmail = sharedResources.getUserRepository()
                    .getUserById(net.getAdminId())
                    .getEmail();

            PetriNetTableRow row = new PetriNetTableRow(
                    net,
                    net.getName(),
                    creatorEmail,
                    net.getCreationDateFormatted()
            );
            tableRows.add(row);
        }

        tableView.setItems(tableRows);
    }

    @FXML
    private void goToAdminArea(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminArea.fxml"));
        Parent root = loader.load();

        AdminAreaController controller = loader.getController();
        controller.setSharedResources(sharedResources);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void goToExploreNets(ActionEvent event) {

    }
    public void goToHelp(ActionEvent event) {

    }

    @FXML
    private void logOut(ActionEvent event) throws IOException {
        sharedResources.setCurrentUser(null);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        LoginViewController controller = loader.getController();
        controller.setSharedResources(sharedResources);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }


    public void setSharedResources(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
        populateTable();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
