// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/09

package bna.bnlib;

import bna.bnlib.misc.Toolkit;


/**
 * Map assignment of a superset of variables to assignment of their subset.
 */
public class VariableSubsetMapper {
    private Variable[] superset;
    private int[] mapping; // subset -> index in superset
    
    public VariableSubsetMapper(Variable[] superset, Variable[] subset)  {
        if(!Toolkit.isSubset(superset, subset))
            throw new BayesianNetworkRuntimeException("Not a subset");
        this.superset = superset;
        // generate the mapping
        this.mapping = new int[subset.length];
        for(int i = 0 ; i < subset.length ; i++)
            this.mapping[i] = Toolkit.indexOf(superset, subset[i]);
    }
    
    public int[] map(int[] supersetAssignment) {
        int[] subsetAssignment = new int[mapping.length];
        return this.map(supersetAssignment, subsetAssignment);
    }
    
    public int[] map(int[] supersetAssignment, int[] subsetAssignment) {
        if(subsetAssignment.length != this.mapping.length)
            throw new BayesianNetworkRuntimeException("Invalid array for target assignment.");
        if(!Toolkit.validateAssignment(this.superset, supersetAssignment))
            throw new BayesianNetworkRuntimeException("Invalid assignment of the superset.");
        for(int i = 0 ; i < this.mapping.length ; i++)
            subsetAssignment[i] = supersetAssignment[this.mapping[i]];
        return subsetAssignment;
    }
}
