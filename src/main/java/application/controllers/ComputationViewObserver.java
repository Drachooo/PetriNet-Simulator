package application.controllers;

import application.logic.Computation;
import application.logic.ComputationObserver;
import java.util.Objects;

/**
 * Concrete Observer class responsible for linking the Computation Model state
 * to the visual update logic in the ViewPetriNetController.
 */
public class ComputationViewObserver extends ComputationObserver {

    private final Computation subject;
    private final ViewPetriNetController view;

    /**
     * Constructs the Concrete Observer and attaches it to the Subject.
     * * @param subject The Computation object being observed.
     * @param view The specific View Controller instance that needs refreshing.
     */
    public ComputationViewObserver(Computation subject, ViewPetriNetController view) {
        this.subject = Objects.requireNonNull(subject);
        this.view = Objects.requireNonNull(view);
        this.subject.attach(this);
    }

    /**
     * Called by the Subject (Computation) when its state changes.
     */
    @Override
    public void update(Computation updatedComputation) {
        view.setCurrentComputation(updatedComputation);

        view.refreshState();
    }
}