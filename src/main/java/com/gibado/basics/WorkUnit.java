package com.gibado.basics;

import java.util.List;

public class WorkUnit implements Runnable {
    private State state;
    private WorkUnit parent;
    private List<WorkUnit> dependents;
    private long startTime;
    private long totalTime;


    public void run() {

    }
}
