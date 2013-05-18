// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

import bna.bnlib.misc.TextualTable;
import bna.bnlib.misc.Toolkit;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Immutable representation of a factor.
 * The factor can be iterated over all possible assignments of its scope (not over values!).
 */
public class Factor implements Iterable<int[]> {
    private Variable[] scope;
    private double[] values;
    private AssignmentIndexMapper mapper; // mapping: index into this.values <-> int[] assignment
    
    
    /**
     * Create a new factor with given scope and values.
     * The values are interpreted in the following way: Initially all variables
     * are assigned their first possible value which corresponds to value
     * values[0] of this factor. Then the leftmost variable increments its assignment,
     * possibly overflowing wich results in the next variable incrementing its
     * value etc.; the new assignment corresponds with values[1] etc.
     * @param scope
     * @param values
     * @throws BNLibIllegalArgumentException When scope or values argument is
     *         invalid.
     */
    public Factor(Variable[] scope, double[] values) throws BNLibIllegalArgumentException {
        if(scope == null || scope.length == 0)
            throw new BNLibIllegalArgumentException("Scope cannot be null nor empty");
        this.scope = Arrays.copyOf(scope, scope.length);
        this.values = Arrays.copyOf(values, values.length);
        this.mapper = new AssignmentIndexMapper(scope);
        
        if(!this.hasValidCardinality())
            throw new BNLibIllegalArgumentException("Invalid values length wrt scope.");
    }
    
    /** Create factor with given scope and with each entry set to the given value. */
    public Factor(Variable[] scope, double valueOfEachEntry) {
        this.scope = Arrays.copyOf(scope, scope.length);
        this.values = new double[Toolkit.cardinality(scope)];
        if(valueOfEachEntry != 0)
            Arrays.fill(this.values, valueOfEachEntry);
        this.mapper = new AssignmentIndexMapper(scope);
    }
    
    /** Get value of the factor on given index (ie. this.values[index]). */
    public double getProbability(int index) {
        return this.values[index];
    }
    
    /**
     * Get value of the factor corresponding to given assignment.
     * @throws BNLibInvalidInstantiationException When the given argument is
     *         not an assignment of the variables in scope of this factor.
     */
    public double getProbability(int[] assignment) throws BNLibInvalidInstantiationException {
        if(!Toolkit.validateAssignment(this.scope, assignment))
            throw new BNLibInvalidInstantiationException("Invalid assignment wrt. scope of the factor.");
        return this.values[this.mapper.assignmentToIndex(assignment)];
    }
    
    /**
     * Get the number of values in this factor.
     * Cardinality can be also viewed as a product of cardinalities of all variables
     * in the scope of this factor.
     */
    public int getCardinality() {
        return values.length;
    }
    
    /** Get the scope (variables) of this factor. */
    public Variable[] getScope() {
        return Arrays.copyOf(this.scope, this.scope.length);
    }
    
    /** Perform factor marginalization over given set of variables. */
    public Factor marginalize(Variable[] over) throws BNLibIllegalOperationException {
        Variable[] newScope = Toolkit.difference(this.scope, over);
        if(newScope.length == 0)
            throw new BNLibIllegalOperationException("Marginalizing over all variables yields an empty factor.");
        double[] newValues = new double[Toolkit.cardinality(newScope)];
        VariableSubsetMapper scopeToNewScopeMapper = new VariableSubsetMapper(this.scope, newScope);
        AssignmentIndexMapper newScopeIndexMapper = new AssignmentIndexMapper(newScope);
        int[] newScopeAssignment = new int[newScope.length];
        for(int[] scopeAssignment : this) {
            double value = this.getProbability(scopeAssignment);
            scopeToNewScopeMapper.map(scopeAssignment, newScopeAssignment);
            int index = newScopeIndexMapper.assignmentToIndex(newScopeAssignment);
            newValues[index] += value;
        }
        return new Factor(newScope, newValues);
    }
    
    /** Make all values of the factor sum to one. */
    public Factor normalize() {
        return this.normalizeByFirstNVariables(this.scope.length);
    }
    
