package com.gibado.basics.workunit.example;

import com.gibado.basics.workunit.ITaskRunner;
import com.gibado.basics.workunit.WorkUnit;

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
     * Logs a message to the console
     * @param message Message to log
     */
    public static void logToConsole(String message) {
        System.out.println(message);
    }

    /**
     * Prints out data for a {@link ITaskRunner} tree
     * @param workUnit {@link ITaskRunner} root to start with
     */
    public void logWorkUnitTree(ITaskRunner workUnit) {
        logWorkUnitTree("", workUnit);
    }

    /**
     * Prints out data for a {@link ITaskRunner} tree
     * @param prepend String to note how many levels deep into the tree
     * @param workUnit {@link ITaskRunner} root to start with
     */
    private void logWorkUnitTree(String prepend, ITaskRunner workUnit) {
        log(prepend + workUnit.toString());
        if (workUnit.getDependents() != null) {
            for (ITaskRunner dependent : workUnit.getDependents()) {
                logWorkUnitTree(prepend + "-", dependent);
            }
        }
    }

    /**
     * Prints out data for a {@link WorkUnit} tree to the console
     * @param workUnit {@link WorkUnit} root to start with
     */
    public static void logWorkUnitTreeToConsole(ITaskRunner workUnit) {
        logWorkUnitTreeToConsole("", workUnit);
    }

    /**
     * Prints out data for a {@link WorkUnit} tree to the console
     * @param prepend String to note how many levels deep into the tree
     * @param workUnit {@link WorkUnit} root to start with
     */
    private static void logWorkUnitTreeToConsole(String prepend, ITaskRunner workUnit) {
        logToConsole(prepend + workUnit.toString());
        if (workUnit.getDependents() != null) {
            for (ITaskRunner dependent : workUnit.getDependents()) {
                logWorkUnitTreeToConsole(prepend + "-", dependent);
            }
        }
    }
}
