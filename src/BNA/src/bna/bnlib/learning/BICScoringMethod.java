// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/10

package bna.bnlib.learning;

import bna.bnlib.*;


/**
 * Class implementing the BIC score.
 * Super-class takes care of caching of delta family scores.
 */
public class BICScoringMethod extends DecomposableScoringMethod {
    
    public BICScoringMethod(DatasetInterface dataset) {
        super(dataset);
    }
    
    @Override
    public double absoluteScore(BayesianNetwork bn) {
        // the likelihood-score part
        double likelihoodScore = 0;
        for(Node node : bn.getNodes())
            likelihoodScore += this.computeFamilyScore(node);
        // the dimension part
        double structurePenalization = this.computeComplexityPenalty(bn);
        return likelihoodScore + structurePenalization;
    }
    
    @Override
    /**
     * Compute the mutual information between node and its parents from the dataset.
     * Cache is not used at all.
     */
    protected double computeFamilyScore(Node n) {
        double N = this.dataset.getSize();
        if(n.getParentCount() == 0) // TODO really works like this?
            return 0.0;
        Variable[] nSet = {n.getVariable()};
        Variable[] parentsSet = n.getParentVariables();
        return N * dataset.mutualInformation(nSet, parentsSet);
    }
    
    @Override
    /** In the BIC formula, how does the dimension term change? */
    protected double computeIncreaseOfComplexityPenalty(BayesianNetwork bn, AlterationAction action) throws BNLibIllegalStructuralModificationException {
        double penaltyOld = this.computeComplexityPenalty(bn);
        action.apply(bn);
        double penaltyNew = this.computeComplexityPenalty(bn);
        action.undo(bn);
        return penaltyNew - penaltyOld;
    }
    
    private double computeComplexityPenalty(BayesianNetwork bn) {
        double N = this.dataset.getSize();
        double dim = bn.getNetworkDimension();
        return -Math.log(N) / 2 * dim;
    }
}
