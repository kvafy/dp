// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/08

package bna.bnlib;


/**
 * Base exception class for errors mostly caused by incorrect work with the library.
 */
public class BayesianNetworkRuntimeException extends RuntimeException {
    public BayesianNetworkRuntimeException() {
        super();
    }
    
    public BayesianNetworkRuntimeException(String msg) {
        super(msg);
    }
}
