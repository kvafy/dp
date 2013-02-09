/*
 * // Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
 * // Author:  David Chaloupka (xchalo09)
 * // Created: 2013/02/09
 */

package bna.bnlib;

/**
 * Map assignment of a superset of variables to assignment of their subset.
 */
class VariableSubsetMapper {
    private int[] mapping; // subset -> index in superset
    
    public VariableSubsetMapper(Variable[] superset, Variable[] subset)  {
        if(!Toolkit.isSubset(superset, subset))
            throw new BayesianNetworkRuntimeException("Not a subset");
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
        for(int i = 0 ; i < this.mapping.length ; i++)
            subsetAssignment[i] = supersetAssignment[this.mapping[i]];
        return subsetAssignment;
    }
}
