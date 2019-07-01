package com.gibado.basics;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessPlant {
	private Stack<WorkUnit> workStack = new Stack<WorkUnit>(); // TODO change this to a tree structure so that WorkUnits don't get stuck behind unrelated ones.
	private ExecutorService pool;
	private static final List<WorkUnit> EMPTY_LIST = new ArrayList<WorkUnit>();

	public ProcessPlant(int threadCount) {
		pool = Executors.newFixedThreadPool(threadCount);
	}

	/**
	 * Starts working on the given WorkUnit
	 * @param workUnit {@link WorkUnit} to process
	 */
	public void queueWorkUnit(WorkUnit workUnit) {
		if (State.READY.equals(workUnit.updateState())) {
			pool.submit(workUnit);
		} else {

			workStack.push(workUnit);

			Queue<WorkUnit> needToQueue = new LinkedList<WorkUnit>(workUnit.getDependents());
			while (!needToQueue.isEmpty()) {
				needToQueue.addAll(broadQueue(needToQueue.poll()));
			}
		}
	}

	/**
	 * Queues up dependent {@link WorkUnit}s using a broad search as apposed to a deep search.
	 * @param workUnit {@link WorkUnit} to queue up
	 * @return Returns the dependent {@link WorkUnit}s of the given {@link WorkUnit}.
	 */
	private List<WorkUnit> broadQueue(WorkUnit workUnit) {
		if (!State.READY.equals(workUnit.updateState())) {
			workStack.push(workUnit);
			return workUnit.getDependents();
		}
		pool.submit(workUnit);
		return EMPTY_LIST;
	}
}
