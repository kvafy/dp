// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Immutable.
 */
public class Factor implements Iterable<int[]> {
    private Variable[] scope;
    private double[] values;
    private AssignmentIndexMapper mapper;
    
    private static final String TO_STRING_COLUMN_SEPARATOR = " | ";
    
    
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
    
    public Factor normalizeByFirstNVariables(int n) {
        if(n < 1 || n > this.scope.length)
            throw new BayesianNetworkRuntimeException("Normalization by invalid number of variables.");
        double[] normalizedValues = new double[this.values.length];
        // determine how many consequent values have to sum to 1.0
        int valuesInABlock = 0;
        for(int i = 0 ; i < n ; i++)
            valuesInABlock += this.scope[i].getCardinality();
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
    
    public final boolean hasValidCardinality() {
        int cardinalityByScope = Toolkit.cardinality(this.scope);
        return this.values.length == cardinalityByScope;
    }
    
    /** Iterate over all possible assignemnts to variables in scope of this factor. */
    @Override
    public Iterator<int[]> iterator() {
        return new AssignmentIterator(this.scope);
    }
    
    @Override
    public String toString() {
        final int MIN_WIDTH = 3,
                  PROBABILITY_WIDTH = 5;
        StringBuilder res = new StringBuilder();
        int[] columnWidths = this.toString_determineOutputColumnWidths(MIN_WIDTH, PROBABILITY_WIDTH);
        String[] columnFormatStrings = this.toString_determineOutputColumnFormatString(columnWidths);
        
        this.toString_appendHeader(res, columnFormatStrings);
        res.append(System.lineSeparator());
        this.toString_appendHeaderSeparator(res, columnWidths);
        for(int[] assignment : this) {
            res.append(System.lineSeparator());
            this.toString_appendAssignment(res, columnFormatStrings, assignment);
        }
        return res.toString();
    }
    
    private void toString_appendHeader(StringBuilder sb, String[] columnFormatStrings) {
        for(int i = 0 ; i < this.scope.length ; i++) {
            if(i != 0)
                sb.append(Factor.TO_STRING_COLUMN_SEPARATOR);
            String varName = this.scope[i].getName();
            sb.append(String.format(columnFormatStrings[i], varName));
        }
        sb.append(Factor.TO_STRING_COLUMN_SEPARATOR);
        sb.append(String.format(columnFormatStrings[this.scope.length], "prob"));
    }
    
    private void toString_appendHeaderSeparator(StringBuilder sb, int[] columnWidths) {
        for(int i = 0 ; i < this.scope.length ; i++) {
            if(i != 0)
                this.toString_appendNTimes(sb, "=", TO_STRING_COLUMN_SEPARATOR.length());
            this.toString_appendNTimes(sb, "=", columnWidths[i] + 2); // column one-space padding on both sides
        }
        this.toString_appendNTimes(sb, "=", TO_STRING_COLUMN_SEPARATOR.length());
        this.toString_appendNTimes(sb, "=", columnWidths[this.scope.length] + 2);
    }
    
    private void toString_appendNTimes(StringBuilder sb, String s, int count) {
        for(int i = 0 ; i < count ; i++)
            sb.append(s);
    }
    
    private void toString_appendAssignment(StringBuilder sb, String[] columnFormatStrings, int[] assignment) {
        for(int i = 0 ; i < this.scope.length ; i++) {
            if(i != 0)
                sb.append(Factor.TO_STRING_COLUMN_SEPARATOR);
            Variable varI = this.scope[i];
            int varIValue = assignment[i];
            String varIStrValue = varI.getValues()[varIValue];
            sb.append(String.format(columnFormatStrings[i], varIStrValue));
        }
        sb.append(Factor.TO_STRING_COLUMN_SEPARATOR);
        String probStr = String.valueOf(this.getProbability(assignment));
        sb.append(String.format(columnFormatStrings[this.scope.length], probStr));
    }
    
    private int[] toString_determineOutputColumnWidths(int minwidth, int probwidth) {
        int[] charWidths = new int[this.scope.length + 1];
        for(int i = 0 ; i < this.scope.length ; i++) {
            Variable iVar = this.scope[i];
            charWidths[i] = minwidth;
            charWidths[i] = Math.max(charWidths[i], iVar.getName().length());
            for(String iVal : iVar.getValues())
                charWidths[i] = Math.max(charWidths[i], iVal.length());
        }
        charWidths[this.scope.length] = probwidth; // probability column
        return charWidths;
    }
    
    private String[] toString_determineOutputColumnFormatString(int[] columnWidths) {
        String[] formatStrings = new String[columnWidths.length];
        for(int i = 0 ; i < columnWidths.length ; i++)
            formatStrings[i] = String.format(" %%%ds ", columnWidths[i]);
        return formatStrings;
    }
}
