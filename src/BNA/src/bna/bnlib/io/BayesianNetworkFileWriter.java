// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/21

package bna.bnlib.io;

import bna.bnlib.BNLibIOException;
import bna.bnlib.BayesianNetwork;


/**
 * Template method object for writing a Bayesian network specification to a file.
 */
public abstract class BayesianNetworkFileWriter {
    protected String filename;
    
    public BayesianNetworkFileWriter(String filename) {
        this.filename = filename;
    }
    
    public abstract void save(BayesianNetwork bn) throws BNLibIOException;
}
