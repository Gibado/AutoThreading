package com.gibado.basics;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This object represents a single unit of work.
 */
public abstract class WorkUnit implements Runnable {
    private String name = "Default WorkUnit";
    private State state;
    private WorkUnit parent;
    private List<WorkUnit> dependents;
    private Map<String, Sharable<?>> requiredMap;
    private Instant startTime = null;
    private long taskTime = -1;
    private long totalTime = -1;
    private long timeout = -1;
    private ProcessPlant processPlant;
    private Exception exceptionThrown;

    /**
     * Creates a one off task
     */
    public WorkUnit() {
        this(null, null);
    }

    /**
     * Creates a task that must be completed before the given {@link WorkUnit}
     * @param parent {@link WorkUnit} to be triggered after this {@link WorkUnit} is completed
     */
    public WorkUnit(WorkUnit parent) {
        this(parent, null);
    }

    /**
     * Creates a task that must be completed before the given parent {@link WorkUnit}, but cannot start until the given
     * dependent {@link WorkUnit}s have completed
     * @param parent {@link WorkUnit} to be triggered after this {@link WorkUnit} is completed
     * @param dependents {@link WorkUnit}s that must be done before this {@link WorkUnit} can begin
     */
    public WorkUnit(WorkUnit parent, List<WorkUnit> dependents) {
        this.parent = parent;
        if (dependents != null && !dependents.isEmpty()) {
            this.dependents = dependents;
            state = State.WAITING_DEPENDENT;
        } else {
            state = State.READY;
        }
    }

    /**
     * Defines how long is acceptable to wait for {@link Sharable} resources.
     * @param milliseconds Time in milliseconds
     */
    public void setTimeout(long milliseconds) {
        this.timeout = milliseconds;
    }

    /**
     * Returns the name of this {@link WorkUnit}
     * @return Returns the name of this {@link WorkUnit}
     */
    public String getName() { return name; }

    /**
     * Gives this {@link WorkUnit} a name
     * @param name Name to refer to this {@link WorkUnit} as
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns all {@link WorkUnit}s this is dependent on
     * @return Returns all {@link WorkUnit}s this is dependent on
     */
    public List<WorkUnit> getDependents() { return dependents; }

    /**
     * Assigns dependents to this {@link WorkUnit} that must be completed before this {@link WorkUnit} can begin
     * @param dependents {@link List} of {@link WorkUnit}s that must be completed before this {@link WorkUnit} can begin
     */
    public void setDependents(List<WorkUnit> dependents) {
        this.dependents = dependents;
        for (WorkUnit dependent : dependents) {
            dependent.setParent(this);
        }
    }

    /**
     * Assigns dependents to this {@link WorkUnit} that must be completed before this {@link WorkUnit} can begin
     * @param dependents {@link WorkUnit}s that must be completed before this {@link WorkUnit} can begin
     */
    public void setDependents(WorkUnit ... dependents) {
        setDependents(Arrays.asList(dependents));
    }

    /**
     * Adds all required {@link Sharable} resources
     * @param requiredMap {@link Map} of {@link Sharable} resources used when using the performTask method
     */
    public void addAllRequired(Map<String, Sharable<?>> requiredMap) {
        if (this.requiredMap == null) {
            this.requiredMap = new HashMap<>();
        }
        this.requiredMap.putAll(requiredMap);
    }

    /**
     * Adds a {@link Sharable} resource for use during the performTask method
     * @param key Access key
     * @param sharable {@link Sharable} resource
     */
    public void addRequiredParam(String key, Sharable<?> sharable) {
        if (requiredMap == null) {
            requiredMap = new HashMap<>();
        }
        requiredMap.put(key, sharable);
    }

    /**
     * Reference back to the {@link ProcessPlant} for callback actions
     * @param processPlant {@link ProcessPlant} for callback actions
     */
    protected void setProcessPlant(ProcessPlant processPlant) { this.processPlant = processPlant; }

    /**
     * Returns the {@link WorkUnit} to be executed after this task is complete
     * @return Returns the {@link WorkUnit} to be executed after this task is complete
     */
    protected WorkUnit getParent() { return parent; }

