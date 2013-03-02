// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/01

package bna.bnlib.learning;

import bna.bnlib.*;


/**
 * Static class for learning parameters of a network based on given dataset.
 */
public class ParameterLearner {

    /** Produce a new network with identical structure but with CPTs computed by maximum likelihood estimation. */
    public static BayesianNetwork learnMLE(BayesianNetwork bnOrig, Dataset dataset) {
        if(!Toolkit.isSubset(dataset.getVariables(), bnOrig.getVariables()))
            throw new BayesianNetworkRuntimeException("Some variables of the network aren't present in the dataset.");
        
        BayesianNetwork bnLearnt = new BayesianNetwork(bnOrig);
        for(Node node : bnLearnt.getNodes()) {
            Variable[] nodeFactorScope = Toolkit.union(new Variable[]{node.getVariable()}, node.getParentVariables());
            Factor nodeFactorUnnormalized = dataset.computeFactor(nodeFactorScope);
            Factor nodeFactor = nodeFactorUnnormalized.normalizeByFirstNVariables(1);
            bnLearnt.setCPT(node.getVariable().getName(), nodeFactor);
        }
        return bnLearnt;
    }
}
