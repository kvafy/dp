// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/14

package bna.bnlib;

import java.util.HashMap;
import java.util.Iterator;


/**
 * LRU associative cache.
 */
public class LRUCache<K,V> implements Iterable<V> {
    int capacity;
    HashMap<K, Pair<V,DListNode<K>>> itemMap;
    DList<K> itemRecencyList; // head ~ the most recently used, tail ~ the least recently used
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.itemMap = new HashMap<K, Pair<V,DListNode<K>>>(capacity);
        this.itemRecencyList = new DList<K>();
    }
    
    public V get(K key) {
        Pair<V,DListNode<K>> pair = this.itemMap.get(key);
        if(pair != null) {
            this.itemRecencyList.relocateToHead(pair.second); // update timestamp
            return pair.first;
        }
        else
            return null;
    }
    
    public void put(K key, V value) {
        DListNode<K> node = this.itemRecencyList.addFirst(key);
        Pair<V,DListNode<K>> pair = new Pair<V,DListNode<K>>(value, node);
        this.itemMap.put(key, pair);
        this.flushOverTheLimitData();
    }
    
    public int size() {
        return this.itemRecencyList.size;
    }
    
    private void flushOverTheLimitData() {
        while(this.itemRecencyList.size() > this.capacity) {
            K lruKey = this.itemRecencyList.popLast();
            this.itemMap.remove(lruKey);
        }
    }

    @Override
    public Iterator<V> iterator() {
        return new Iterator<V>() {
            DListNode<K> currentNode = itemRecencyList.head;
            
            @Override
            public boolean hasNext() {
                return this.currentNode != null;
            }

            @Override
            public V next() {
                K curKey = this.currentNode.data;
                V curValue = itemMap.get(curKey).first;
                this.currentNode = this.currentNode.next;
                return curValue;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Deletion is not supported.");
            }
        };
    }
    
    class Pair<A,B> {
        final A first;
        final B second;
        
        public Pair(A a, B b) {
            this.first = a;
            this.second = b;
        }
    }
}


class DList<T> {
    DListNode<T> head = null,
                 tail = null;
    int size = 0;

    
    public int size() {
        return this.size;
    }
    
    public DListNode<T> addFirst(T item) {
        DListNode<T> node = new DListNode<T>(item);
        if(this.size == 0) {
            this.head = node;
            this.tail = node;
        }
        else {
            node.next = this.head;
            this.head.prev = node;
            this.head = node;
        }        
        this.size++;
        return node;
    }
    
    public T popLast() {
        if(this.size == 0)
            throw new RuntimeException("Cannot pop from empty list.");
        
        T tailValue = this.tail.data;
        if(this.size == 1) {
            this.head = null;
            this.tail = null;
        }
        else {
            this.tail = this.tail.prev;
            this.tail.next = null;
        }
        this.size--;
        return tailValue;
    }
    
    void relocateToHead(DListNode<T> node) {
        if(node == this.head)
            return;
        // take the node out
        DListNode<T> oldPrev = node.prev,
                     oldNext = node.next;
        if(oldPrev != null)
            oldPrev.next = oldNext;
        if(oldNext != null)
            oldNext.prev = oldPrev;
        // could have been the tail node
        if(this.tail == node)
            this.tail = oldPrev;

        // put before the current head
        node.prev = null;
        node.next = this.head;
        this.head.prev = node;
        this.head = node;
    }
}

class DListNode<T> {
    T data;
    DListNode<T> prev, next;
    
    public DListNode() {
        this(null);
    }
    
    public DListNode(T data) {
        this.data = data;
        this.prev = null;
        this.next = null;
    }
}

