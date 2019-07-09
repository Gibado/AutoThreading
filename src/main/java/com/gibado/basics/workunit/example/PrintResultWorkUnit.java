package com.gibado.basics.workunit.example;

import com.gibado.basics.sharable.Sharable;
import com.gibado.basics.workunit.WorkUnit;

import java.util.Collection;
import java.util.Map;

/**
 * Prints out the {@link WorkUnit} tree results
 */
public class PrintResultWorkUnit extends WorkUnit {
	private static final String logKey = "log";

	public PrintResultWorkUnit(Sharable<Logger> logShare, Sharable<?> ... printable) {
		addResource(logKey, logShare);
		int index = 0;
		for (Sharable<?> resource : printable) {
			addResource(String.valueOf(index), resource);
			index++;
		}
		setName("Print results");
	}

	@Override
	public void performTask(Map<String, ?> params) {
		Logger logger = (Logger) params.get(logKey);
		Collection<?> resources = params.values();
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
				logger.logWorkUnitTree(this);
				logged = true;
			}
		}

		if (!logged) {
			Logger.logWorkUnitTreeToConsole(this);
		}
	}
}
