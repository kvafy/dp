// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/10


package bna.bnlib.learning;

import bna.bnlib.*;


/**
 * Class implementing the BIC score.
 */
public class BICScoringMethod extends ScoringMethod {
    public BICScoringMethod(Dataset dataset) {
        super(dataset);
    }
    
    @Override
    public double absoluteScore(BayesianNetwork bn) {
        double N = this.dataset.getSize();
        // the likelihood-score part
        double likelihoodScore = 0;
        for(Node node : bn.getNodes()) {
            if(node.getParentCount() == 0) // TODO really works like this?
                continue;
            likelihoodScore += dataset.mutualInformation(new Variable[]{node.getVariable()}, node.getParentVariables());
        }
        likelihoodScore *= N;
        // the dimension part
        double dim = bn.getDegreesOfFreedomInCPDs();
        double structurePenalization = Math.log(N) / 2 * dim;
        return likelihoodScore - structurePenalization;
    }
    
    @Override
    public double deltaScore(BayesianNetwork bn, AlterationAction action) {
        try {
            // TODO better
            double originalScore = this.absoluteScore(bn);
            action.apply(bn);
            double newScore = this.absoluteScore(bn);
            action.undo(bn);
            return newScore - originalScore;
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
