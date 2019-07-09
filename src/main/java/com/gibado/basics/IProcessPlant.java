package com.gibado.basics;

import com.gibado.basics.sharable.ISharable;
import com.gibado.basics.sharable.SharableMap;
import com.gibado.basics.workunit.ITask;
import com.gibado.basics.workunit.ITaskRunner;

import java.util.Map;

public interface IProcessPlant {

    /**
     * Converts the given {@link ITask} into an {@link ITaskRunner} and starts working on it
     * @param task The {@link ITask} to process
     * @return Returns a reference to the {@link ITaskRunner} that is wrapping the given {@link ITask}
     */
    ITaskRunner queueTask(ITask task);

    /**
     * Starts working on the given WorkUnit
     * @param runner {@link ITaskRunner} to process
     */
    void queueWorkUnit(ITaskRunner runner);

    /**
     * Checks if a parent {@link ITaskRunner} was waiting on this {@link ITaskRunner} that is now done processing. If a parent
     * {@link ITaskRunner} exists then this will begin attempting to process the parent {@link ITaskRunner}
     * @param runner {@link ITaskRunner} that has finished processing
     */
    void signalComplete(ITaskRunner runner);

    /**
     * Adds all required {@link ISharable} resources
     * @param resourceMap {@link Map} of {@link ISharable} resources for use during the performTask method
     */
    void addAllResources(Map<String, ISharable<?>> resourceMap);

    /**
     * Adds all required {@link ISharable} resources
     * @param sharableMap {@link SharableMap} containing a {@link Map} of {@link ISharable} resources for use during the performTask method
     */
    void addSharableMap(SharableMap sharableMap);

    /**
     * Adds a {@link ISharable} resource for {@link ITask}s to use during the performTask method
     * @param key Access key
     * @param resource {@link ISharable} resource
     */
    void addResource(String key, ISharable<?> resource);
}
