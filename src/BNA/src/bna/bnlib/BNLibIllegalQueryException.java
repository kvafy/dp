// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/24

package bna.bnlib;


/**
 * Thrown when query specified as a string is invalid.
 */
public class BNLibIllegalQueryException extends BNLibException {
    public BNLibIllegalQueryException() {
        super();
    }
    
    public BNLibIllegalQueryException(String msg) {
        super(msg);
    }
}
