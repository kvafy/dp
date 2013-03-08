// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/xx/xx

package bna.bnlib;


import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.Collection;

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
     * Test of validateAssignment method, of class Toolkit.
     */
    @Test
    public void testValidateAssignment_OK() throws BayesianNetworkException {
        System.out.println("validateAssignment");
        String[] values2 = {"false", "true"},
                 values3 = {"low", "medium", "high"};
        Variable binaryVar = new Variable("binary", values2),
                 ternaryVar = new Variable("ternary", values3);
        Variable[] scope = {binaryVar, ternaryVar};
        double[] values = new double[Toolkit.cardinality(scope)];
        Factor fBinTern = new Factor(new Variable[]{binaryVar, ternaryVar}, values);
        int[] validAssignment = {1, 2};
        fBinTern.getProbability(validAssignment);
    }
    
    /**
     * Test of cardinality method, of class Toolkit.
     */
    @Test(expected=BayesianNetworkRuntimeException.class)
    public void testValidateAssignment_fail1() throws BayesianNetworkException {
        System.out.println("validateAssignment");
        String[] values2 = {"false", "true"},
                 values3 = {"low", "medium", "high"};
        Variable binaryVar = new Variable("binary", values2),
                 ternaryVar = new Variable("ternary", values3);
        Variable[] scope = {binaryVar, ternaryVar};
        double[] values = new double[Toolkit.cardinality(scope)];
        Factor fBinTern = new Factor(new Variable[]{binaryVar, ternaryVar}, values);
        int[] invalidAssignment = {2, 2};
        fBinTern.getProbability(invalidAssignment);
        fail();
    }
    
    /**
     * Test of cardinality method, of class Toolkit.
     */
    @Test(expected=BayesianNetworkRuntimeException.class)
    public void testValidateAssignment_fail2() throws BayesianNetworkException {
        System.out.println("validateAssignment");
        String[] values2 = {"false", "true"},
                 values3 = {"low", "medium", "high"};
        Variable binaryVar = new Variable("binary", values2),
                 ternaryVar = new Variable("ternary", values3);
        Variable[] scope = {binaryVar, ternaryVar};
        double[] values = new double[Toolkit.cardinality(scope)];
        Factor fBinTern = new Factor(new Variable[]{binaryVar, ternaryVar}, values);
        int[] invalidAssignment = {1, 3};
        fBinTern.getProbability(invalidAssignment);
        fail();
    }
    
    /**
     * Test of cardinality method, of class Toolkit.
     */
    @Test(expected=BayesianNetworkRuntimeException.class)
    public void testValidateAssignment_fail3() throws BayesianNetworkException {
        System.out.println("validateAssignment");
        String[] values2 = {"false", "true"},
                 values3 = {"low", "medium", "high"};
        Variable binaryVar = new Variable("binary", values2),
                 ternaryVar = new Variable("ternary", values3);
        Variable[] scope = {binaryVar, ternaryVar};
        double[] values = new double[Toolkit.cardinality(scope)];
        Factor fBinTern = new Factor(new Variable[]{binaryVar, ternaryVar}, values);
        int[] invalidAssignment = {-1, 0};
        fBinTern.getProbability(invalidAssignment);
        fail();
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
     * Test of areEqual method, of class Toolkit.
     */
    @Test
    public void testAreEqual() {
        System.out.println("areEqual");
        Integer[] set1A = {1, 2, 3};
        Integer[] set1B = {3, 1, 2};
        boolean expResult1 = true;
        boolean result1 = Toolkit.areEqual(set1A, set1B);
        assertEquals(expResult1, result1);
        
        Integer[] set2A = {1, 2, 3};
        Integer[] set2B = {3, 1, 0};
        boolean expResult2 = false;
        boolean result2 = Toolkit.areEqual(set2A, set2B);
        assertEquals(expResult2, result2);
        
        Integer[] set3A = {1, 2, 3};
        Integer[] set3B = {3, 1};
        boolean expResult3 = false;
        boolean result3 = Toolkit.areEqual(set3A, set3B);
        assertEquals(expResult3, result3);
        
        Integer[] set4A = {};
        Integer[] set4B = {};
        boolean expResult4 = true;
        boolean result4 = Toolkit.areEqual(set4A, set4B);
        assertEquals(expResult4, result4);
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
     * Test of union method, of class Toolkit.
     */
    @Test
    public void testUnion() {
        System.out.println("union");
        Object[][][] setA_setB_setResult = {
            {{1, 2, 3}, {}, {1, 2, 3}},
            {{}, {1, 2, 3}, {1, 2, 3}},
            {{1.0}, {2.0}, {1.0, 2.0}},
            {{2.0}, {1.0}, {1.0, 2.0}},
        };
        for(Object[][] testCase : setA_setB_setResult) {
            Object[] setA = testCase[0],
                     setB = testCase[1],
                     unionExpected = testCase[2];
            Object[] union = Toolkit.union(setA, setB);
            assertTrue(Toolkit.areEqual(union, unionExpected));
        }
    }
    
    /**
     * Test of difference method, of class Toolkit.
     */
    @Test
    public void testDifference() {
        System.out.println("difference");
        Object[][][] setA_setB_setResult = {
            {{1, 2, 3}, {}, {1, 2, 3}},
            {{}, {1, 2, 3}, {}},
            {{1.0}, {2.0}, {1.0}},
            {{2.0}, {1.0}, {2.0}},
            {{1.0, 2.0}, {1.0}, {2.0}},
            {{2.0}, {2.0}, {}},
        };
        for(Object[][] testCase : setA_setB_setResult) {
            Object[] setA = testCase[0],
                     setB = testCase[1],
                     differenceExpected = testCase[2];
            Object[] difference = Toolkit.difference(setA, setB);
            assertTrue(Toolkit.areEqual(difference, differenceExpected));
        }
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
