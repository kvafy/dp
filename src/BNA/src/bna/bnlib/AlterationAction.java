// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib;


/**
 * Abstract class of an action that takes an existing Bayesian network and modifies it.
 */
public abstract class AlterationAction {
    public abstract void apply(BayesianNetwork bnOrig) throws BayesianNetworkException;
}
