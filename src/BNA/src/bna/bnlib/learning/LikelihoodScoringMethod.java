// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/02

package bna.bnlib.learning;

import bna.bnlib.*;


/**
 * Class implementing the likelihood score.
 */
public class LikelihoodScoringMethod extends ScoringMethod {
    private DatasetInterface dataset;
    
    
    public LikelihoodScoringMethod(DatasetInterface dataset) {
        this.dataset = dataset;
    }
    
    @Override
    public double absoluteScore(BayesianNetwork bn) {
        double N = this.dataset.getSize();
        double score = 0;
        for(Node node : bn.getNodes()) {
            if(node.getParentCount() == 0) // TODO really works like this?
                continue;
            score += dataset.mutualInformation(new Variable[]{node.getVariable()}, node.getParentVariables());
        }
        score *= N;
        return score;
    }
    
    public double deltaScore(BayesianNetwork bn, AlterationAction action) throws BNLibIllegalStructuralModificationException {
        // TODO better
        double originalScore = this.absoluteScore(bn);
        action.apply(bn);
        double newScore = this.absoluteScore(bn);
        action.undo(bn);
        return newScore - originalScore;
    }
}
