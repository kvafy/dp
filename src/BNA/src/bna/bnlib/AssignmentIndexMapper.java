// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/08

package bna.bnlib;

import bna.bnlib.misc.Toolkit;

/**
 * Map between assignment of variables and corresponding index in a linear vector.
 */
public class AssignmentIndexMapper {
    private Variable[] vars;
    private int assignmentsCount;
    private int[] accessVector;
    
    public AssignmentIndexMapper(Variable[] variables) {
        this.vars = variables;
        this.assignmentsCount = Toolkit.cardinality(this.vars);
        // generate accessVector[i] = \prod_{j=0..i-1} scope[j].cardinality
        this.accessVector = new int[variables.length];
        accessVector[0] = 1;
        for(int i = 1 ; i < variables.length ; i++)
            accessVector[i] = accessVector[i - 1] * variables[i - 1].getCardinality();
    }
    
    public int assignmentToIndex(int[] assignment) throws BNLibInvalidInstantiationException, BNLibIllegalArgumentException {
        // validate parameters
        if(assignment == null)
            throw new BNLibIllegalArgumentException("Null assignment.");
        if(!Toolkit.validateAssignment(this.vars, assignment))
            throw new BNLibInvalidInstantiationException("Invalid assignment for given list of variables.");
        // index computation
        int index = 0;
        for(int i = 0 ; i < assignment.length ; i++)
            index += this.accessVector[i] * assignment[i];
        return index;
    }
    
    public int[] indexToAssignment(int index) throws BNLibInvalidInstantiationException {
        if(index < 0 || index >= this.assignmentsCount)
            throw new BNLibInvalidInstantiationException(String.format("Assignment index %i out of valid range [0, %i).", index, this.assignmentsCount));
        int[] assignment = new int[this.vars.length];
        for(int i = 0 ; i < assignment.length ; i++) {
            assignment[i] = index % this.vars[i].getCardinality();
            index /= this.vars[i].getCardinality();
        }
        return assignment;
    }
}
