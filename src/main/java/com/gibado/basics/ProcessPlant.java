package com.gibado.basics;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import static com.gibado.basics.WorkUnit.State;

/**
 * Object to initiate {@link WorkUnit} tasks
 */
public class ProcessPlant {
	private ThreadPoolExecutor pool;

	/**
	 * Creates a Process plant that will attempt to run as many {@link WorkUnit}s concurrently as possible.
	 * @param threadCount Maximum number of {@link WorkUnit}s to process at one time
	 */
	public ProcessPlant(int threadCount) {
		pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
	}

	/**
	 * Starts working on the given WorkUnit
	 * @param workUnit {@link WorkUnit} to process
	 */
	public void queueWorkUnit(WorkUnit workUnit) {
        workUnit.setProcessPlant(this);
		if (State.READY.equals(workUnit.updateState())) {
			// This WorkUnit is ready to start working
			pool.execute(workUnit);
		} else {
			// This WorkUnit needs other WorkUnit(s) to be done first
			for (WorkUnit dependent : workUnit.getDependents()) {
				// Check if any of the dependents are ready
				queueWorkUnit(dependent);
			}
		}
	}

	/**
	 * Checks if a parent {@link WorkUnit} was waiting on this {@link WorkUnit} that is now done processing. If a parent
	 * {@link WorkUnit} exists then this will begin attempting to process the parent {@link WorkUnit}
	 * @param workUnit {@link WorkUnit} that has finished processing
	 */
	protected void signalComplete(WorkUnit workUnit) {
	    WorkUnit parent = workUnit.getParent();
	    // If there's no parent then this line of work is done
        if(parent != null) {
            State parentState = parent.updateState();
		    if (State.ERROR.equals(workUnit.updateState())) {
		    	// This will cascade the Error state up this line of work
                signalComplete(parent);
		    } else {
		        if (!parent.isThreadClaimed() && (State.READY.equals(parentState) || State.WAITING_RESOURCE.equals(parentState))) {
		        	// Starts working on the parent WorkUnit
                    pool.execute(parent);
                }
            }
		}
	}
}
