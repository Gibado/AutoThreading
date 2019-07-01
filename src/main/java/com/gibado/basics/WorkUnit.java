package com.gibado.basics;

import java.util.List;

public class WorkUnit implements Runnable {
    private State state;
    private WorkUnit parent;
    private List<WorkUnit> dependents;
    private List<Sharable> sharables;
    private long startTime;
    private long totalTime;

    /**
     * Updates and returns the latest state of this {@link WorkUnit}.
     * @return Returns the current state of this {@link WorkUnit}.
     */
    protected State updateState() {
        if (dependents != null) {
            for (WorkUnit dependent : dependents) {
                State dependentState = dependent.updateState();
                switch (dependentState) {
                    case READY:
                    case IN_PROGRESS:
                    case WAITING:
                        // Wait for all dependents to be done
                        state = State.WAITING;
                        return state;
                    case ERROR:
                        // If a dependent found an error then this task cannot be processed
                        state = State.ERROR;
                        return state;
                    case DONE:
                        // This dependent is done
                        break;
                }
            }
        }
        if (!areSharablesAvailable()) {
            state = State.WAITING;
            return state;
        }

        state = State.READY;
        return state;
    }

    /**
     * Checks if the necessary {@link Sharable}s are available for processing this task.  This does not claim the {@link Sharable}s!
     * @return Returns true if the {@link Sharable}s are available.
     */
    private boolean areSharablesAvailable() {
        if (sharables != null) {
            for (Sharable sharable : sharables) {
                if (sharable.isLocked()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void run() {

    }
}
