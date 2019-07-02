package com.gibado.basics;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This object represents a single unit of work.
 */
public abstract class WorkUnit implements Runnable {
    private State state;
    private WorkUnit parent;
    private List<WorkUnit> dependents;
    private Map<String, Sharable<Object>> sharableMap;
    private long startTime;
    private long totalTime;
    private long timeout = -1;

    public WorkUnit() {
        this(null, null);
    }

    public WorkUnit(WorkUnit parent) {
        this(parent, null);
    }

    public WorkUnit(WorkUnit parent, List<WorkUnit> dependents) {
        this.parent = parent;
        if (dependents != null && !dependents.isEmpty()) {
            this.dependents = dependents;
            state = State.WAITING;
        } else {
            state = State.READY;
        }
    }

    public List<WorkUnit> getDependents() { return dependents; }
    public void setDependents(List<WorkUnit> dependents) { this.dependents = dependents; }
    public void setSharableMap(Map<String, Sharable<Object>> sharableMap) { this.sharableMap = sharableMap; }

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
        if (sharableMap != null) {
            for (Sharable sharable : sharableMap.values()) {
                if (sharable.isLocked()) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * This method will be called to perform the described unit of work.
     */
    public abstract void performTask(Map<String, Object> params);

    public final void run() {
        Instant startTime = Instant.now();
        state = State.INITIATED;
        try {
            boolean workDone = false;
            while (!workDone) {
                // Check if Sharables are available
                if (areSharablesAvailable()) {
                    // claim Sharables
                    Map<String, Object> params = claimAllSharable();
                    // check if we got the sharableMap
                    if (!containsNull(params)) {
                        state = State.IN_PROGRESS;
                        performTask(params);
                        workDone = true;
                    }
                    // release sharableMap for others to use
                    releaseAll();
                }
            }
        } catch (Exception e) {
            state = State.ERROR;
            throw e;
        } finally {
            Instant entTime = Instant.now();
            totalTime = Duration.between(entTime, startTime).toMillis();
        }
        state = State.DONE;
        // TODO trigger parent workflow
    }

    /**
     * Releases all {@link Sharable}s
     */
    private void releaseAll() {
        for (Sharable<?> sharable : sharableMap.values()) {
            sharable.release(this);
        }
    }

    /**
     * Checks for null values in the param Map.
     * @param params Parameter Map to check
     * @return Returns true if not all {@link Sharable}s were claimed
     */
    private boolean containsNull(Map<String, Object> params) {
        for (Object value : params.values()) {
            if (value == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Claims all {@link Sharable}s and stores the values in a parameter map
     * @return Returns the Objects from the {@link Sharable} map
     */
    private Map<String, Object> claimAllSharable() {
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, Sharable<Object>> entry : sharableMap.entrySet()) {
            params.put(entry.getKey(), entry.getValue().claim(this));
        }
        return params;
    }

    public String toString() {
        return state.toString();
    }

    public long getStartTime() { return startTime; }

    public long getTotalTime() { return totalTime; }
}
