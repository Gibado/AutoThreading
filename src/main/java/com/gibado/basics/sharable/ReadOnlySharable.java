package com.gibado.basics.sharable;

import com.gibado.basics.workunit.ITaskRunner;

/**
 * Represents a {@link Sharable} that is read only so thread protection isn't needed.
 * @param <T> Object type
 */
public class ReadOnlySharable<T> extends Sharable<T> {

	/**
	 * An object wrapper that protects an object that might be shared between threads
	 *
	 * @param value Object that could be used by multiple threads
	 */
	public ReadOnlySharable(T value) {
		super(value);
	}

	/**
	 * Returns the object value
	 * @return Returns the object value
	 */
	@Override
	public synchronized T claim(ITaskRunner runner) {
		T value = super.claim(runner);
		this.release(runner);
		return value;
	}
}
