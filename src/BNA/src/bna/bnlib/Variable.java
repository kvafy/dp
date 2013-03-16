// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

import bna.bnlib.misc.Toolkit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;


/**
 * A discrete variable consisting of its name and array of possible assignments.
 * Immutable.
 */
public class Variable {
    private String name;
    private String[] values;    // list of possible values
    
    public Variable(String name, String[] values) throws BayesianNetworkException {
        if(!Toolkit.unique(values))
            throw new BayesianNetworkException("Variable must have unique values.");
        this.name = name;
        this.values = Arrays.copyOf(values, values.length);
    }
    
    public Variable(String name, Collection<String> values) {
        this.name = name;
        this.values = new String[values.size()];
        values.toArray(this.values);
    }
    
    public String getName() {
        return this.name;
    }
    
    public String[] getValues() {
        return Arrays.copyOf(this.values, this.values.length);
    }
    
    /** Convert textual assignment to index in this.values array. */
    public int getValueIndex(String value) {
        for(int i = 0 ; i < this.values.length ; i++) {
            if(this.values[i].equals(value))
                return i;
        }
        String msg = String.format("Unknown assignment \"%s\" of variable \"%s\".", value, this.name);
        throw new BayesianNetworkRuntimeException(msg);
    }
    
    public int getCardinality() {
        return this.values.length;
    }
    
    @Override
    public boolean equals(Object o) {
        if(o instanceof Variable)
            return ((Variable)o).getName().equals(this.name);
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.name);
        return hash;
    }
}
