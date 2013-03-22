// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/20

package bna.view;


/**
 * Thrown when an internal check fails (which should never be the case).
 */
public class InternalCheckException extends RuntimeException {
    public InternalCheckException() {
        super();
    }
    
    public InternalCheckException(String msg) {
        super(msg);
    }
}
