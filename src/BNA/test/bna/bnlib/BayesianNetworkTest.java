/*
 * // Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
 * // Author:  David Chaloupka (xchalo09)
 * // Created: 2013/xx/xx
 */
package bna.bnlib;

import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author David Chaloupka
 */
public class BayesianNetworkTest {
    private final String SPRINKLER_NET_PATH = "../../networks/sprinkler.net";
    private final String CYCLIC_NET_PATH = "../../networks/cycle.net";
    
    public BayesianNetworkTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of loadFromFile method, of class BayesianNetwork.
     */
    @Test(expected=BNLibIOException.class)
    public void testLoadFromFile_NotDAG() throws Exception {
        System.out.println("loadFromFile - not a DAG");
        String filename = CYCLIC_NET_PATH;
        BayesianNetwork.loadFromFile(filename);
    }

    /**
     * Test of getVariable method, of class BayesianNetwork.
     */
    @Test
    public void testGetVariable_OK() {
        System.out.println("getVariable - existing variable");
        BayesianNetwork bn = null;
        try {
            bn = BayesianNetwork.loadFromFile(SPRINKLER_NET_PATH);
        }
        catch(Exception bnex) {
            fail("Sprinkler net threw an exception.");
        }
        bn.getVariable("CLOUDY");
    }
    
    /**
     * Test of getVariable method, of class BayesianNetwork.
     */
    @Test(expected=BNLibNonexistentVariableException.class)
    public void testGetVariable_Fail() {
        System.out.println("getVariable - non-existing variable");
        BayesianNetwork bn = null;
        bn = BayesianNetwork.loadFromFile(SPRINKLER_NET_PATH);
        bn.getVariable("cloudy");
    }

    /**
     * Test of getNode method, of class BayesianNetwork.
     */
    @Test
    public void testGetNode_OK() {
        System.out.println("getNode - existing variable");
        BayesianNetwork bn = null;
        try {
            bn = BayesianNetwork.loadFromFile(SPRINKLER_NET_PATH);
        }
        catch(Exception bnex) {
            fail("Sprinkler net threw an exception.");
        }
        bn.getNode("CLOUDY");
    }
    
    /**
     * Test of getNode method, of class BayesianNetwork.
     */
    @Test(expected=BNLibNonexistentVariableException.class)
    public void testGetNode_Fail() {
        System.out.println("getNode - non-existing variable");
        BayesianNetwork bn = null;
        bn = BayesianNetwork.loadFromFile(SPRINKLER_NET_PATH);
        bn.getNode("cloudy");
    }

    /**
     * Test of getVariablesCount method, of class BayesianNetwork.
     */
    @Test
    public void testGetVariablesCount() {
        System.out.println("getVariablesCount");
        BayesianNetwork instance = null;
        try {
            instance = BayesianNetwork.loadFromFile(SPRINKLER_NET_PATH);
        }
        catch(Exception bnex) {
            fail("Sprinkler net threw an exception.");
        }
        int expResult = 4;
        int result = instance.getVariablesCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of topologicalSort method, of class BayesianNetwork.
     */
    @Test
    public void testTopologicalSort() {
        System.out.println("topologicalSort");
        BayesianNetwork bn = null;
        try {
            bn = BayesianNetwork.loadFromFile(SPRINKLER_NET_PATH);
        }
        catch(Exception bnex) {
            fail("Sprinkler net threw an exception.");
        }
        Variable[] expResultA = {
            bn.getVariable("CLOUDY"),
            bn.getVariable("RAIN"),
            bn.getVariable("SPRINKLER"),
            bn.getVariable("WETGRASS"),
        };
        Variable[] expResultB = {
            bn.getVariable("CLOUDY"),
            bn.getVariable("SPRINKLER"),
            bn.getVariable("RAIN"),
            bn.getVariable("WETGRASS"),
        };
        Variable[] result = bn.topologicalSort();
        boolean ok = Arrays.equals(result, expResultA) || Arrays.equals(result, expResultB);
        assertTrue(ok);
    }
}
