/*
 * // Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
 * // Author:  David Chaloupka (xchalo09)
 * // Created: 2013/xx/xx
 */
package bna.bnlib;

import java.util.Collection;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author David Chaloupka
 */
public class ToolkitTest {
    
    public ToolkitTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of unique method, of class Toolkit.
     */
    @Test
    public void testUnique_GenericType() {
        System.out.println("unique");
        Integer[] array1 = {1, 2, 3, 4, 5};
        boolean expResult1 = true;
        boolean result1 = Toolkit.unique(array1);
        assertEquals(expResult1, result1);
        
        Integer[] array2 = {1, 2, 3, 4, 5, 1};
        boolean expResult2 = false;
        boolean result2 = Toolkit.unique(array2);
        assertEquals(expResult2, result2);
    }

    /**
     * Test of unique method, of class Toolkit.
     */
    @Test
    public void testUnique_Collection() {
        System.out.println("unique");
        Collection<Integer> collection = new java.util.ArrayList<Integer>();
        collection.add(1);
        collection.add(2);
        collection.add(3);
        boolean expResult1 = true;
        boolean result1 = Toolkit.unique(collection);
        assertEquals(expResult1, result1);
        
        collection.add(3);
        boolean expResult2 = false;
        boolean result2 = Toolkit.unique(collection);
        assertEquals(expResult2, result2);
    }

    /**
     * Test of cardinality method, of class Toolkit.
     */
    @Test
    public void testCardinality() throws BayesianNetworkException {
        System.out.println("cardinality");
        String[] values = {"low", "medium", "high"};
        Variable[] X = {new Variable("A", values),
                        new Variable("B", values)
                       };
        int expResult = 9;
        int result = Toolkit.cardinality(X);
        assertEquals(expResult, result);
    }

    /**
     * Test of isSubset method, of class Toolkit.
     */
    @Test
    public void testIsSubset() {
        System.out.println("isSubset");
        Integer[] superset1 = {1, 2, 3};
        Integer[] subset1 = {2, 3, 4};
        boolean expResult1 = false;
        boolean result1 = Toolkit.isSubset(superset1, subset1);
        assertEquals(expResult1, result1);
        
        Integer[] superset2 = {1, 2, 3};
        Integer[] subset2 = {2, 3};
        boolean expResult2 = true;
        boolean result2 = Toolkit.isSubset(superset2, subset2);
        assertEquals(expResult2, result2);
    }
    
    /**
     * Test of isSubset method, of class Toolkit.
     */
    @Test
    public void testAreDisjoint() {
        System.out.println("areDisjoint");
        Integer[] setA1 = {1, 2, 3};
        Integer[] setB1 = {2, 3, 4};
        boolean expResult1 = false;
        boolean result1 = Toolkit.areDisjoint(setA1, setB1);
        assertEquals(expResult1, result1);
        
        Integer[] setA2 = {1, 2, 3};
        Integer[] setB2 = {4, 5, 6};
        boolean expResult2 = true;
        boolean result2 = Toolkit.areDisjoint(setA2, setB2);
        assertEquals(expResult2, result2);
    }

    /**
     * Test of arrayContains method, of class Toolkit.
     */
    @Test
    public void testArrayContains() {
        System.out.println("arrayContains");
        Integer[] array1 = {1, 2, 3, 4, 5};
        Integer obj1 = 3;
        boolean expResult1 = true;
        boolean result1 = Toolkit.arrayContains(array1, obj1);
        assertEquals(expResult1, result1);
        
        Integer[] array2 = {1, 2, 3, 4, 5};
        Integer obj2 = 6;
        boolean expResult2 = false;
        boolean result2 = Toolkit.arrayContains(array2, obj2);
        assertEquals(expResult2, result2);        
    }
}
