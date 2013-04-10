// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib.learning;

import bna.bnlib.AlterationAction;
import bna.bnlib.BayesianNetwork;


/**
 * A general class for structure scoring methods (there might be subclasses for BIC, Bayesian etc.).
 * The scoring method works with a notion of "current network" in order to use
 * an efficient caching scheme. Therefore the optimization algorithm needs
 * to notify the scoring method about the new network structure for the next
 * step of local search so that right cache entries can be invalidated.
 */
public abstract class ScoringMethod {
    /** Compute overall score of the network structure. */
    public abstract double absoluteScore(BayesianNetwork bn);
    
    /** Determine how would the score change if we applied given alteration. */
    public abstract double deltaScore(BayesianNetwork bn, AlterationAction action);
    
    /** This method is needed if we were to implement some caching scheme of scores. */
    public abstract void notifyNetworkAlteration(AlterationAction actionTaken);
}
