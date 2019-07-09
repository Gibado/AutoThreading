package com.gibado.basics.workunit;


import com.gibado.basics.IProcessPlant;
import com.gibado.basics.sharable.ISharable;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * A basic object that handles everything other than the actual {@link ITask}
 */
public class WorkUnitShell implements ITaskRunner {
    private ITask task;
    private State state;
    private Instant startTime;
    
    private long timeout = 60000; // 1 minute
    private long taskTime = -1;
    private long totalTime = -1;
    private Exception exceptionThrown;
    private Map<String, ISharable<?>> requiredMap;
    private Collection<ITaskRunner> dependents;
    private ITaskRunner parent;
    private IProcessPlant processPlant;

    public WorkUnitShell(ITask task) {
        this.task = task;
    }

    @Override
    public void run() {
        WorkUnitHelper.runLogic(this);
    }

    @Override
    public void performTask(Map<String, ?> params) {
        task.performTask(params);
    }

    @Override
    public void exceptionHandling(Exception exception, Map<String, ?> params) {
        task.exceptionHandling(exception, params);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setStartTime(Instant now) {
        this.startTime = now;
    }

    @Override
    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }

    @Override
    public void setTimeout(long milliseconds) {
        this.timeout = milliseconds;
    }

    @Override
    public long getTaskTime() {
        return this.taskTime;
    }

    @Override
    public void setTaskTime(long taskTime) {
        this.taskTime = taskTime;
    }

    @Override
    public long getTotalTime() {
        return this.totalTime;
    }

    @Override
    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public Exception getExceptionThrown() {
        return this.exceptionThrown;
    }

    @Override
    public void setExceptionThrown(Exception e) {
        this.exceptionThrown = e;
    }

    @Override
    public void addAllResources(Map<String, ISharable<?>> resourceMap) {
        this.requiredMap = WorkUnitHelper.verifyMap(this.requiredMap);
        this.requiredMap.putAll(resourceMap);
    }

    @Override
    public void addResource(String key, ISharable<?> resource) {
        requiredMap = WorkUnitHelper.verifyMap(requiredMap);
        requiredMap.put(key, resource);
    }

    @Override
    public Map<String, ISharable<?>> getRequiredMap() { return this.requiredMap; }

    @Override
    public Collection<ITaskRunner> getDependents() {
        return this.dependents;
    }

    @Override
    public void setDependents(ITaskRunner... dependents) { this.dependents = Arrays.asList(dependents); }

    @Override
    public void setDependents(Collection<ITaskRunner> dependents) { this.dependents = dependents; }

    @Override
    public ITaskRunner getParent() {
        return this.parent;
    }

    @Override
    public void setParent(ITaskRunner parent) {
        this.parent = parent;
    }

    @Override
    public IProcessPlant getProcessPlant() {
        return this.processPlant;
    }

    @Override
    public void setProcessPlant(IProcessPlant processPlant) {
        this.processPlant = processPlant;
    }

    @Override
    public Collection<String> getResourceKeys() {
        return this.task.getResourceKeys();
    }

    @Override
    public String getName() {
        return this.task.getName();
    }

    @Override
    public String toString() {
        return WorkUnitHelper.toString(this);
    }
}
