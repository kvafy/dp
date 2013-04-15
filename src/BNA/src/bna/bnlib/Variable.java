// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

import bna.bnlib.misc.Toolkit;
import java.util.Arrays;
import java.util.Collection;


/**
 * A discrete variable consisting of its name and array of possible assignments.
 * Immutable.
 */
public class Variable {
    // what characters cannot be contained within a variable name
    private static final String ILLEGAL_NAME_CHARACTERS = ",|=() \r\n\t";
    // what characters cannot be contained within a variable value
    private static final String ILLEGAL_VALUE_CHARACTERS = ",|=() \r\n\t";
    
    private String name;
    private String[] values;    // list of possible values
    
    
    /**
     * Create a new variable.
     * @param name
     * @param values
     * @throws BNLibIllegalValueSpecificicationException When given name or
     *         values array isn't valid for a variable.
     */
    public Variable(String name, String[] values) throws BNLibIllegalVariableSpecificicationException {
        this.name = name;
        this.values = Arrays.copyOf(values, values.length);
        this.validateName(this.name);
        this.validateValues(this.values);
    }
    
    /**
     * Create a new variable.
     * @param name
     * @param values
     * @throws BNLibIllegalValueSpecificicationException When given name or
     *         values array isn't valid for a variable.
     */
    public Variable(String name, Collection<String> values) throws BNLibIllegalVariableSpecificicationException {
        this.name = name;
        this.values = new String[values.size()];
        values.toArray(this.values);
        this.validateName(this.name);
        this.validateValues(this.values);
    }
    
    private void validateName(String name) throws BNLibIllegalVariableSpecificicationException {
        if(name == null || name.isEmpty())
            throw new BNLibIllegalVariableSpecificicationException("Variable name cannot be null nor empty.");
        for(int i = 0 ; i < Variable.ILLEGAL_NAME_CHARACTERS.length() ; i++) {
            String ch = Variable.ILLEGAL_NAME_CHARACTERS.substring(i, i + 1);
            if(name.contains(ch)) {
                String msg = String.format("Variable name cannot contain the character '%s'.", ch);
                throw new BNLibIllegalVariableSpecificicationException(msg);
            }
        }
    }
    
    private void validateValues(String[] values) throws BNLibIllegalVariableSpecificicationException {
        if(!Toolkit.unique(values))
            throw new BNLibIllegalVariableSpecificicationException("Variable must have unique values.");
        for(String value : values) {
            if(value == null || value.isEmpty())
                throw new BNLibIllegalVariableSpecificicationException("Variable value cannot be null nor empty.");
            for(int i = 0 ; i < Variable.ILLEGAL_VALUE_CHARACTERS.length() ; i++) {
                String ch = Variable.ILLEGAL_VALUE_CHARACTERS.substring(i, i + 1);
                if(value.contains(ch)) {
                    String msg = String.format("Variable value cannot contain the character '%s'.", ch);
                    throw new BNLibIllegalVariableSpecificicationException(msg);
                }
            }
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public String[] getValues() {
        return Arrays.copyOf(this.values, this.values.length);
    }
    
    /** Convert textual assignment to index in this.values array. */
    public int getValueIndex(String value) throws BNLibNonexistentVariableValueException {
        for(int i = 0 ; i < this.values.length ; i++) {
            if(this.values[i].equals(value))
                return i;
        }
        String msg = String.format("\"%s\" is not an assignment of variable \"%s\".", value, this.name);
        throw new BNLibNonexistentVariableValueException(msg);
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
        hash = 97 * hash + this.name.hashCode();
        return hash;
    }
}
