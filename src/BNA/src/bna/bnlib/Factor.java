// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

import java.util.Arrays;

/**
 * Immutable.
 */
public class Factor {
    private Variable[] scope;
    private double[] values;
    private AssignmentIndexMapper mapper;
    
    
    public Factor(Variable[] scope, double[] values) {
        this.scope = Arrays.copyOf(scope, scope.length);
        this.values = Arrays.copyOf(values, values.length);
        this.mapper = new AssignmentIndexMapper(scope);
        
        if(!this.hasValidCardinality())
            throw new BayesianNetworkRuntimeException("Invalid values length wrt scope");
    }
    
    public double getProbability(int index) {
        return this.values[index];
    }
    
    public double getProbability(int[] assignment) {
        return this.values[this.mapper.assignmentToIndex(assignment)];
    }
    
    public int getCardinality() {
        return values.length;
    }
    
    public Variable[] getScope() {
        // TODO performance
        return Arrays.copyOf(this.scope, this.scope.length);
    }
    
    public final boolean hasValidCardinality() {
        int cardinalityByScope = Toolkit.cardinality(this.scope);
        return this.values.length == cardinalityByScope;
    }
}
