// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/11

package bna.bnlib;


/**
 * Exception that thrown only due to some internal error of the library itself.
 */
public class BNLibInternalException extends BNLibException {
    public BNLibInternalException() {
    }
    
    public BNLibInternalException(String msg) {
        super(msg);
    }
}
