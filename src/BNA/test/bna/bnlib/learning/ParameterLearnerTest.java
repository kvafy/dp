// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
 // Author:  David Chaloupka (xchalo09)
// Created: 2013/03/09
 
package bna.bnlib.learning;

import bna.bnlib.misc.Toolkit;
import bna.bnlib.*;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author David Chaloupka
 */
public class ParameterLearnerTest {
    
    private Variable coinVar;
    private BayesianNetwork coinBn;
    private int[] recordHeads, recordTails;
    
    
    public ParameterLearnerTest() throws BayesianNetworkException {
        this.coinVar = new Variable("COIN", new String[]{"heads", "tails"});
        this.recordHeads = new int[]{0};
        this.recordTails = new int[]{1};
        this.coinBn = new BayesianNetwork(new Variable[]{this.coinVar});
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of learnMLE method, of class ParameterLearner.
     */
    @Test
    public void testLearnMLE() {
        System.out.println("learnMLE");
        Dataset dataset = new Dataset(this.coinBn.getVariables());
        Factor factorResult;
        // 1x heads, 0x tails => P(h) = 1, P(t) = 0
        dataset.addRecord(this.recordHeads);
        factorResult = ParameterLearner.learnMLE(this.coinBn, dataset).getNode(this.coinVar).getFactor();
        assertEquals(factorResult.getProbability(this.recordHeads), (1.0) / (1 + 0), Toolkit.DOUBLE_EPS);
        assertEquals(factorResult.getProbability(this.recordTails), (0.0) / (1 + 0), Toolkit.DOUBLE_EPS);
        // 2x heads, 0x tails => P(h) = 1, P(t) = 0
        dataset.addRecord(this.recordHeads);
        factorResult = ParameterLearner.learnMLE(this.coinBn, dataset).getNode(this.coinVar).getFactor();
        assertEquals(factorResult.getProbability(this.recordHeads), (2.0) / (2 + 0), Toolkit.DOUBLE_EPS);
        assertEquals(factorResult.getProbability(this.recordTails), (0.0) / (2 + 0), Toolkit.DOUBLE_EPS);
        // 2x heads, 3x tails => P(h) = 2/5, P(t) = 3/5
        dataset.addRecord(this.recordTails);
        dataset.addRecord(this.recordTails);
        dataset.addRecord(this.recordTails);
        factorResult = ParameterLearner.learnMLE(this.coinBn, dataset).getNode(this.coinVar).getFactor();
        assertEquals(factorResult.getProbability(this.recordHeads), (2.0) / (2 + 3), Toolkit.DOUBLE_EPS);
        assertEquals(factorResult.getProbability(this.recordTails), (3.0) / (2 + 3), Toolkit.DOUBLE_EPS);
    }

    /**
     * Test of learnBayesianEstimationUniform method, of class ParameterLearner.
     */
    @Test
    public void testLearnBayesianEstimationUniform() {
        System.out.println("learnBayesianEstimationUniform");
        final double alpha = 1; // equivalent sample size
        Dataset dataset = new Dataset(this.coinBn.getVariables());
        Factor factorResult;
        // 1x heads, 0x tails => P(h) = (1 + 0.5) / (1 + 1) , P(t) = 0.5 / (1 + 1)
        dataset.addRecord(this.recordHeads);
        factorResult = ParameterLearner.learnBayesianEstimationUniform(this.coinBn, dataset, alpha).getNode(this.coinVar).getFactor();
        assertEquals(factorResult.getProbability(this.recordHeads), (1 + alpha/2) / (1 + 0 + alpha), Toolkit.DOUBLE_EPS);
        assertEquals(factorResult.getProbability(this.recordTails), (0 + alpha/2) / (1 + 0 + alpha), Toolkit.DOUBLE_EPS);
        // 2x heads, 0x tails => P(h) = (2 + 0.5) / (2 + 1), P(t) = 0.5 + (2 + 1)
        dataset.addRecord(this.recordHeads);
        factorResult = ParameterLearner.learnBayesianEstimationUniform(this.coinBn, dataset, alpha).getNode(this.coinVar).getFactor();
        assertEquals(factorResult.getProbability(this.recordHeads), (2 + alpha/2) / (2 + 0 + alpha), Toolkit.DOUBLE_EPS);
        assertEquals(factorResult.getProbability(this.recordTails), (0 + alpha/2) / (2 + 0 + alpha), Toolkit.DOUBLE_EPS);
        // 2x heads, 3x tails => P(h) = 2/5, P(t) = 3/5
        dataset.addRecord(this.recordTails);
        dataset.addRecord(this.recordTails);
        dataset.addRecord(this.recordTails);
        factorResult = ParameterLearner.learnBayesianEstimationUniform(this.coinBn, dataset, alpha).getNode(this.coinVar).getFactor();
        assertEquals(factorResult.getProbability(this.recordHeads), (2 + alpha/2) / (2 + 3 + alpha), Toolkit.DOUBLE_EPS);
        assertEquals(factorResult.getProbability(this.recordTails), (3 + alpha/2) / (2 + 3 + alpha), Toolkit.DOUBLE_EPS);
    }
}
