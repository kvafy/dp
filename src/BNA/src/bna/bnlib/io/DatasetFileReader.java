// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/30

package bna.bnlib.io;

import bna.bnlib.BNLibIOException;
import bna.bnlib.Variable;
import bna.bnlib.learning.Dataset;
import java.util.List;


/**
 * Template method object for reading a dataset from file.
 */
public abstract class DatasetFileReader {
    protected String filename;
    
    
    public DatasetFileReader(String filename) {
        this.filename = filename;
    }
    
    /** Template method to load a BN from network. */
    public final Dataset load() throws BNLibIOException {
        Dataset dataset = new Dataset(this.readVariables());
        for(int[] row : this.readDataRows())
            dataset.addRecord(row);
        return dataset;
    }
    
    protected abstract Variable[] readVariables();
    protected abstract List<int[]> readDataRows();
}
