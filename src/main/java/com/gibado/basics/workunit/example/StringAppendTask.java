package com.gibado.basics.workunit.example;

import com.gibado.basics.workunit.ITask;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Appends action text to a {@link StringBuilder}
 */
public class StringAppendTask implements ITask {
    private final String name;
    private static final String logKey = "log";
    private final String appenderKey;
    private final String actionText;

    public StringAppendTask(String name, String actionText, String appenderKey) {
        this.name = name;
        this.actionText = actionText;
        this.appenderKey = appenderKey;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void performTask(Map<String, ?> params) {
        // Grab the params needed
        StringBuilder appender = (StringBuilder) params.get(appenderKey);
        Logger logger = (Logger) params.get(logKey);
        // append
        appender.append(" (").append(actionText).append(")");
        // log
        logger.log(getName());
        logger.log(appender.toString());
    }

    @Override
    public void exceptionHandling(Exception exception, Map<String, ?> params) {
        // Ignore exceptions
    }

    @Override
    public Collection<String> getResourceKeys() {
        return Arrays.asList(appenderKey, logKey);
    }

    @Override
    public Collection<ITask> getDependents() {
        return Collections.EMPTY_LIST;
    }
}
