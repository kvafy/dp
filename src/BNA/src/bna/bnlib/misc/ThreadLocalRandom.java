// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/21

package bna.bnlib.misc;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;


/**
 * Custom implementation of java.util.concurrent.ThreadLocalRandom from Java 7.
 * Uses WeakHashMap for Thread instances, so that when a Thread is no longer
 * strongly reachable, it can be collected and its associated Random instance
 * becomes garbage as well.
 */
public class ThreadLocalRandom {
    // weak references ensure that the map doesn't contain entries for dead threads
    private static Map<Thread, Random> theadRandomMap = new WeakHashMap<Thread, Random>();
    
    
    /** Get Random instance that is specific to the current thread. */
    public static Random current() {
        // Java 6 - hand-made ThreadLocalRandom using weak references
        Thread currentThread = Thread.currentThread();
        Random rand = theadRandomMap.get(currentThread);
        if(rand == null) {
            rand = new Random();
            theadRandomMap.put(currentThread, rand);
        }
        return rand;
        
        // Java 7
        /*return java.util.concurrent.ThreadLocalRandom.current();*/
    }
}
