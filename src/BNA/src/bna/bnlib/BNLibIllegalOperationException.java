// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/13

package bna.bnlib;


/**
 * Whenever an impossible operation is attempted (eg. removing non-existent edge to a graph).
 */
public class BNLibIllegalOperationException extends BNLibException {
    public BNLibIllegalOperationException() {
    }
    
    public BNLibIllegalOperationException(String msg) {
        super(msg);
    }
}
