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
    private int[] indexMapping;   // subset -> index in superset
    private int[][] valueMapping; // supersetIntValue -> subsetIntValue
    
    public VariableSubsetMapper(Variable[] superset, Variable[] subset)  {
        if(!Toolkit.isSubset(superset, subset))
            throw new BNLibIllegalArgumentException("Second array isn't a subset of the first one.");
        this.superset = superset;
        // generate the positional mapping
        this.indexMapping = new int[subset.length];
        for(int i = 0 ; i < subset.length ; i++)
            this.indexMapping[i] = Toolkit.indexOf(superset, subset[i]);
        // generate value mapping
        this.valueMapping = new int[subset.length][];
        for(int i = 0 ; i < subset.length ; i++) {
            Variable iSubVar = subset[i],
                     iSupVar = superset[this.indexMapping[i]];
            if(!Toolkit.areEqual(iSubVar.getValues(), iSupVar.getValues())) {
                String msg = String.format("Variable \"%s\" has different set of values in superset and in subset.", iSubVar.getName());
                throw new BNLibIllegalArgumentException(msg);
            }
            String[] iSupValues = iSupVar.getValues();
            this.valueMapping[i] = new int[iSubVar.getCardinality()];
            for(int j = 0 ; j < iSubVar.getCardinality() ; j++)
                this.valueMapping[i][j] = iSubVar.getValueIndex(iSupValues[j]);
        }
    }
    
    public int[] map(int[] supersetAssignment) {
        int[] subsetAssignment = new int[indexMapping.length];
        return this.map(supersetAssignment, subsetAssignment);
    }
    
    public int[] map(int[] supersetAssignment, int[] subsetAssignment) throws BNLibInvalidInstantiationException, BNLibIllegalArgumentException {
        if(subsetAssignment.length != this.indexMapping.length)
            throw new BNLibIllegalArgumentException("Invalid array for target assignment.");
        if(!Toolkit.validateAssignment(this.superset, supersetAssignment))
            throw new BNLibInvalidInstantiationException("Invalid assignment of the superset variables.");
        
        for(int i = 0 ; i < this.indexMapping.length ; i++) {
            // map positions
            int supersetValue = supersetAssignment[this.indexMapping[i]];
            // translate assignment value index
            subsetAssignment[i] = this.valueMapping[i][supersetValue];
        }
        return subsetAssignment;
    }
}
