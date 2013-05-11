// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/14

package bna.bnlib.misc;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * LRU associative cache.
 * The implementation uses LinkedHashMap which remembers the access order
 * (with "true" specified in the constructor) and with redefined method
 * LinkedHashMap#removeEldestEntry to achieve the desired cache capacity.
 */
public class LRUCache<K,V> implements Iterable<V> {
    private int capacity;
    private LinkedHashMap<K,V> data;
    
    
    /**
     * Create an LRU cache with the given capacity.
     * To determine the least recently used entry last access is used.
     */
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
    
    /** Clear all entries in this cache. */
    public void clear() {
        this.data.clear();
    }
    
    /**
     * Get value associated with given key or null if no such key exists.
     * Accessing an element moves it in from of the que of recently used objects.
     * Note that returning null may also mean that the mapped value actually
     * is null.
     */
    public V get(K key) {
        return this.data.get(key);
    }
    
    /** Put or rewrite the given pair mapping. */
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
