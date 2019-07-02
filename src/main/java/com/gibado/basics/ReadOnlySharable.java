package com.gibado.basics;

/**
 * Represents a {@link Sharable} that is read only so thread protection isn't needed.
 * @param <T> Object type
 */
public class ReadOnlySharable<T> extends Sharable<T> {
	/**
	 * An object wrapper that protects a read only object that might be shared between threads
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
	public synchronized T claim(WorkUnit workUnit) {
		T value = super.claim(workUnit);
		this.release(workUnit);
		return value;
	}
}
