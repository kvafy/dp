// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/01

package bna.bnlib.learning;

import bna.bnlib.*;
import bna.bnlib.misc.Toolkit;


/**
 * Static class for learning parameters of a network based on given dataset.
 */
public class ParameterLearner {

    /**
     * Produce a new network with identical structure but with CPTs computed by maximum likelihood estimation.
     * @throws BNLibInconsistentVariableSetsException When network and dataset contain different variables.
     */
    public static BayesianNetwork learnMLE(BayesianNetwork bnOrig, Dataset dataset) throws BNLibInconsistentVariableSetsException {
        if(!Toolkit.isSubset(dataset.getVariables(), bnOrig.getVariables()))
            throw new BNLibInconsistentVariableSetsException("Some variables of the network aren't present in the dataset.");
        
        BayesianNetwork bnLearnt = bnOrig.copyStructureWithEmptyCPDs();
        for(Node node : bnLearnt.getNodes()) {
            Variable[] nodeFactorScope = Toolkit.union(new Variable[]{node.getVariable()}, node.getParentVariables());
            Factor nodeFactorUnnormalized = dataset.computeFactor(nodeFactorScope);
            Factor nodeFactor = nodeFactorUnnormalized.normalizeByFirstNVariables(1);
            bnLearnt.setCPT(node.getVariable().getName(), nodeFactor);
        }
        return bnLearnt;
    }
    
    /**
     * Produce a new network with identical structure but with CPTs computed by Bayesian estimation with uniform prior.
     * @param alpha Equivalent sample size.
     * @throws BNLibInconsistentVariableSetsException When network and dataset contain different variables.
     */
    public static BayesianNetwork learnBayesianEstimationUniform(BayesianNetwork bnOrig, Dataset dataset, double alpha) throws BNLibInconsistentVariableSetsException {
        if(!Toolkit.isSubset(dataset.getVariables(), bnOrig.getVariables()))
            throw new BayesianNetworkRuntimeException("Some variables of the network aren't present in the dataset.");
        
        BayesianNetwork bnLearnt = bnOrig.copyStructureWithEmptyCPDs();
        for(Node node : bnLearnt.getNodes()) {
            Variable[] nodeFactorScope = Toolkit.union(new Variable[]{node.getVariable()}, node.getParentVariables());
            Factor nodeFactorRealCounts = dataset.computeFactor(nodeFactorScope);
            double alphaUniform = alpha / Toolkit.cardinality(nodeFactorScope);
            Counter pseudoCounts = new Counter(nodeFactorScope, alphaUniform);
            Factor nodeFactorPseudoCounts = pseudoCounts.toFactor();
            Factor nodeFactor = Factor.sumFactors(new Factor[] {nodeFactorRealCounts, nodeFactorPseudoCounts});
            nodeFactor = nodeFactor.normalizeByFirstNVariables(1);
            bnLearnt.setCPT(node.getVariable().getName(), nodeFactor);
        }
        return bnLearnt;
    }
}
