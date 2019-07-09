package com.gibado.basics.workunit;

import java.util.Collection;

public interface ITask extends ITaskBase {
    /**
     * Returns any other {@link ITask}s that must be completed before this task can be performed
     * @return Returns any other {@link ITask}s that must be completed before this task can be performed
     */
    Collection<ITask> getDependents();
}
