package com.gibado.basics.workunit;

import com.gibado.basics.IProcessPlant;
import com.gibado.basics.sharable.ISharable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for working in
 */
public class WorkUnitHelper {
    /** Value to signal not to timeout during work */
    public static final long NO_TIMEOUT = -1;

    /**
     * Handles acquiring and releasing the required resources for the given {@link ITaskRunner}, initiates its task,
     * updates its {@link State} and time values
     * @param runner {@link ITaskRunner} to attempt to perform its task
     */
    public static void runLogic(ITaskRunner runner) {
        // Check if this task has already been done
        if (State.DONE.equals(runner.getState())) {
            return;
        }
        Instant startTime = Instant.now();
        runner.setStartTime(startTime);
        long elapsedTime = Duration.between(runner.getStartTime(), Instant.now()).toMillis();
        runner.setState(State.INITIATED);
        Map<String, Object> params = null;
        try {
            boolean workDone = false;
            long timeout = runner.getTimeout();
            while (!workDone && (timeout ==-1 || elapsedTime < timeout)) {
                // Check if Sharables are available
                if (areRequiredAvailable(runner)) {
                    // claim Sharables
                    params = claimAllRequired(runner);
                    // check if we got the requiredMap
                    if (!containsNull(params)) {
                        runner.setState(State.IN_PROGRESS);
                        runner.performTask(params);
                        runner.setState(State.DONE);
                        workDone = true;
                    }
                    // release requiredMap for others to use
                    releaseAll(runner);
                }
                elapsedTime = Duration.between(startTime, Instant.now()).toMillis();
            }
            if (!workDone) {
                // Timeout reached
                throw new IllegalStateException("Could not grab required Sharable(s) in time: " + requiredMapToString(runner.getRequiredMap()));
            }
        } catch (Exception e) {
            runner.setExceptionThrown(e);
            runner.setState(State.ERROR);
            runner.exceptionHandling(e, params);
            throw e;
        } finally {
            // Make sure Sharables have been released
            releaseAll(runner);
            // Update times
            Instant endTime = Instant.now();
            long taskTime = Duration.between(runner.getStartTime(), endTime).toMillis();
            runner.setTaskTime(taskTime);
            runner.setTotalTime(taskTime + getTotalDependentTime(runner));
            // Call back to trigger parent WorkUnit
            signalEnd(runner);
        }
    }

