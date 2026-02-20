# Petri Net Simulator

A Java-based Petri Net simulator built to formally define and execute distributed systems, featuring a strict Role-Based Access Control (RBAC) system. The application allows users to design network topologies and safely execute state transitions based on their assigned permissions.

## 👥 System Roles

* **Administrator:** Designs the Petri Net by defining places, transitions, and directed arcs. Supervises global computations and executes admin-reserved transitions.
* **End User:** Subscribes to available networks, starts computation instances, and interacts with the system by firing user-level transitions.

## ✨ Key Features

* **Graphical Modeling:** Visual creation of Petri Nets with structural validation, ensuring single initial (`p_init`) and final (`p_final`) places.
* **Interactive Execution:** Automatic calculation of enabled transitions based on the current marking.
* **Concurrency Management (Snapshots):** When a user starts a computation, the system creates a *Deep Copy* snapshot of the network using Jackson. This isolates the user's execution environment from any subsequent modifications made by the administrator.
* **Local Persistence:** Network structures and computation histories are saved locally in JSON format, requiring no external database setup.

## 🏗️ Architecture & Design Patterns

The project follows a strict **MVC (Model-View-Controller)** architecture, backed by a central Service Layer (`ProcessService`). 

To handle complex business logic and UI decoupling, several **GoF Design Patterns** were implemented:

* **Singleton:** `SharedResources` ensures a single global access point for repositories.
* **Strategy:** Handles the RBAC logic. `TransitionExecutionStrategy` dynamically applies either `AdminExecutionStrategy` or `UserExecutionStrategy` at runtime.
* **Observer:** Keeps the JavaFX UI synchronized. The `Computation` class notifies `ComputationViewObserver` of state changes, completely decoupling the UI from the execution engine.
* **Facade:** The `ProcessService` provides a unified interface, hiding the complexity of business rules, security checks, and JSON persistence from the JavaFX controllers.
* **Simple Factory:** Classes like `PlaceViewFactory` and `TransitionViewFactory` encapsulate the dynamic generation and property binding of JavaFX graphical nodes.

## 🛠️ Tech Stack

* **Language:** Java
* **UI Framework:** JavaFX
* **Build Tool:** Maven
* **Serialization:** Jackson (JSON)
* **Testing:** JUnit 5, Mockito

## 🚀 Setup & Run

The project uses Maven for dependency management and build automation.

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Drachooo/PetriNet-Simulator.git
   cd PetriNet-Simulator
   ```

2. **Build the project and run tests:**
   ```bash
   mvn clean install
   ```

3. **Start the application:**
   ```bash
   mvn javafx:run
   ```

## 🧪 Testing

The system's reliability is ensured by a suite of **70 automated tests**. Testing covers three critical areas:

* **Structural Integrity:** Validates bipartite graph constraints and UUID generation.
* **Execution Engine:** Verifies the firing rules, token consumption, and production within the `PetriNet` class.
* **Security & Permissions:** Proves that the *Strategy* pattern correctly prevents administrators from bypassing constraints to fire user transitions on their own networks.

---

## ✍️ Authors

* **Luca Quaresima**
* **Matteo Drago**

> *Developed for the Software Engineering course, University of Verona.*
@
