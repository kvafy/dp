// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/25

package bna.bnlib;


/**
 * Thrown when we are given an assignment of variables that is invalid.
 * That could mean variable value index out of range, instantiation of wrong
 * number of variables etc.
 */
public class BNLibInvalidInstantiationException extends BNLibException {
    public BNLibInvalidInstantiationException() {
        super();
    }
    
    public BNLibInvalidInstantiationException(String msg) {
        super(msg);
    }
}
