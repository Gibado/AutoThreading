package com.gibado.basics.sharable;

import com.gibado.basics.workunit.ITaskRunner;

/**
 * An object wrapper that protects an object that might be shared between threads
 * @param <T> Object type
 */
public class Sharable<T> implements ISharable<T> {
	public static final String SHARABLE_TAG = "SHARABLE";

	private T value;
	private ITaskRunner claimed = null;

	/**
	 * An object wrapper that protects an object that might be shared between threads
	 * @param value Object that could be used by multiple threads
	 */
	public Sharable(T value) {
		this.value = value;
	}


	@Override
	public void assignValue(T value) {
		if (this.value == null) {
			this.value = value;
		}
	}

	@Override
	public synchronized boolean isLocked() {
		return claimed != null;
	}

	@Override
	public synchronized T claim(ITaskRunner runner) {
		if (!isLocked()) {
			claimed = runner;
			return value;
		} else {
			return null;
		}
	}

	@Override
	public synchronized void release(ITaskRunner runner) {
	    if (claimed.equals(runner)) {
	        claimed = null;
        }
	}

	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    if (claimed != null) {
	        sb.append(" - ").append(claimed.toString());
        }
	    if (value != null) {
			sb.append(" - ");
			if (value instanceof Sharable) {
				sb.append(SHARABLE_TAG);
			} else {
				sb.append(value.toString());
			}
		}
	    return sb.toString();
    }
}
