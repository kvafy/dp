// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/21

package bna.bnlib;

/**
 * When we attempt a structural change of a network (ie. edges) which is invalid.
 */
public class BNLibIllegalStructuralModificationException extends BNLibIllegalOperationException {
    public BNLibIllegalStructuralModificationException() {
        super();
    }
    
    public BNLibIllegalStructuralModificationException(String msg) {
        super(msg);
    }
}
