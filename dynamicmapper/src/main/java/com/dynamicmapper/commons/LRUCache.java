package com.dynamicmapper.commons;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * A quick LRUCache ( Least Recently Used ) extending LinkedHasMap
 *
 * @Authored by walter.dumba
 *
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {


    private final int cacheSize;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true);
        this.cacheSize = capacity;
    }

    /**
     * We override this method so we get LinkedHashMap doing changes automatically on entries
     * @see: super.removeEldestEntry doc
     * @param eldest - the entry least recently use we want map to remove automatically as the cache reaches its full capacity
     * @return <tt>true</tt> if the eldest entry should be removed, <tt>false</tt> if should be retained
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
       return this.size()> this.cacheSize;
    }
}
