package com.gibado.basics.workunit;

import com.gibado.basics.WorkUnit;

/**
 * Logs information to the console and tracks this for later
 */
public class Logger {
    private StringBuilder completeLog = new StringBuilder();

    public String toString() {
        return completeLog.toString();
    }

    /**
     * Logs a message
     * @param message Message to log
     */
    public void log(String message) {
        completeLog.append(message).append("\n");
        System.out.println(message);
    }

    /**
     * Prints out data for a {@link WorkUnit} tree
     * @param workUnit {@link WorkUnit} root to start with
     */
    public void logWorkUnitTree(WorkUnit workUnit) {
        logWorkUnitTree("", workUnit);
    }

    /**
     * Prints out data for a {@link WorkUnit} tree
     * @param prepend String to note how many levels deep into the tree
     * @param workUnit {@link WorkUnit} root to start with
     */
    private void logWorkUnitTree(String prepend, WorkUnit workUnit) {
        log(prepend + workUnit.toString());
        if (workUnit.getDependents() != null) {
            for (WorkUnit dependent : workUnit.getDependents()) {
                logWorkUnitTree(prepend + "-", dependent);
            }
        }
    }
}
