// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

/**
 * Base exception class for all the operations with Bayesian networks.
 */
public class BayesianNetworkException extends Exception {
    public BayesianNetworkException() {
        super();
    }
    
    public BayesianNetworkException(String msg) {
        super(msg);
    }
}