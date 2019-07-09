package com.gibado.basics;

import com.gibado.basics.sharable.ISharable;
import com.gibado.basics.sharable.SharableMap;
import com.gibado.basics.workunit.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static com.gibado.basics.workunit.WorkUnitHelper.isThreadClaimed;
import static com.gibado.basics.workunit.WorkUnitHelper.updateState;

/**
 * Object to initiate {@link WorkUnit} tasks
 */
public class ProcessPlant implements IProcessPlant {
	private ThreadPoolExecutor pool;
	private long timeout = -1; // TODO Add in timeout option
	private Map<String, ISharable<?>> resourceMap;

	/**
	 * Creates a Process plant that will attempt to run as many {@link WorkUnit}s concurrently as possible.
	 * @param threadCount Maximum number of {@link WorkUnit}s to process at one time
	 */
	public ProcessPlant(int threadCount) {
		pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
	}

	@Override
	public ITaskRunner queueTask(ITask task) {
		ITaskRunner runner = prepareTask(task);
		queueWorkUnit(runner);
		return runner;
	}

	@Override
	public void queueWorkUnit(ITaskRunner runner) {
        runner.setProcessPlant(this);
		if (State.READY.equals(updateState(runner))) {
			// This WorkUnit is ready to start working
			pool.execute(runner);

			if (runner.getParent() != null) {
				State workUnitState = updateState(runner);
				while (!State.ERROR.equals(workUnitState) && !State.DONE.equals(updateState(runner))) {
					try {
						// Give the other threads a chance to work
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					workUnitState = updateState(runner);
				}
			}
		} else {
			// This WorkUnit needs other WorkUnit(s) to be done first
			for (ITaskRunner dependent : runner.getDependents()) {
				// Check if any of the dependents are ready
				queueWorkUnit(dependent);
			}
		}
	}

	@Override
	public void signalComplete(ITaskRunner runner) {
	    ITaskRunner parent = runner.getParent();
	    // If there's no parent then this line of work is done
        if(parent != null) {
            State parentState = updateState(parent);
		    if (State.ERROR.equals(updateState(runner))) {
		    	// This will cascade the Error state up this line of work
                signalComplete(parent);
		    } else {
		        if (!isThreadClaimed(parent) && (State.READY.equals(parentState) || State.WAITING_RESOURCE.equals(parentState))) {
		        	// Starts working on the parent WorkUnit
                    pool.execute(parent);
                }
            }
		}
	}

	@Override
	public void addAllResources(Map<String, ISharable<?>> resourceMap) {
		this.resourceMap = WorkUnitHelper.verifyMap(this.resourceMap);
		this.resourceMap.putAll(resourceMap);
	}

	@Override
	public void addSharableMap(SharableMap sharableMap) {
		this.addAllResources(sharableMap.getResourceMap());
	}

	@Override
	public void addResource(String key, ISharable<?> resource) {
		resourceMap = WorkUnitHelper.verifyMap(resourceMap);
		resourceMap.put(key, resource);
	}

	/**
	 * Converts an {@link ITask} into an {@link ITaskRunner}
	 * @param task Task to prepare for processing
	 * @return Returns an {@link ITaskRunner} based on the given {@link ITask}
	 */
	private ITaskRunner prepareTask(ITask task) {
		Collection<ITask> dependents = task.getDependents();
		// Convert the task into a runner
		ITaskRunner runner = new WorkUnitShell(task);
		// Connect required resources to the runner
		for (String key : task.getResourceKeys()) {
			runner.addResource(key, this.resourceMap.get(key));
		}

		// Convert all sub-tasks into runners and connect them to the new runner
		List<ITaskRunner> dependentRunners = new ArrayList<>(dependents.size());
		for (ITask dependent : dependents) {
			ITaskRunner dependentRunner = prepareTask(dependent);
			// Set reference back to parent task
			dependentRunner.setParent(runner);
			dependentRunners.add(dependentRunner);
		}
		runner.setDependents(dependentRunners);
		return runner;
	}
}
