// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/05

package bna.bnlib;

import java.util.Arrays;


/**
 * Class serving as a counter addressable by assignment of variables.
 */
public class Counter {
    private Variable[] variables;
    private AssignmentIndexMapper indexMapper;
    private double[] values;
    
    
    public Counter(Variable[] variables) {
        this.variables = Arrays.copyOf(variables, variables.length);
        this.indexMapper = new AssignmentIndexMapper(this.variables);
        this.values = new double[Toolkit.cardinality(this.variables)];
    }
    
    public void add(int[] assignment, double delta) {
        int index = this.indexMapper.assignmentToIndex(assignment);
        this.values[index] += delta;
    }
    
    public Factor toFactor() {
        return new Factor(this.variables, this.values);
    }
}
