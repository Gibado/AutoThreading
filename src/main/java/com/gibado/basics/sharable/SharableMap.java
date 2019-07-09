package com.gibado.basics.sharable;

import java.util.HashMap;
import java.util.Map;

/**
 * Object to make organizing {@link ISharable}s easier
 */
public class SharableMap {
    private Map<String, ISharable<?>> resourceMap = new HashMap<>();

    /**
     * Wraps the given objectToShare in a new {@link ISharable} and adds it to the resource map
     * @param key Key to use when adding it to the resource map
     * @param objectToShare Object to wrap in a {@link ISharable}
     * @param <T> Object class
     */
    public <T> void addNewResource(String key, T objectToShare) {
        resourceMap.put(key, new Sharable<>(objectToShare));
    }

    /**
     * Adds the given {@link ISharable} to the resource map
     * @param key Key to use when adding it to the resource map
     * @param sharable {@link ISharable} to add to the resource map
     * @param <T> Object class
     */
    public <T> void addResource(String key, ISharable<T> sharable) {
        resourceMap.put(key, sharable);
    }

    /**
     * Returns the resourceMap that was built
     * @return Returns the resourceMap that was built
     */
    public Map<String, ISharable<?>> getResourceMap() {
        return resourceMap;
    }

    /**
     * Removes all resources that were added to this {@link SharableMap}
     */
    public void clear() {
        this.resourceMap.clear();
    }
}
