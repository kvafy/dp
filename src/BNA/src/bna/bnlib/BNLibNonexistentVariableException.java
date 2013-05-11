// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/24

package bna.bnlib;


/**
 * Thrown when we use a variable that doesn't exist although it is needed.
 */
public class BNLibNonexistentVariableException extends BNLibException {
    public BNLibNonexistentVariableException() {
        super();
    }
    
    public BNLibNonexistentVariableException(String msg) {
        super(msg);
    }
}
