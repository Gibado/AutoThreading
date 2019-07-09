package com.gibado.basics.workunit;

import com.gibado.basics.IProcessPlant;
import com.gibado.basics.sharable.ISharable;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This object represents a single unit of work.
 */
public abstract class WorkUnit implements ITaskRunner {
    private String name = "Default WorkUnit";
    private State state;
    private ITaskRunner parent;
    private Collection<ITaskRunner> dependents;
    private Map<String, ISharable<?>> requiredMap;
    private Instant startTime = null;
    private long taskTime = -1;
    private long totalTime = -1;
    private long timeout = 60000; // 1 minute
//    private long timeout = NO_TIMEOUT;
    private IProcessPlant processPlant;
    private Exception exceptionThrown;

    /**
     * Creates a one off task
     */
    public WorkUnit() {
        this(null, null);
    }

    /**
     * Creates a task that must be completed before the given {@link ITaskRunner}
     * @param parent {@link ITaskRunner} to be triggered after this {@link ITaskRunner} is completed
     */
    public WorkUnit(ITaskRunner parent) {
        this(parent, null);
    }

    /**
     * Creates a task that must be completed before the given parent {@link ITaskRunner}, but cannot start until the given
     * dependent {@link ITaskRunner}s have completed
     * @param parent {@link ITaskRunner} to be triggered after this {@link ITaskRunner} is completed
     * @param dependents {@link ITaskRunner}s that must be done before this {@link ITaskRunner} can begin
     */
    public WorkUnit(ITaskRunner parent, List<ITaskRunner> dependents) {
        this.setParent(parent);
        if (dependents != null && !dependents.isEmpty()) {
            this.setDependents(dependents);
            this.setState(State.WAITING_DEPENDENT);
        } else {
            this.setState(State.READY);
        }
    }

    public State getState() { return this.state; }
    public void setState(State state) { this.state = state; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public long getTaskTime() { return taskTime; }
    public void setTaskTime(long taskTime) { this.taskTime = taskTime; }

    public long getTimeout() { return this.timeout; }
    public void setTimeout(long milliseconds) { this.timeout = milliseconds; }

    public long getTotalTime() { return totalTime; }
    public void setTotalTime(long totalTime) { this.totalTime = totalTime; }

    public void setProcessPlant(IProcessPlant processPlant) { this.processPlant = processPlant; }
    public IProcessPlant getProcessPlant() { return this.processPlant; }

    public ITaskRunner getParent() { return parent; }
    public void setParent(ITaskRunner parent) { this.parent = parent; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Exception getExceptionThrown() { return this.exceptionThrown; }
    public void setExceptionThrown(Exception e) { this.exceptionThrown = e; }

    public Map<String, ISharable<?>> getRequiredMap() { return this.requiredMap; }

    public Collection<ITaskRunner> getDependents() { return dependents; }

    public void setDependents(Collection<ITaskRunner> dependents) {
        this.dependents = dependents;
        for (ITaskRunner dependent : dependents) {
            dependent.setParent(this);
        }
    }

    public void setDependents(ITaskRunner ... dependents) {
        setDependents(Arrays.asList(dependents));
    }

    public final void run() {
        WorkUnitHelper.runLogic(this);
    }

    public String toString() {
        return WorkUnitHelper.toString(this);
    }

    public void exceptionHandling(Exception exception, Map<String, ?> params) {}

    @Override
    public void addAllResources(Map<String, ISharable<?>> resourceMap) {
        this.requiredMap = WorkUnitHelper.verifyMap(this.requiredMap);
        this.requiredMap.putAll(resourceMap);
    }

    @Override
    public void addResource(String key, ISharable<?> resource) {
        this.requiredMap = WorkUnitHelper.verifyMap(this.requiredMap);
        requiredMap.put(key, resource);
    }

    @Override
    public Collection<String> getResourceKeys() {
        return null;
    }
}
