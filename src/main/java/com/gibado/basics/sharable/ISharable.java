package com.gibado.basics.sharable;

import com.gibado.basics.workunit.ITaskRunner;

public interface ISharable<T> {
    /**
     * Allows the value to be set once
     * @param value Value to store in this Sharable
     */
    void assignValue(T value);

    /**
     * Returns true if this {@link Sharable} is currently claimed by a thread and is not available to be edited
     * @return Returns true if this {@link Sharable} is currently claimed by a thread and is not available to be edited
     */
    boolean isLocked();

    /**
     * Locks this Sharable so that other threads cannot use it until this is released
     * @param runner The {@link ITaskRunner} that is claiming this {@link Sharable}
     * @return Returns the object value if it's available, otherwise null is returned
     */
    T claim(ITaskRunner runner);

    /**
     * Releases the claim on this {@link Sharable} so that other threads can access this again.
     * @param runner Must be the original {@link ITaskRunner} that claimed this {@link Sharable}
     */
    void release(ITaskRunner runner);
}
