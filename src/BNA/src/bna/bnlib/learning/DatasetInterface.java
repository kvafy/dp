// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/14

package bna.bnlib.learning;

import bna.bnlib.*;


/**
 * Generic interface of a dataset holding samples (multiset of instantiations) of a set of variables.
 */
public interface DatasetInterface {
    /** Get the set of variables whose instantiations this dataset records. */
    public Variable[] getVariables();
    
    /** Get the number of records in this dataset. */
    public int getSize();
    
    /** Add new record to the dataset. */
    public void addRecord(int[] record);
    
    /** Count occurences of all assignments to given variables and return as a factor. */
    public Factor computeFactor(Variable[] scope);
    
    /** Compute mutual information between two sets of variables. */
    public double mutualInformation(Variable[] set1, Variable[] set2);
}
