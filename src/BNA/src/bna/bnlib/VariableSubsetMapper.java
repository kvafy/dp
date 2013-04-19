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
    
    
    /**
     * Creates new mapper to map an assignment of superset variables to an assignment
     * of subset variables.
     * @throws BNLibIllegalArgumentException When variables of the first array
     *         aren't a superset of the variables of the second argument or
     *         when the variables don't have the exact same set of possible assingments.
     */
    public VariableSubsetMapper(Variable[] superset, Variable[] subset) throws BNLibIllegalArgumentException {
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
    
    /**
     * Map an assignment of the superset variables of this mapper to an assignment
     * to the subset variables of this mapper.
     * @throws BNLibInvalidInstantiationException When the first argument isn't
     *         an instantiation of the superset variables of this mapper.
     */
    public int[] map(int[] supersetAssignment) {
        int[] subsetAssignment = new int[indexMapping.length];
        return this.map(supersetAssignment, subsetAssignment);
    }
    
    /**
     * Map an assignment of the superset variables of this mapper to an assignment
     * to the subset variables of this mapper.
     * For the subset assignment is used the passed array.
     * @param supersetAssignment
     * @param subsetAssignment
     * @return
     * @throws BNLibInvalidInstantiationException When the first argument isn't
     *         an instantiation of the superset variables of this mapper.
     * @throws BNLibIllegalArgumentException When the second argument is not
     *         exactly of the size needed for holding an assignment of the
     *         subset variables of this mapper.
     */
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
