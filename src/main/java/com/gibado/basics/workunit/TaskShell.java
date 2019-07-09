package com.gibado.basics.workunit;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Abstract class for an {@link ITask} that can be extended from
 */
public abstract class TaskShell implements ITask {
    private static final String name = "Default Task Shell";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void performTask(Map<String, ?> params) {
        System.out.println("Default task not implemented");
    }

    @Override
    public void exceptionHandling(Exception exception, Map<String, ?> params) {
        // Do nothing
    }

    @Override
    public Collection<String> getResourceKeys() {
        return Collections.EMPTY_LIST;
    }
}
