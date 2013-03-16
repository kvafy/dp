// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/14

package bna.bnlib.misc;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * LRU associative cache.
 */
public class LRUCache<K,V> implements Iterable<V> {
    private int capacity;
    private LinkedHashMap<K,V> data;
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        // for the LRU we can quite ellegantly use standard class LinkedHashMap
        final float HASH_LOAD_FACTOR = 0.75f; // default from documentation
        final int HASH_CAPACITY = capacity;
        final int HASH_SIZE = (int)(HASH_CAPACITY / HASH_LOAD_FACTOR);
        this.data = new LinkedHashMap<K,V>(HASH_SIZE, HASH_LOAD_FACTOR, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K,V> entry) {
                // this method is invoked always after inserting a new item
                return this.size() > HASH_CAPACITY;
            }
        };
    }
    
    public void clear() {
        this.data.clear();
    }
    
    public V get(K key) {
        return this.data.get(key);
    }
    
    public void put(K key, V value) {
        this.data.put(key, value);
    }
    
    /** Return current number of items in the cache. */
    public int size() {
        return this.data.size();
    }
    
    /** Returns the maximal number of items this LRU cache can hold. */
    public int capacity() {
        return this.capacity;
    }

    @Override
    /** Provides iterator over the values in reversed access order (most recently accessed item is last). */
    public Iterator<V> iterator() {
        return this.data.values().iterator();
    }
}