    /**
     * Assigns a task to be completed after this task is done
     * @param workUnit {@link WorkUnit} to be executed after this one
     */
    private void setParent(WorkUnit workUnit) { this.parent = workUnit; }

    /**
     * Returns true if this {@link WorkUnit} already has a thread working on it
     * @return Returns true if this {@link WorkUnit} already has a thread working on it
     */
    public synchronized boolean isThreadClaimed() {
        return startTime != null;
    }

    /**
     * Updates and returns the latest state of this {@link WorkUnit}.
     * @return Returns the current state of this {@link WorkUnit}.
     */
    protected synchronized State updateState() {
        if (State.DONE.equals(state) || State.ERROR.equals(state)) {
            // Task has already been attempted or completed
            return state;
        }
        // Check for dependents have to be completed
        if (dependents != null) {
            // See if we care about any dependent states
            State dependentState = getHighestPriorityState(dependents);

            switch (dependentState) {
                case WAITING_DEPENDENT:
                case WAITING_RESOURCE:
                case READY:
                case INITIATED:
                case IN_PROGRESS:
                    // Wait for all dependents to be done
                    state = State.WAITING_DEPENDENT;
                    return state;
                case ERROR:
                    // If a dependent found an error then this task cannot be processed
                    state = State.ERROR;
                    exceptionHandling(exceptionThrown, null);
                    return state;
                case DONE:
                    // This dependent is done
                    break;
            }
        }
        // Check if resources are available
        if (!areRequiredAvailable()) {
            state = State.WAITING_RESOURCE;
            return state;
        }

        // Nothing in the way of starting this task
        state = State.READY;
        return state;
    }

    /**
     * Finds the most important {@link State} held by one of the dependents
     * @param dependents {@link WorkUnit}s to check
     * @return Returns the highest importance {@link State} held by one of the dependents
     */
    private State getHighestPriorityState(final List<WorkUnit> dependents) {
        State result = State.DONE;
        for (WorkUnit dependent : dependents) {
            State dependentState = dependent.updateState();
            switch (dependentState) {
                case WAITING_DEPENDENT:
                case WAITING_RESOURCE:
                case READY:
                case INITIATED:
                case IN_PROGRESS:
                    // Wait for all dependents to be done
                    if (dependentState.compareTo(result) > 0) {
                        result = dependentState;
                    }
                    break;
                case ERROR:
                    // If a dependent found an error then this task cannot be processed
                    exceptionThrown = dependent.exceptionThrown;
                    return State.ERROR;
                case DONE:
                    // This dependent is done
                    break;
            }
        }
        return result;
    }

