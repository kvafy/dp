// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/02

package bna.bnlib.learning;

import bna.bnlib.*;


/**
 * Class implementing the likelihood score.
 * This scoring method is not to be used as it doesn't take into account
 * the network complexity.
 */
public class LikelihoodScoringMethod extends DecomposableScoringMethod {
    
    public LikelihoodScoringMethod(DatasetInterface dataset) {
        super(dataset);
    }
    
    @Override
    /**
     * Compute the mutual information between node and its parents from the dataset.
     * Cache is not used at all.
     */
    protected double computeFamilyScore(Node n) {
        int N = this.dataset.getSize();
        if(n.getParentCount() == 0)
            return 0.0;
        Variable[] nSet = {n.getVariable()};
        Variable[] parentsSet = n.getParentVariables();
        return N * dataset.mutualInformation(nSet, parentsSet);
    }
    
    @Override
    protected double computeComplexityPenalty(BayesianNetwork bn) {
        return 0;
    }
}
