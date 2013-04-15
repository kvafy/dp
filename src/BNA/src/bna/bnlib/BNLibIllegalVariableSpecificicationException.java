// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/15

package bna.bnlib;


/**
 * Thrown at an attempt to create a variable instance with invalid name or values.
 */
public class BNLibIllegalVariableSpecificicationException extends BNLibException {
    public BNLibIllegalVariableSpecificicationException() {
    }
    
    public BNLibIllegalVariableSpecificicationException(String msg) {
        super(msg);
    }
}
