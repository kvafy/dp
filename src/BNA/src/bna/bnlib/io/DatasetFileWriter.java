// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/10

package bna.bnlib.io;

import bna.bnlib.BNLibIOException;
import bna.bnlib.learning.Dataset;


/**
 * Template method object for writing a dataset to file.
 */
public abstract class DatasetFileWriter {
    protected String filename;
    
    public DatasetFileWriter(String filename) {
        this.filename = filename;
    }
    
    public abstract void save(Dataset dataset) throws BNLibIOException;
}
