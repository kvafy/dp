// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/09

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
    
    private final double DOUBLE_EPS = 1e-5;
    
    
    public FactorTest() {
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
    
    @Test(expected=BNLibIllegalArgumentException.class)
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
    
    /**
     * Test of marginalize method, of class Factor.
     */
    @Test
    public void testMarginalize() {
        System.out.println("marginalize");
        // marginalize P(RAIN | CLOUDY) over RAIN => every entry should be one
        Factor marginalizedByRain = this.rainGivenCloudyFactor.marginalize(new Variable[]{this.rainVariable});
        assertEquals(marginalizedByRain.getProbability(0), 1.0, this.DOUBLE_EPS); // cloudy low
        assertEquals(marginalizedByRain.getProbability(1), 1.0, this.DOUBLE_EPS); // cloudy medium
        assertEquals(marginalizedByRain.getProbability(2), 1.0, this.DOUBLE_EPS); // cloudy high
        // marginalize P(RAIN | CLOUDY) over CLOUDY
        Factor marginalizedByCloudy = this.rainGivenCloudyFactor.marginalize(new Variable[]{this.cloudyVariable});
        assertEquals(marginalizedByCloudy.getProbability(0), 0.1 + 0.5 + 0.75, this.DOUBLE_EPS); // rain false
        assertEquals(marginalizedByCloudy.getProbability(1), 0.9 + 0.5 + 0.25, this.DOUBLE_EPS); // rain true
        
        // marginalization in a more general factor
        Variable var1 = new Variable("V1", new String[] {"a", "b", "c"}),
                 var2 = new Variable("V2", new String[] {"k", "l"}),
                 var3 = new Variable("V3", new String[] {"x", "y"});
        Variable[] generalScope = {var1, var2, var3};
        double[] generalValues = {
            1, 2, 3,   // a/b/c | k, x
            5, 5, 5,   // a/b/c | l, x
            3, 2, 1,   // a/b/c | k, y
            0, 0, 0,   // a/b/c | l, y
        };
        Factor generalFactor = new Factor(generalScope, generalValues);
        
        Factor marginalizedOverV1 = generalFactor.marginalize(new Variable[]{var1});
        assertEquals(marginalizedOverV1.getProbability(new int[]{0, 0}), 1 + 2 + 3, this.DOUBLE_EPS);
        assertEquals(marginalizedOverV1.getProbability(new int[]{1, 0}), 5 + 5 + 5, this.DOUBLE_EPS);
        assertEquals(marginalizedOverV1.getProbability(new int[]{0, 1}), 3 + 2 + 1, this.DOUBLE_EPS);
        assertEquals(marginalizedOverV1.getProbability(new int[]{1, 1}), 0 + 0 + 0, this.DOUBLE_EPS);
        
        Factor marginalizedOverV1V2 = generalFactor.marginalize(new Variable[]{var2, var1});
        assertEquals(marginalizedOverV1V2.getProbability(new int[]{0}), 1 + 2 + 3 + 5 + 5 + 5, this.DOUBLE_EPS);
        assertEquals(marginalizedOverV1V2.getProbability(new int[]{1}), 3 + 2 + 1 + 0 + 0 + 0, this.DOUBLE_EPS);
        
        Factor marginalizedOverV2V3 = generalFactor.marginalize(new Variable[]{var2, var3});
        assertEquals(marginalizedOverV2V3.getProbability(new int[]{0}), 1 + 5 + 3 + 0, this.DOUBLE_EPS);
        assertEquals(marginalizedOverV2V3.getProbability(new int[]{1}), 2 + 5 + 2 + 0, this.DOUBLE_EPS);
        assertEquals(marginalizedOverV2V3.getProbability(new int[]{2}), 3 + 5 + 1 + 0, this.DOUBLE_EPS);
    }
    
    /**
     * Test of normalize method, of class Factor.
     */
    @Test
    public void testNormalize() {
        System.out.println("normalize");
        // normalization of the P(RAIN | CLOUDY) factor by the all variables changes the factor
        Factor normalized = this.rainGivenCloudyFactor.normalize();
        double factorSum = 3.0; // sum of all entries in rainGivenCloudyFactor
        assertEquals(normalized.getProbability(0), 0.1 / factorSum, this.DOUBLE_EPS);
        assertEquals(normalized.getProbability(1), 0.9 / factorSum, this.DOUBLE_EPS);
        assertEquals(normalized.getProbability(2), 0.5 / factorSum, this.DOUBLE_EPS);
        assertEquals(normalized.getProbability(3), 0.5 / factorSum, this.DOUBLE_EPS);
        assertEquals(normalized.getProbability(4), 0.75 / factorSum, this.DOUBLE_EPS);
        assertEquals(normalized.getProbability(5), 0.25 / factorSum, this.DOUBLE_EPS);
    }
    
    /**
     * Test of normalizeByFirstNVariables method, of class Factor.
     */
    @Test
    public void testNormalizeByFirstNVariables() {
        System.out.println("normalizeByFirstNVariables");
        // normalization of the P(RAIN | CLOUDY) factor by the RAIN variable should't change anything
        Factor normalized = this.rainGivenCloudyFactor.normalizeByFirstNVariables(1);
        for(int[] assignment : this.rainGivenCloudyFactor)
            assertEquals(this.rainGivenCloudyFactor.getProbability(assignment), normalized.getProbability(assignment), this.DOUBLE_EPS);
        
        // normalization in a more general factor
        Variable var1 = new Variable("V1", new String[] {"a", "b", "c"}),
                 var2 = new Variable("V2", new String[] {"k", "l"}),
                 var3 = new Variable("V3", new String[] {"x", "y"});
        Variable[] generalScope = {var1, var2, var3};
        double[] generalValues = {
            1, 2, 3,   // a/b/c | k, x
            5, 5, 5,   // a/b/c | l, x
            3, 2, 1,   // a/b/c | k, y
            0, 0, 0,   // a/b/c | l, y
        };
        Factor generalFactor = new Factor(generalScope, generalValues);
        
        Factor normalizedByFirst1 = generalFactor.normalizeByFirstNVariables(1);
        assertEquals(normalizedByFirst1.getProbability(new int[]{0, 0, 0}), 1.0 / (1 + 2 + 3), this.DOUBLE_EPS);
        assertEquals(normalizedByFirst1.getProbability(new int[]{2, 0, 0}), 3.0 / (1 + 2 + 3), this.DOUBLE_EPS);
        assertEquals(normalizedByFirst1.getProbability(new int[]{1, 1, 0}), 5.0 / (5 + 5 + 5), this.DOUBLE_EPS);
        assertEquals(normalizedByFirst1.getProbability(new int[]{1, 0, 1}), 2.0 / (3 + 2 + 1), this.DOUBLE_EPS);
        assertEquals(normalizedByFirst1.getProbability(new int[]{0, 1, 1}), 0.0, this.DOUBLE_EPS);
        
        Factor normalizedByFirst2 = generalFactor.normalizeByFirstNVariables(2);
        assertEquals(normalizedByFirst2.getProbability(new int[]{0, 0, 0}), 1.0 / (1 + 2 + 3 + 5 + 5 + 5), this.DOUBLE_EPS);
        assertEquals(normalizedByFirst2.getProbability(new int[]{2, 0, 0}), 3.0 / (1 + 2 + 3 + 5 + 5 + 5), this.DOUBLE_EPS);
        assertEquals(normalizedByFirst2.getProbability(new int[]{1, 1, 0}), 5.0 / (1 + 2 + 3 + 5 + 5 + 5), this.DOUBLE_EPS);
        assertEquals(normalizedByFirst2.getProbability(new int[]{1, 0, 1}), 2.0 / (3 + 2 + 1 + 0 + 0 + 0), this.DOUBLE_EPS);
        assertEquals(normalizedByFirst2.getProbability(new int[]{0, 1, 1}), 0.0 / (3 + 2 + 1 + 0 + 0 + 0), this.DOUBLE_EPS);
    }
    
    /**
     * Test of sumFactors method, of class Factor.
     */
    @Test
    public void testSumFactors() {
        System.out.println("sumFactors");
        // test adding two factors with different order of variables in their scopes
        Variable var2 = new Variable("V2", new String[] {"k", "l"}),
                 var3 = new Variable("V3", new String[] {"x", "y"});
        Variable[] scope = {var2, var3},
                   scopeReversed = {var3, var2};
        double[] values = {
            0.0,   // k, x
            0.5,   // l, x
            0.1,   // k, y
            0.3,   // l, y
        };
        double[] valuesReversed = {
            1 - 0.0,   // x, k
            1 - 0.1,   // y, k
            1 - 0.5,   // x, l
            1 - 0.3,   // y, l
        };
        Factor factor = new Factor(scope, values),
               factorReversed = new Factor(scopeReversed, valuesReversed);
        Factor factorSum = Factor.sumFactors(new Factor[]{factor, factorReversed});
        for(int[] assignment : factorSum)
            assertEquals(factorSum.getProbability(assignment), 1, this.DOUBLE_EPS);
    }
}
