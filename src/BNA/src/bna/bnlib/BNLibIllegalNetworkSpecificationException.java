// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/21

package bna.bnlib;


/**
 * Thrown on an attempt to create a network, or its component, from bad data (eg. non-unique variables).
 */
public class BNLibIllegalNetworkSpecificationException extends BNLibException {
    public BNLibIllegalNetworkSpecificationException() {
        super();
    }
    
    public BNLibIllegalNetworkSpecificationException(String msg) {
        super(msg);
    }
}