    /**
     * Checks if the necessary {@link Sharable}s are available for processing this task.  This does not claim the
     * {@link Sharable}s!
     * @return Returns true if the {@link Sharable}s are available.
     */
    private boolean areRequiredAvailable() {
        if (requiredMap != null) {
            for (Sharable sharable : requiredMap.values()) {
                if (sharable.isLocked()) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * This method will be called to perform the described unit of work.
     * @param params {@link Map} containing values based on the {@link Sharable} added to this {@link WorkUnit}
     */
    public abstract void performTask(Map<String, ?> params);

    /**
     * This method is called if an exception occurs during the performTask method before the exception is thrown again
     * @param exception Exception that was thrown during the performTask method
     * @param params {@link Map} containing values based on the {@link Sharable} added to this {@link WorkUnit}
     */
    public void exceptionHandling(Exception exception, Map<String, ?> params) {}

    public final void run() {
        // Check if this task has already been done
        if (State.DONE.equals(state)) {
            return;
        }
        startTime = Instant.now();
        long elapsedTime = Duration.between(startTime, Instant.now()).toMillis();
        state = State.INITIATED;
        Map<String, Object> params = null;
        try {
            boolean workDone = false;
            while (!workDone && (timeout ==-1 || elapsedTime < timeout)) {
                // Check if Sharables are available
                if (areRequiredAvailable()) {
                    // claim Sharables
                    params = claimAllRequired();
                    // check if we got the requiredMap
                    if (!containsNull(params)) {
                        state = State.IN_PROGRESS;
                        performTask(params);
                        state = State.DONE;
                        workDone = true;
                    }
                    // release requiredMap for others to use
                    releaseAll();
                }
                elapsedTime = Duration.between(startTime, Instant.now()).toMillis();
            }
            if (!workDone) {
                // Timeout reached
                throw new IllegalStateException("Could not grab required Sharable(s) in time: " + requiredMapToString(requiredMap));
            }
        } catch (Exception e) {
            this.exceptionThrown = e;
            state = State.ERROR;
            exceptionHandling(e, params);
            throw e;
        } finally {
            // Make sure Sharables have been released
            releaseAll();
            // Update times
            Instant endTime = Instant.now();
            taskTime = Duration.between(startTime, endTime).toMillis();
            totalTime = taskTime + getTotalDependentTime();
            // Call back to trigger parent WorkUnit
            signalEnd();
        }
    }

    /**
     * Converts a {@link Sharable} {@link Map} into a String
     * @param requiredMap A {@link Map} of String, {@link Sharable}
     * @return Returns a {@link Map} of String, {@link Sharable} into a String for logging
     */
    private String requiredMapToString(Map<String, Sharable<?>> requiredMap) {
        StringBuilder sb = new StringBuilder("[");
        for (Map.Entry<String, Sharable<?>> entry : requiredMap.entrySet()) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append("{").append(entry.getKey()).append(", ").append(entry.getValue().toString()).append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Notifies the {@link ProcessPlant} that this {@link WorkUnit} is no longer processing
     */
    private void signalEnd() {
        processPlant.signalComplete(this);
    }

    /**
     * Releases all {@link Sharable}s
     */
    private void releaseAll() {
        for (Sharable<?> sharable : requiredMap.values()) {
            if (sharable.isLocked()) {
                sharable.release(this);
            }
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
     * Attempts to claims all {@link Sharable}s and stores the values in a parameter map.  If a {@link Sharable} wasn't
     * claimed then the value in the {@link Map} will be null.
     * @return Returns the Objects from the {@link Sharable} map
     */
    private Map<String, Object> claimAllRequired() {
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, Sharable<?>> entry : requiredMap.entrySet()) {
            params.put(entry.getKey(), entry.getValue().claim(this));
        }
        return params;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(name).append(" - ").append(state);
        if (taskTime > -1) {
             sb.append(" in ").append(taskTime).append(" ms (Total: ").append(totalTime).append(" ms)");
        }
        return sb.toString();
    }

	/**
	 * Returns the amount of time the dependents took to complete their tasks as a collective
	 * @return Returns the amount of time the dependents took to complete their tasks as a collective
	 */
	private long getTotalDependentTime() {
        long dependentTime = 0;
        if (dependents != null) {
            for (WorkUnit dependent : dependents) {
                dependentTime += dependent.getTotalTime();
            }
        }
        return dependentTime;
    }

    /**
     * Returns start time in milliseconds
     * @return Returns start time in milliseconds
     */
    public long getStartTime() { return startTime.toEpochMilli(); }

    /**
     * Returns how much time in milliseconds was taken to start and perform this task
     * @return Returns how much time in milliseconds was taken to start and perform this task
     */
    public long getTaskTime() { return taskTime; }

    /**
     * Returns how much time was taken to start and perform this task and all the tasks it depends on
     * @return Returns how much time was taken to start and perform this task and all the tasks it depends on
     */
    public long getTotalTime() { return totalTime; }

    /**
     * Different states that a {@link WorkUnit} can be in.  This is ordered by importance.
     */
    public enum State {
        /** {@link WorkUnit} has finished its task successfully */
        DONE,
        /** {@link WorkUnit} has started performing its task */
        IN_PROGRESS,
        /** {@link WorkUnit} has started attempting to claim its required {@link Sharable} resources */
        INITIATED,
        /** {@link WorkUnit} is ready to perform its task */
        READY,
        /** Requires at least 1 more {@link Sharable} resource required for performing the {@link WorkUnit} task */
        WAITING_RESOURCE,
        /** Requires a dependent {@link WorkUnit} to process before continuing */
        WAITING_DEPENDENT,
        /** {@link WorkUnit} encountered an error */
        ERROR
    }
}
