// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/14

package bna.bnlib.learning;

import bna.bnlib.*;


/**
 *
 * @author David Chaloupka
 */
public interface DatasetInterface {
    public Variable[] getVariables();
    
    public int getSize();
    
    /** Add new record to the dataset. */
    public void addRecord(int[] record);
    
    /** Count occurences of all assignments to given variables and return as a factor. */
    public Factor computeFactor(Variable[] scope);
    
    /** Compute mutual information between two sets of variables. */
    public double mutualInformation(Variable[] set1, Variable[] set2);
}