    /**
     * Values for assignments that differ only in (n+1)-th variable and higher will sum to one.
     * @throws BNLibIllegalOperationException When the value of n is invalid wrt.
     *         the scope of this factor.
     */
    public Factor normalizeByFirstNVariables(int n) throws BNLibIllegalOperationException {
        if(n < 1 || n > this.scope.length)
            throw new BNLibIllegalOperationException("Normalization by invalid number of variables.");
        double[] normalizedValues = new double[this.values.length];
        // determine how many consequent values have to sum to 1.0
        int valuesInABlock = 1;
        for(int i = 0 ; i < n ; i++)
            valuesInABlock *= this.scope[i].getCardinality();
        // each valuesInABlock values must sum to one, make it so
        for(int block = 0 ; block < this.getCardinality() / valuesInABlock ; block++) {
            int blockOffset = block * valuesInABlock;
            // determine sum
            double blockSum = 0;
            for(int i = 0 ; i < valuesInABlock ; i++)
                blockSum += this.values[blockOffset + i];
            // divide each value in the block by the block sum
            if(blockSum == 0)
                blockSum = 1.0;
            for(int i = 0 ; i < valuesInABlock ; i++)
                normalizedValues[blockOffset + i] = this.values[blockOffset + i] / blockSum;
        }
        
        return new Factor(this.scope, normalizedValues);
    }
    
    /** Check whether the factor has valid cardinality (the this.values vector) wrt its scope. */
    public final boolean hasValidCardinality() {
        int cardinalityByScope = Toolkit.cardinality(this.scope);
        return this.values.length == cardinalityByScope;
    }
    
    /**
     * Sums set of factor which all have exactly the same scope.
     * @throws BNLibIllegalArgumentException When the factors cannot be summed
     *         (the array is empty or scopes of the factors aren't equal).
     */
    public static Factor sumFactors(Factor[] factors) throws BNLibIllegalArgumentException {
        if(factors == null || factors.length == 0)
            throw new BNLibIllegalArgumentException("The factors array must be non-empty.");
        for(int i = 1 ; i < factors.length ; i++)
            if(!Toolkit.areEqual(factors[0].getScope(), factors[i].getScope()))
                throw new BNLibIllegalArgumentException("All factors need to have the same variables in their scope.");
        
        Variable[] sumScope = factors[0].getScope();
        double[] sumValues = new double[factors[0].getCardinality()];
        AssignmentIndexMapper sumIndexMapper = new AssignmentIndexMapper(sumScope);
        VariableSubsetMapper mappers[] = new VariableSubsetMapper[factors.length]; // throws BNLibIllegalArgumentException
        for(int i = 0 ; i < mappers.length ; i++)
            mappers[i] = new VariableSubsetMapper(sumScope, factors[i].getScope());
        for(int[] assignment : factors[0]) {
            double partialSum = 0;
            for(int i = 0 ; i < factors.length ; i++) {
                int[] iAssignment = mappers[i].map(assignment);
                partialSum += factors[i].getProbability(iAssignment);
            }
            sumValues[sumIndexMapper.assignmentToIndex(assignment)] = partialSum;
        }
        return new Factor(sumScope, sumValues);
    }
    
    /** Iterate over all possible assignments to variables in the scope of this factor. */
    @Override
    public Iterator<int[]> iterator() {
        return new AssignmentIterator(this.scope);
    }
    
    @Override
    public String toString() {
        final int PRECISION = 6;
        // first generate header
        String[] header = new String[this.scope.length + 1];
        for(int i = 0 ; i < this.scope.length ; i++)
            header[i] = this.scope[i].getName();
        header[this.scope.length] = "probability";
        TextualTable table = new TextualTable(header, PRECISION, true);
        // generate the data part, row by row
        for(int[] assignment : this) {
            LinkedList<Object> row = new LinkedList<Object>();
            for(int i = 0 ; i < this.scope.length ; i++) {
                Variable iVar = this.scope[i];
                int iVarValue = assignment[i];
                row.addLast(iVar.getValues()[iVarValue]);
            }
            row.addLast(this.getProbability(assignment));
            table.addRow(row.toArray());
        }
        return table.toString();
    }
}
