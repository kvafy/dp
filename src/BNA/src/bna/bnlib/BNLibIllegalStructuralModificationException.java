// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/21

package bna.bnlib;


/**
 * Thrown on an attempt of an invalid structural change of a network.
 * For example, removing a non-existent edge or adding an edge that introduces
 * a cycle.
 */
public class BNLibIllegalStructuralModificationException extends BNLibIllegalOperationException {
    public BNLibIllegalStructuralModificationException() {
        super();
    }
    
    public BNLibIllegalStructuralModificationException(String msg) {
        super(msg);
    }
}
