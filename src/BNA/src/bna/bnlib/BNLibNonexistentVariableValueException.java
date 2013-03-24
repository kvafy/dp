// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/24

package bna.bnlib;

/**
 * Thrown when we ask for a variable value that doesn't exist.
 */
public class BNLibNonexistentVariableValueException extends BNLibException {
    public BNLibNonexistentVariableValueException() {
        super();
    }
    
    public BNLibNonexistentVariableValueException(String msg) {
        super(msg);
    }
}
