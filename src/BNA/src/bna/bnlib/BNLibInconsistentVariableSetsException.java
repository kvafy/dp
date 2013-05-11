// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/04

package bna.bnlib;


/**
 * Thrown when two sets of variables are inconsistent ("consistency" depends on context).
 * For example an attempt to learn parameters of a network with dataset
 * containing different variables will result in this exception being thrown.
 */
public class BNLibInconsistentVariableSetsException extends BNLibException {
    public BNLibInconsistentVariableSetsException() {
        super();
    }
    
    public BNLibInconsistentVariableSetsException(String msg) {
        super(msg);
    }
}
