package com.gibado.basics.workunit.example;

import com.gibado.basics.sharable.Sharable;
import com.gibado.basics.workunit.ITask;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Prints out the {@link com.gibado.basics.workunit.ITaskRunner} tree results
 */
public class PrintResultTask implements ITask {
	private static final String logKey = "log";
	private static final String resourceMapKey = "resourceMap";
	private static final String name = "Print sharables task";

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void performTask(Map<String, ?> params) {
		Logger logger = (Logger) params.get(logKey);
		Map<String, Sharable<?>> resourceMap = (Map<String, Sharable<?>>) params.get(resourceMapKey);
		Collection<?> resources = resourceMap.values();
		resources.remove(logger);
		for (Object resource : resources) {
			logger.log(resource.toString());
		}
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
		return Arrays.asList(logKey, resourceMapKey);
	}

	@Override
	public Collection<ITask> getDependents() {
		return Collections.EMPTY_LIST;
	}
}
