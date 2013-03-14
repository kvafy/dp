// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/14

package bna.bnlib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author David Chaloupka
 */
public class LRUCacheTest {
    
    public LRUCacheTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of get method, of class LRUCache.
     */
    @Test
    public void testAsAWhole() {
        Random rand = new Random();
        // what data can be inserted
        Integer[] keys = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        String[] values = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        // parameters of the test
        final int OPERATION_COUNT = 100 * 1000;
        final int CAPACITY = 6;
        
        LRUCache<Integer,String> cache = new LRUCache<Integer,String>(CAPACITY);
        SimpleLRU<Integer,String> referentialCache = new SimpleLRU<Integer,String>(CAPACITY);
        
        System.out.println("complex test");
        for(int i = 0 ; i < OPERATION_COUNT ; i++) {
            int dataIndex = rand.nextInt(keys.length);
            Integer k = keys[dataIndex];
            String v = values[dataIndex];
            if(referentialCache.keys.contains(k)) {
                // try to retrieve somethink
                //System.out.println("try to retrieve " + k.toString());
                String vFromCache = cache.get(k),
                       vFromReferentialCache = referentialCache.get(k);
                boolean equalResult = (vFromCache == null && vFromReferentialCache == null)
                        || vFromReferentialCache.equals(vFromCache);
                assertTrue(equalResult);
            }
            else {
                // try to insert
                //System.out.println("try to insert <" + k.toString() + "," + v + ">");
                cache.put(k, v);
                referentialCache.put(k, v);
            }
            assertEquals(referentialCache.size(), cache.size());
            assertTrue(this.equalCaches(referentialCache, cache));
        }
    }
    
    private <K,V> boolean equalCaches(SimpleLRU<K,V> simple, LRUCache<K,V> complex) {
        Iterator<V> simpleIter = simple.values.iterator(),
                    complexIter = complex.iterator();
        while(true) {
            if(!simpleIter.hasNext() && !complexIter.hasNext())
                return true;
            if(simpleIter.hasNext() ^ complexIter.hasNext())
                fail("One iterator is longer than the other.");
            V simpleVal = simpleIter.next(),
              complexVal = complexIter.next();
            if(!simpleVal.equals(complexVal)) {
                String complexDump = "";
                for(V v : complex)
                    complexDump += v.toString() + ", ";
                String simpleDump = "";
                for(V v : simple.values)
                    simpleDump += v.toString() + ", ";
                String msg = "Values on the same indices don't match\n"
                           + "complex dump: " + complexDump + "\n"
                           + "simple dump: " + simpleDump;
                fail(msg);
            }
        }
    }
}


class SimpleLRU<K,V> {
    final int capacity;
    ArrayList<K> keys = new ArrayList<K>();
    ArrayList<V> values = new ArrayList<V>();
    
    public SimpleLRU(int capacity) {
        this.capacity = capacity;
    }
    
    public V get(K key) {
        int index = this.keys.indexOf(key);
        if(index == -1)
            return null;
        else {
            // move to head
            V value = this.values.get(index);
            this.keys.remove(index);
            this.values.remove(index);
            this.keys.add(0, key);
            this.values.add(0, value);
            return value;
        }
    }
    
    public void put(K key, V value) {
        if(this.keys.contains(key)) {
            int oldIndex = this.keys.indexOf(key);
            this.keys.remove(oldIndex);
            this.values.remove(oldIndex);
        }
        this.keys.add(0, key);
        this.values.add(0, value);
        
        while(this.keys.size() > this.capacity) {
            int lastIndex = this.keys.size() - 1;
            this.keys.remove(lastIndex);
            this.values.remove(lastIndex);
        }
    }
    
    public int size() {
        return this.keys.size();
    }
}