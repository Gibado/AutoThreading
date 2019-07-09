package com.gibado.basics.workunit;

import com.gibado.basics.IProcessPlant;
import com.gibado.basics.sharable.ISharable;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

public interface ITaskRunner extends Runnable, ITaskBase {
    /**
     * Returns the current {@link State} of this {@link ITaskRunner}
     * @return Returns the current {@link State} of this {@link ITaskRunner}
     */
    State getState();

    /**
     * Assigns the given {@link State} to this {@link ITaskRunner}
     * @param state {@link State} this {@link ITaskRunner} should be
     */
    void setState(State state);

    /**
     * Returns start time in milliseconds
     * @return Returns start time in milliseconds
     */
    Instant getStartTime();
    /**
     * Sets the Start time for this {@link ITaskRunner}
     * @param now Instant when this {@link ITaskRunner} started attempting to grab resources
     */
    void setStartTime(Instant now);

    /**
     * Gets the amount of time in milliseconds this {@link ITaskRunner} is allowed to spend attempting to claim its
     * required resources before moving to an error {@link State}.  If this value is -1 then there is no timeout.
     * @return Returns the amount of time in milliseconds this {@link ITaskRunner} is allowed to spend attempting to claim its
     * required resources
     */
    long getTimeout();

    /**
     * Defines how long is acceptable to wait for {@link ISharable} resources.
     * @param milliseconds Time in milliseconds
     */
    void setTimeout(long milliseconds);

    /**
     * Returns how much time in milliseconds was taken to start and perform this task
     * @return Returns how much time in milliseconds was taken to start and perform this task
     */
    long getTaskTime();

    /**
     * Set the amount of time this task took to complete
     * @param taskTime The amount of time this task took to complete
     */
    void setTaskTime(long taskTime);

    /**
     * Returns how much time was taken to start and perform this task and all the tasks it depends on
     * @return Returns how much time was taken to start and perform this task and all the tasks it depends on
     */
    long getTotalTime();

    /**
     * Assigns the amount of time that was taken to start and perform this task and all the tasks it depends on
     * @param totalTime The amount of time that was taken to start and perform this task and all the tasks it depends on
     */
    void setTotalTime(long totalTime);

    /**
     * Returns the {@link Exception} that was thrown by this {@link ITaskRunner} or one of its dependent {@link ITaskRunner}s
     * @return Returns the {@link Exception} that was thrown by this {@link ITaskRunner} or one of its dependent {@link ITaskRunner}s
     */
    Exception getExceptionThrown();

    /**
     * Assigns the given {@link Exception} that was thrown by this {@link ITaskRunner} or one of its dependent {@link ITaskRunner}s
     * @param e The {@link Exception} that was thrown by this {@link ITaskRunner} or one of its dependent {@link ITaskRunner}s
     */
    void setExceptionThrown(Exception e);


    /**
     * Adds all required {@link ISharable} resources
     * @param resourceMap {@link Map} of {@link ISharable} resources for use during the performTask method
     */
    void addAllResources(Map<String, ISharable<?>> resourceMap);

    /**
     * Adds a {@link ISharable} resource for {@link ITask}s to use during the performTask method
     * @param key Access key
     * @param resource {@link ISharable} resource
     */
    void addResource(String key, ISharable<?> resource);

    /**
     * Returns a map of all the required resources needed to perform this {@link ITaskRunner}s task
     * @return Returns a map of all the required resources needed to perform this {@link ITaskRunner}s task
     */
    Map<String, ISharable<?>> getRequiredMap();

    /**
     * Returns all {@link ITaskRunner}s this is dependent on
     * @return Returns all {@link ITaskRunner}s this is dependent on
     */
    Collection<ITaskRunner> getDependents();

    /**
     * Assigns the given {@link ITaskRunner}(s) as a dependent {@link ITaskRunner} that needs to be completed before this
     * {@link ITaskRunner} can perform its task.
     * @param dependents {@link ITaskRunner}(s) that should perform their task(s) before this {@link ITaskRunner}
     */
    void setDependents(ITaskRunner ... dependents);

    /**
     * Assigns the given {@link ITaskRunner}s as dependent {@link ITaskRunner}s that needs to be completed before this
     * {@link ITaskRunner} can perform its task.
     * @param dependents {@link ITaskRunner}s that should perform their tasks before this {@link ITaskRunner}
     */
    void setDependents(Collection<ITaskRunner> dependents);

    /**
     * Returns the {@link ITaskRunner} to be executed after this task is complete
     * @return Returns the {@link ITaskRunner} to be executed after this task is complete
     */
    ITaskRunner getParent();

    /**
     * Assigns a {@link ITaskRunner} to be executed after this task is complete
     * @param parent {@link ITaskRunner} to be executed after this task is complete
     */
    void setParent(ITaskRunner parent);

    /**
     * Returns a reference to the {@link IProcessPlant} that is managing this
     * {@link ITaskRunner}
     * @return Returns a reference to the {@link IProcessPlant} that is
     * managing this {@link ITaskRunner}
     */
    IProcessPlant getProcessPlant();

    /**
     * Reference back to the {@link IProcessPlant} for callback actions
     * @param processPlant {@link IProcessPlant} for callback actions
     */
    void setProcessPlant(IProcessPlant processPlant);

    /**
     * Returns a list of keys that will be used during this task
     * @return Returns a list of keys that will be used during this task
     */
    Collection<String> getResourceKeys();
}
