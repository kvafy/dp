// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/13

package bna.bnlib;


/**
 * Thrown when an impossible operation is attempted.
 * Examples: removing non-existent edge of a graph, marginalizing over wrong variables etc.
 */
public class BNLibIllegalOperationException extends BNLibException {
    public BNLibIllegalOperationException() {
    }
    
    public BNLibIllegalOperationException(String msg) {
        super(msg);
    }
}
