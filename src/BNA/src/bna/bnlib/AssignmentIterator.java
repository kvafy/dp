// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/10

package bna.bnlib;


/**
 * Iterate over all possible assignments of given variables.
 * The leftmost variable changes value the most frequently.
 */
class AssignmentIterator implements java.util.Iterator<int[]> {
    private Variable[] vars;
    private int[] assignment;
    private boolean first;
    
    public AssignmentIterator(Variable[] vars) {
        this.vars = vars;
        this.assignment = new int[this.vars.length];
        this.first = true;
    }

    @Override
    public boolean hasNext() {
        return !this.allValuesWouldOverflow();
    }

    @Override
    public int[] next() {
        if(first) {
            first = false;
            return this.assignment;
        }
        this.increment();
        return this.assignment;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Operation remove() is not supported.");
    }
    
    private void increment() {
        for(int i = 0 ; i < this.vars.length ; i++) {
            if(this.assignment[i] + 1 < vars[i].getCardinality()) {
                this.assignment[i]++;
                return;
            }
            else
                this.assignment[i] = 0; // overflow
        }
    }
    
    private boolean allValuesWouldOverflow() {
        for(int i = 0 ; i < this.vars.length ; i++)
            if(this.assignment[i] + 1 < vars[i].getCardinality())
                return false;
        return true;
    }
}
