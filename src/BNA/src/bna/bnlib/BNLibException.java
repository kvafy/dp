// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/11

package bna.bnlib;


/**
 * The base exception of all other exceptions in the bnlib package and subpackages.
 * All the exceptions are unchecked.
 */
public class BNLibException extends RuntimeException {
    public BNLibException() {
        super();
    }
    
    public BNLibException(String msg) {
        super(msg);
    }
}
