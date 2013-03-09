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
    
    
    /**
     * Create counter addresable by assignments of given set of variables, each entry with given initial value.
     * @param variables
     * @param init Initial value in each entry of the counter.
     */
    public Counter(Variable[] variables, double init) {
        this.variables = Arrays.copyOf(variables, variables.length);
        this.indexMapper = new AssignmentIndexMapper(this.variables);
        this.values = new double[Toolkit.cardinality(this.variables)];
        if(init != 0)
            Arrays.fill(this.values, init);
    }
    
    /**
     * Create counter addresable by assignments of given set of variables, each entry equal to 0.
     * @param variables
     */
    public Counter(Variable[] variables) {
        this(variables, 0.0);
    }
    
    public void add(int[] assignment, double delta) {
        if(!Toolkit.validateAssignment(this.variables, assignment))
            throw new BayesianNetworkRuntimeException("Invalid variables assignment for mapper.");
        int index = this.indexMapper.assignmentToIndex(assignment);
        this.values[index] += delta;
    }
    
    public Factor toFactor() {
        return new Factor(this.variables, this.values);
    }
}
