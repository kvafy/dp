// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib.learning;

import bna.bnlib.BayesianNetwork;


/**
 * A general class for structure scoring methods.
 */
public abstract class ScoringMethod {
    /** Compute the overall score of the network structure. */
    public abstract double absoluteScore(BayesianNetwork bn);
}
