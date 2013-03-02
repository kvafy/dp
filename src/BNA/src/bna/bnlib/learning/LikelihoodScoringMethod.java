// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/02

package bna.bnlib.learning;

import bna.bnlib.*;


/**
 * Class implementing the likelihood score.
 */
public class LikelihoodScoringMethod extends ScoringMethod {
    public LikelihoodScoringMethod(Dataset dataset) {
        super(dataset);
    }
    
    @Override
    public double absoluteScore(BayesianNetwork bn) {
        double score = 0;
        for(Node node : bn.getNodes()) {
            if(node.getParentCount() == 0) // TODO really works like this?
                continue;
            score += dataset.mutualInformation(new Variable[]{node.getVariable()}, node.getParentVariables());
        }
        return score;
    }
    
    @Override
    public double deltaScore(BayesianNetwork bn, AlterationAction action) {
        try {
            // TODO better
            BayesianNetwork bnModified = new BayesianNetwork(bn);
            action.apply(bnModified);
            return this.absoluteScore(bnModified) - this.absoluteScore(bn);
        }
        catch(BayesianNetworkException bnex) {
            bnex.printStackTrace();
            return 0;
        }
    }
    
    /** This method is needed if we were to implement some caching scheme of scores. */
    @Override
    public void notifyNetworkAlteration(AlterationAction actionTaken) {
        // no cache, so empty implementation
    }
}
