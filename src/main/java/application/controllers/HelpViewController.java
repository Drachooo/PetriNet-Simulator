package application.controllers;

import application.logic.SharedResources;
import application.logic.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HelpViewController implements Initializable {

    private User currentUser;
    private SharedResources sharedResources;

    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundImage;
    @FXML private Button adminAreaButton;

    @FXML private WebView introWebView;
    @FXML private WebView rolesWebView;
    @FXML private WebView rulesWebView;
    @FXML private WebView exampleWebView;

    private boolean isExternalWindow = false;

    @FXML private VBox sideBarHelp;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.sharedResources = SharedResources.getInstance();

        // Setup background responsivo
        UIHelper.setupCenterCropBackground(rootStackPane, backgroundImage);

        loadHelpContent();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUser != null && adminAreaButton != null) {
            adminAreaButton.setVisible(currentUser.isAdmin());
            adminAreaButton.setManaged(currentUser.isAdmin());
        }
    }

    private void loadHelpContent() {
        String introContent = getHtmlStyle() +
                "<body>" +
                "<h3>1. Introduction to Petri Nets</h3>" +

                "<h4>1.1. Background on Petri Nets</h4>" +
                "<p>Petri nets are a mathematical modeling language used for the description of distributed systems. A Petri net is a directed bipartite graph consisting of <b>places</b>, <b>transitions</b>, and <b>directed arcs</b>. Places can contain tokens, and the distribution of tokens across the places represents the state (marking) of the system.</p>" +

                "<h4>1.1.1. Formal Definition</h4>" +
                "<p>The formal definition of a Petri net includes:</p>" +
                "<ul>" +
                "<li>A set of places <b>P = {p<sub>1</sub>, p<sub>2</sub>, ..., p<sub>n</sub>}</b></li>" +
                "<li>A set of transitions <b>T = {t<sub>1</sub>, t<sub>2</sub>, ..., t<sub>m</sub>}</b></li>" +
                "<li>A set of arcs <b>A ⊆ (P × T) U (T × P)</b></li>" +
                "<li>A<sub>n</sub> initial marking <b>M<sub>0</sub>: P → N</b>, which assigns a non-negative integer number of tokens to each place.</li>" +
                "</ul>" +

                "<h4>1.1.3. Special Places and Process Completion</h4>" +
                "<ul>" +
                "<li><b>Initial Place:</b> Each Petri net must have exactly one designated initial place (p<sub>init</sub>). The initial marking assigns exactly one token to this place. It has no incoming arcs, and at least one outgoing arc.</li>" +
                "<li><b>Final Place:</b> Each Petri net must have exactly one designated final place (p<sub>final</sub>). It has at least one incoming arc, and no outgoing arcs.</li>" +
                "</ul>" +

                "<p style='color: tomato; border: 1px solid tomato; padding: 10px;'><b>Process Completion:</b> A computation is considered complete when a token is placed in the final place <b>p<sub>final</sub></b>. At this point, the computation is automatically closed and marked as completed.</p>" +
                "</body>";

        introWebView.getEngine().loadContent(introContent);


        String rolesContent = getHtmlStyle() +
                "<body>" +
                "<h3>2. System Roles and Permissions</h3>" +
                "<p>The system features two main roles: <b>administrators</b> who design Petri nets, and <b>end users</b> who execute processes modeled by these nets.</p>" +

                "<div style='margin-bottom: 20px;'>" +
                "<h4 style='color: #4da6ff;'>2.1. Administrator Role</h4>" +
                "<p>Administrators have the following capabilities and restrictions:</p>" +
                "<ul>" +
                "<li>Design and manage their own Petri nets with places and transitions</li>" +
                "<li>Define initial and final places for each of their Petri nets</li>" +
                "<li>Partition transitions into <b>administrator-executable</b> and <b>user-executable</b> categories</li>" +
                "<li>View and manage all process executions related to their own Petri nets</li>" +
                "<li>Delete computations related to their own Petri nets</li>" +
                "<li>Fire <u>only administrator-designated transitions</u> in computations of their own Petri nets</li>" +
                "<li><b>Cannot act as users</b> of their own Petri nets.</li>" +
                "</ul>" +
                "</div>" +

                "<div>" +
                "<h4 style='color: #45a45f;'>2.2. End User Role</h4>" +
                "<p>End users have the following capabilities and restrictions:</p>" +
                "<ul>" +
                "<li>Subscribe to processes (Petri nets) created by any administrator</li>" +
                "<li>Create and manage computation instances of Petri nets they have subscribed to</li>" +
                "<li>Fire <u>only user-designated transitions</u> in their computation instances</li>" +
                "<li><b>Cannot fire administrator-designated transitions</b> under any circumstances</li>" +
                "<li>View the history of executed transitions with timestamps</li>" +
                "<li>Delete their own computations</li>" +
                "<li>Have at most one active computation instance per Petri net</li>" +
                "</ul>" +
                "</div>" +
                "</body>";

        rolesWebView.getEngine().loadContent(rolesContent);


        String rulesContent = getHtmlStyle() +
                "<body>" +
                "<h3>3. Execution Rules & Semantics</h3>" +

                "<h4>1.1.2. Semantics of Petri Nets</h4>" +
                "<p>The operational semantics of Petri nets define how the system evolves:</p>" +
                "<ul>" +
                "<li><b>Enabling Rule:</b> A transition <i>t ∈ T</i> is enabled if each of its input places contains at least one token.</li>" +
                "<li><b>Firing Rule:</b> When an enabled transition fires, it removes one token from each of its input places, and it adds one token to each of its output places.</li>" +
                "</ul>" +

                "<h4>2.3. Transition Execution Rules</h4>" +
                "<p>The system enforces strict rules regarding who can execute which transitions:</p>" +
                "<ul>" +
                "<li><b>Administrator transitions:</b> can only be fired by the administrator who created the Petri net.</li>" +
                "<li><b>User transitions:</b> can only be fired by users (non-administrators) who have created a computation instance.</li>" +
                "<li>A person acting as an administrator for one Petri net may act as a user for Petri nets created by other administrators.</li>" +
                "</ul>" +

                "<h4>3.3. Process Execution Constraints</h4>" +
                "<ul>" +
                "<li>The system shall track the current marking of each active computation.</li>" +
                "<li>The system shall allow only one active computation per user per Petri net.</li>" +
                "<li>Users shall be able to start a new computation only after completing any previous computation on the same Petri net.</li>" +
                "</ul>" +
                "</body>";

        rulesWebView.getEngine().loadContent(rulesContent);


        String exampleContent = getHtmlStyle() +
                "<body>" +
                "<h3>7.2. Execution Sequence Example</h3>" +
                "<p>Let us walk through a typical execution sequence for the Petri net shown in the figure above:</p>" +
                "<ol>" +
                "<li><b>Initial state:</b> A token is in the initial place <i>p<sub>init</sub></i>.</li>" +
                "<li>The administrator fires transition <b>t<sub>1</sub></b> (admin transition), moving the token to place p<sub>2</sub>.</li>" +
                "<li>The administrator fires transition <b>t<sub>2</sub></b> (admin transition), which: consumes the token from place p<sub>2</sub>, and produces one token in place p<sub>3</sub> and one token in place p<sub>4</sub>.</li>" +
                "<li>At this point, the computation state has tokens in places p<sub>3</sub> and p<sub>4</sub>.</li>" +
                "<li>The user can now fire transition <b>t<sub>3</sub></b> (user transition), moving the token from place p<sub>4</sub> to place p<sub>5</sub>.</li>" +
                "<li>In parallel, the user can fire transition <b>t<sub>4</sub></b> (user transition), moving the token from place p<sub>3</sub> to place p<sub>6</sub>.</li>" +
                "<li>The user then fires transition <b>t<sub>5</sub></b> (user transition), moving the token from place p<sub>5</sub> to place p<sub>7</sub>.</li>" +
                "<li>At this point, both execution paths have completed the user portion, with tokens in places p<sub>6</sub> and p<sub>7</sub>.</li>" +
                "<li>Finally, the administrator fires transition <b>t<sub>6</sub></b> (admin transition), which: consumes tokens from both places p<sub>6</sub> and p<sub>7</sub>, and produces one token in the final place <i>p<sub>final</sub></i>.</li>" +
                "<li>When the token reaches <i>p<sub>final</sub></i>, the computation is automatically marked as completed.</li>" +
                "</ol>" +
                "<p><i>Note: If the admin doesn't fire their transitions, the user cannot proceed. Similarly, if the user doesn't complete their transitions, the admin cannot finalize the process.</i></p>" +
                "</body>";

        exampleWebView.getEngine().loadContent(exampleContent);
    }

    /**
     * CSS style for the HTML content.
     */
    private String getHtmlStyle() {
        return "<html><head><style>" +
                "body { " +
                "   background-color: #1e1e1e; " +
                "   color: #e0e0e0; " +
                "   font-family: 'Segoe UI', sans-serif; " +
                "   font-size: 14px; " +
                "   padding: 10px; " +
                "   line-height: 1.6;" +
                "} " +
                "h3 { color: white; border-bottom: 1px solid #444; padding-bottom: 5px; } " +
                "h4 { margin-bottom: 5px; margin-top: 20px; color: #ddd; } " +
                "li { margin-bottom: 8px; } " +
                "b { color: white; font-weight: bold; } " +
                "</style></head>";
    }

    @FXML
    void goToMainView(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event, "/fxml/MainView.fxml", currentUser);
    }

    @FXML
    void goToExploreNets(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event, "/fxml/ExploreNetsView.fxml", currentUser);
    }

    @FXML
    void goToAdminArea(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event, "/fxml/AdminArea.fxml", currentUser);
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event, "/fxml/LoginView.fxml");
    }

    /**
     * Configures the view to behave as an isolated popup window.
     * Hides the navigation sidebar.
     *
     * @param isExternal true if opened as a utility window.
     */
    public void setExternalWindow(boolean isExternal) {
        this.isExternalWindow = isExternal;

        if (isExternal) {
            if (sideBarHelp != null) {
                sideBarHelp.setVisible(false);
                sideBarHelp.setManaged(false);
            }
            if (adminAreaButton != null) {
                adminAreaButton.setVisible(false);
                adminAreaButton.setManaged(false);
            }
        }
    }

}