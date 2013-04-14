// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/15

package bna.bnlib;


/**
 * A generic exception for unexpected argument value.
 */
public class BNLibIllegalArgumentException extends BNLibException {
    public BNLibIllegalArgumentException() {
    }
    
    public BNLibIllegalArgumentException(String msg) {
        super(msg);
    }
}
