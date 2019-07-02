package com.gibado.basics.workunit;

import com.gibado.basics.Sharable;
import com.gibado.basics.WorkUnit;

import java.util.Map;

/**
 * Appends action text to a {@link StringBuilder}
 */
public class StringAppendWorkUnit extends WorkUnit {
	private static final String appenderKey = "body";
	private static final String logKey = "log";
	private String actionText;

	public StringAppendWorkUnit(String name, String actionText, Sharable<StringBuilder> appendToShare, Sharable<Logger> logShare) {
		addRequiredParam(appenderKey, appendToShare);
		addRequiredParam(logKey, logShare);
		this.setName(name);
		this.actionText = actionText;
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
}
