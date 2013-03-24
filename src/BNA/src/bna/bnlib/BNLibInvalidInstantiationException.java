// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/25

package bna.bnlib;


/**
 * When we are given an assignment of variables that is invalid.
 * That means variable value index out of range or instantiation of wrong
 * number of variables.
 */
public class BNLibInvalidInstantiationException extends BNLibException {
    public BNLibInvalidInstantiationException() {
        super();
    }
    
    public BNLibInvalidInstantiationException(String msg) {
        super(msg);
    }
}
