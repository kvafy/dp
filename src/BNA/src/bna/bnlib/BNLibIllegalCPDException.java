// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/21

package bna.bnlib;


/**
 * When we attempt to set invalid CPD for a node.
 */
public class BNLibIllegalCPDException extends BNLibIllegalOperationException {
    public BNLibIllegalCPDException() {
    }
    
    public BNLibIllegalCPDException(String msg) {
        super(msg);
    }
}
