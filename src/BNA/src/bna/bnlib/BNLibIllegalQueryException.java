// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/24

package bna.bnlib;


/**
 * Thrown when a query specification is invalid.
 * Example could be a wrong query string (missing parentehis, non-existent
 * variable, non-existent variable value etc.).
 */
public class BNLibIllegalQueryException extends BNLibException {
    public BNLibIllegalQueryException() {
        super();
    }
    
    public BNLibIllegalQueryException(String msg) {
        super(msg);
    }
}
