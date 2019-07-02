package com.gibado.basics;

/**
 * An object wrapper that protects an object that might be shared between threads
 * @param <T> Object type
 */
public class Sharable<T> {
	private final T value;
	private WorkUnit claimed = null;

	/**
	 * An object wrapper that protects an object that might be shared between threads
	 * @param value Object that could be used by multiple threads
	 */
	public Sharable(T value) {
		this.value = value;
	}

	/**
	 * Returns true if this {@link Sharable} is currently claimed by a thread and is not available to be edited
	 * @return Returns true if this {@link Sharable} is currently claimed by a thread and is not available to be edited
	 */
	public synchronized boolean isLocked() {
		return claimed != null;
	}

	/**
	 * Locks this Sharable so that other threads cannot use it until this is released
     * @param workUnit The {@link WorkUnit} that is claiming this {@link Sharable}
	 * @return Returns the object value if it's available, otherwise null is returned
	 */
	public synchronized T claim(WorkUnit workUnit) {
		if (!isLocked()) {
			claimed = workUnit;
			return value;
		} else {
			return null;
		}
	}

	/**
	 * Releases the claim on this {@link Sharable} so that other threads can access this again.
     * @param workUnit Must be the original {@link WorkUnit} that claimed this {@link Sharable}
	 */
	public synchronized void release(WorkUnit workUnit) {
	    if (claimed.equals(workUnit)) {
	        claimed = null;
        }
	}
}
