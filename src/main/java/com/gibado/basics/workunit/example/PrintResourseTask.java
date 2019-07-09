package com.gibado.basics.workunit.example;

import com.gibado.basics.workunit.ITask;
import com.gibado.basics.workunit.WorkUnit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Prints out the {@link WorkUnit} tree results
 */
public class PrintResourseTask implements ITask {
	private static final String logKey = "log";
	private final String resourceKey;
	private static final String name = "Print sharables task";

	public PrintResourseTask(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void performTask(Map<String, ?> params) {
		Logger logger = (Logger) params.get(logKey);
		Object resource = params.get(resourceKey);
		logger.log(resource.toString());
	}

	@Override
	public void exceptionHandling(Exception exception, Map<String, ?> params) {
		boolean logged = false;
		if (params != null) {
			Logger logger = (Logger) params.get(logKey);
			if (logger != null) {
				logger.log(exception.toString());
				logged = true;
			}
		}

		if (!logged) {
			exception.printStackTrace();
		}
	}

	@Override
	public Collection<String> getResourceKeys() {
		return Arrays.asList(logKey, resourceKey);
	}

	@Override
	public Collection<ITask> getDependents() {
		return Collections.EMPTY_LIST;
	}
}
