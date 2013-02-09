/*
 * // Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
 * // Author:  David Chaloupka (xchalo09)
 * // Created: 2013/02/09
 */
package bna.bnlib;

import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author David Chaloupka
 */
public class FactorTest {
    private Variable cloudyVariable;
    private Variable rainVariable;
    private Factor rainGivenCloudyFactor;
    
    public FactorTest() throws BayesianNetworkException {
        String[] cloudyVariableValues = {"low", "medium", "high"};
        this.cloudyVariable = new Variable("Cloudy", cloudyVariableValues);
        
        String[] rainVariableValues = {"rain", "no_rain"};
        this.rainVariable = new Variable("Rain", rainVariableValues);
        
        Variable[] rainGivenCloudyFactorScope = {this.rainVariable, this.cloudyVariable};
        double[] rainGivenCloudyFactorValues = {
            0.1, 0.9,  // rain F/T | cloudy = low
            0.5, 0.5,  // rain F/T | cloudy = medium
            0.75, 0.25 // rain F/T | cloudy = high
        };
        this.rainGivenCloudyFactor = new Factor(rainGivenCloudyFactorScope, rainGivenCloudyFactorValues);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Test(expected=BayesianNetworkRuntimeException.class)
    public void testInconsistentCreation() {
        System.out.println("inconsistent creation (short CPT)");
        Variable[] rainGivenCloudyFactorScope = {this.rainVariable, this.cloudyVariable};
        double[] invalidCPT = {0, 0.1, 0.2, 0.3, 0.4};
        new Factor(rainGivenCloudyFactorScope, invalidCPT);
    }

    /**
     * Test of getProbability method, of class Factor.
     */
    @Test
    public void testGetProbability_int() {
        System.out.println("getProbability");
        int index = 5;
        Factor instance = this.rainGivenCloudyFactor;
        double expResult = 0.25;
        double result = instance.getProbability(index);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getProbability method, of class Factor.
     */
    @Test
    public void testGetProbability_intArr() {
        System.out.println("getProbability");        
        int[] assignment = {0, 2}; // Rain = false | Cloudy = high
        Factor instance = this.rainGivenCloudyFactor;
        double expResult = 0.75;
        double result = instance.getProbability(assignment);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getCardinality method, of class Factor.
     */
    @Test
    public void testGetCardinality() {
        System.out.println("getCardinality");
        Factor instance = this.rainGivenCloudyFactor;
        int expResult = 2 * 3;
        int result = instance.getCardinality();
        assertEquals(expResult, result);
    }
}