    /**
     * Checks for null values in the param Map.
     * @param params Parameter Map to check
     * @return Returns true if not all {@link ISharable}s were claimed
     */
    public static boolean containsNull(final Map<String, Object> params) {
        for (Object value : params.values()) {
            if (value == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to claims all {@link ISharable}s and stores the values in a parameter map.  If a {@link ISharable} wasn't
     * claimed then the value in the {@link Map} will be null.
     * @param runner {@link ITaskRunner} to attempt to claim {@link ISharable} resources for
     * @return Returns the Objects from the {@link ISharable} map
     */
    public static Map<String, Object> claimAllRequired(ITaskRunner runner) {
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, ISharable<?>> entry : runner.getRequiredMap().entrySet()) {
            params.put(entry.getKey(), entry.getValue().claim(runner));
        }
        return params;
    }

    /**
     * Releases all {@link ISharable}s
     * @param runner {@link ITaskRunner} release the claims on the {@link ISharable} resources for
     */
    public static void releaseAll(ITaskRunner runner) {
        for (ISharable<?> sharable : runner.getRequiredMap().values()) {
            if (sharable.isLocked()) {
                sharable.release(runner);
            }
        }
    }

    /**
     * Checks if the necessary {@link ISharable}s are available for processing this task.  This does not claim the
     * {@link ISharable}s!
     * @param runner {@link ITaskRunner} with required {@link ISharable} resources
     * @return Returns true if the {@link ISharable}s are available.
     */
    public static boolean areRequiredAvailable(ITaskRunner runner) {
        if (runner.getRequiredMap() != null) {
            for (ISharable sharable : runner.getRequiredMap().values()) {
                if (sharable.isLocked()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Converts a {@link ISharable} {@link Map} into a String
     * @param requiredMap A {@link Map} of String, {@link ISharable}
     * @return Returns a {@link Map} of String, {@link ISharable} into a String for logging
     */
    public static String requiredMapToString(Map<String, ISharable<?>> requiredMap) {
        StringBuilder sb = new StringBuilder("[");
        if (requiredMap != null) {
            for (Map.Entry<String, ISharable<?>> entry : requiredMap.entrySet()) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                sb.append("{").append(entry.getKey()).append(", ").append(entry.getValue().toString()).append("}");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Returns the amount of time the dependents took to complete their tasks as a collective
     * @param runner {@link ITaskRunner} with dependent {@link ITaskRunner}(s)
     * @return Returns the amount of time the dependents took to complete their tasks as a collective
     */
    public static long getTotalDependentTime(ITaskRunner runner) {
        long dependentTime = 0;
        if (runner.getDependents() != null) {
            for (ITaskRunner dependent : runner.getDependents()) {
                dependentTime += dependent.getTotalTime();
            }
        }
        return dependentTime;
    }

    /**
     * Finds the most important {@link State} held by one of the dependents
     * @param runner {@link ITaskRunner} with dependent {@link ITaskRunner}(s)
     * @return Returns the highest importance {@link State} held by one of the dependents
     */
    public static State getHighestPriorityState(ITaskRunner runner) {
        State result = State.DONE;
        for (ITaskRunner dependent : runner.getDependents()) {
            State dependentState = updateState(dependent);
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
                    runner.setExceptionThrown(dependent.getExceptionThrown());
                    return State.ERROR;
                case DONE:
                    // This dependent is done
                    break;
            }
        }
        return result;
    }

    /**
     * Updates and returns the latest state of this {@link ITaskRunner}.
     * @param runner {@link ITaskRunner} with a task
     * @return Returns the current state of this {@link ITaskRunner}.
     */
    public static synchronized State updateState(ITaskRunner runner) {
        final State state = runner.getState();
        if (State.DONE.equals(state) || State.ERROR.equals(state)) {
            // Task has already been attempted or completed
            return state;
        }
        // Check for dependents have to be completed
        if (runner.getDependents() != null) {
            // See if we care about any dependent states
            State dependentState = WorkUnitHelper.getHighestPriorityState(runner);

            switch (dependentState) {
                case WAITING_DEPENDENT:
                case WAITING_RESOURCE:
                case READY:
                case INITIATED:
                case IN_PROGRESS:
                    // Wait for all dependents to be done
                    runner.setState(State.WAITING_DEPENDENT);
                    return runner.getState();
                case ERROR:
                    // If a dependent found an error then this task cannot be processed
                    runner.setState(State.ERROR);
                    runner.exceptionHandling(runner.getExceptionThrown(), null);
                    return runner.getState();
                case DONE:
                    // This dependent is done
                    break;
            }
        }
        // Check if resources are available
        if (!WorkUnitHelper.areRequiredAvailable(runner)) {
            runner.setState(State.WAITING_RESOURCE);
            return runner.getState();
        }

        // Nothing in the way of starting this task
        runner.setState(State.READY);
        return runner.getState();
    }

    /**
     * Notifies the {@link IProcessPlant} that this {@link ITaskRunner} is no longer processing
     * @param runner {@link ITaskRunner} with a task that won't be processing anymore
     */
    public static void signalEnd(ITaskRunner runner) {
        runner.getProcessPlant().signalComplete(runner);
    }

    /**
     * Returns true if this {@link ITaskRunner} already has a thread working on it
     * @param runner {@link ITaskRunner} with a task
     * @return Returns true if this {@link ITaskRunner} already has a thread working on it
     */
    public static synchronized boolean isThreadClaimed(ITaskRunner runner) {
        return runner.getStartTime() != null;
    }

    /**
     * Creates a multi-line String representation of an {@link ITaskRunner} and it's dependents
     * @param runner {@link ITaskRunner} to use as the root or last task to be performed
     * @return Returns a multi-line String representation of an {@link ITaskRunner} and it's dependents
     */
    public static String toString(ITaskRunner runner) {
        StringBuilder sb = new StringBuilder(runner.getName()).append(" - ").append(runner.getState());
        if (runner.getTaskTime() > -1) {
            sb.append(" in ").append(runner.getTaskTime()).append(" ms (Total: ").append(runner.getTotalTime()).append(" ms)");
        }
        return sb.toString();
    }

    /**
     * If the given map is null then a new instantiated map is returned, otherwise the given map is returned
     * @param map Map to check
     * @param <K> Key class
     * @param <V> Value class
     * @return Returns the given map or a newly instantiated map if the given map was null
     */
    public static <K, V> Map<K, V> verifyMap(Map<K, V> map) {
        if (map != null) {
            return map;
        } else {
            return new HashMap<>();
        }
    }
}
