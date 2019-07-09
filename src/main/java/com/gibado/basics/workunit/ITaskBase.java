package com.gibado.basics.workunit;

import com.gibado.basics.sharable.ISharable;

import java.util.Collection;
import java.util.Map;

public interface ITaskBase {
    /**
     * Returns the name of this {@link ITaskRunner}
     * @return Returns the name of this {@link ITaskRunner}
     */
    String getName();

    /**
     * This method will be called to perform the described unit of work.
     * @param params {@link Map} containing values based on the {@link ISharable} added to this {@link WorkUnit}
     */
    void performTask(Map<String, ?> params);

    /**
     * This method is called if an exception occurs during the performTask method before the exception is thrown again
     * @param exception Exception that was thrown during the performTask method
     * @param params {@link Map} containing values based on the {@link ISharable} added to this {@link WorkUnit}
     */
    void exceptionHandling(Exception exception, Map<String, ?> params);

    /**
     * Returns a list of keys that will be used during this task
     * @return Returns a list of keys that will be used during this task
     */
    Collection<String> getResourceKeys();
}
