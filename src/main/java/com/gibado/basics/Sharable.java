package com.gibado.basics;

/**
 * An object wrapper that protects an object that might be shared between threads
 * @param <T> Object type
 */
public class Sharable<T> {
	private final T value;
	private boolean locked = false;

	/**
	 * An object wrapper that protects an object that might be shared between threads
	 * @param value Object that could be used by multiple threads
	 */
	public Sharable(T value) {
		this.value = value;
	}

	/**
	 * Returns true if this Sharable is currently claimed by a thread and is not available to be edited
	 * @return Returns true if this Sharable is currently claimed by a thread and is not available to be edited
	 */
	public synchronized boolean isLocked() {
		return locked;
	}

	/**
	 * Locks this Sharable so that other threads cannot use it until this is released
	 * @return Returns the object value if it's available, otherwise null is returned
	 */
	public synchronized T claim() {
		if (!isLocked()) {
			locked = true;
			return value;
		} else {
			return null;
		}
	}

	/**
	 * Releases the claim on this Sharable so that other threads can access this again.
	 */
	public synchronized void release() {
		locked = false;
	}
}
