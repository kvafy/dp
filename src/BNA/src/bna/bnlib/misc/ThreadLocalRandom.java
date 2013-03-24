// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/21

package bna.bnlib.misc;

import java.util.HashMap;
import java.util.Random;


/**
 * Custom implementation of java.util.concurrent.ThreadLocalRandom from Java7.
 */
public class ThreadLocalRandom {
    private static HashMap<Long, Random> theadRandomMap = new HashMap<Long, Random>();
    
    public static Random current() {
        // Java 6 - hand-made ThreadLocalRandom
        Long threadID = Thread.currentThread().getId();
        Random rand = theadRandomMap.get(threadID);
        if(rand == null) {
            rand = new Random();
            theadRandomMap.put(threadID, rand);
        }
        return rand;
        
        // Java 7
        /*return java.util.concurrent.ThreadLocalRandom.current();*/
    }
}
