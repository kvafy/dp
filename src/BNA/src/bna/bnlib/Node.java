// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

import bna.bnlib.misc.Toolkit;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;


/**
 * Node of a Bayesian network holding it's variable, CPT as a factor and
 * connectivity information.
 * Mutable (but just from inside its own package).
 */
public class Node {
    // associated variables
    private Variable variable;
    private ArrayList<Node> parents = new ArrayList<Node>();
    private ArrayList<Node> children = new ArrayList<Node>();
    
    private Factor factor;
    
    
    public Node(Variable variable) {
        this(variable, null);
    }
    
    public Node(Variable variable, Factor factor) {
        this.variable = variable;
        this.setFactor(factor);
    }
    
    public Variable getVariable() {
        return this.variable;
    }
    
    public Factor getFactor() {
        return this.factor;
    }
    
    public int getParentCount() {
        return this.parents.size();
    }
    
    public int getChildrenCount() {
        return this.children.size();
    }
    
    public Variable[] getParentVariables() {
        Variable[] parentsArray = new Variable[this.parents.size()];
        for(int i = 0 ; i < this.parents.size() ; i++)
            parentsArray[i] = this.parents.get(i).getVariable();
        return parentsArray;
    }
    
    public Node[] getParentNodes() {
        Node[] parentsArray = new Node[this.parents.size()];
        return this.parents.toArray(parentsArray);
    }
    
    public Node[] getChildNodes() {
        Node[] childrenArray = new Node[this.children.size()];
        return this.children.toArray(childrenArray);
    }
    
    public Variable[] getChildVariables() {
        Variable[] childrenArray = new Variable[this.children.size()];
        for(int i = 0 ; i < this.children.size() ; i++)
            childrenArray[i] = this.children.get(i).getVariable();
        return childrenArray;
    }
    
    /**
     * Compute scope according to currently registered parent nodes.
     */
    public Variable[] getScope() {
        Variable[] scope = new Variable[1 + parents.size()];
        scope[0] = this.variable;
        for(int i = 0 ; i < this.parents.size() ; i++)
            scope[i + 1] = this.parents.get(i).getVariable();
        return scope;
    }
    
    public double getProbability(int[] assignment) {
        return this.factor.getProbability(assignment);
    }
    
    public int sampleVariable(int[] assignmentOfParents, Random random) {
        if(this.getParentCount() != assignmentOfParents.length)
            throw new BayesianNetworkRuntimeException("Invalid assignment of parents.");
        double[] probabilities = new double[this.variable.getCardinality()];
        int[] assignment = new int[1 + assignmentOfParents.length];
        System.arraycopy(assignmentOfParents, 0, assignment, 1, assignmentOfParents.length);
        // read probability of all possible assignments of the first variable in the factor given others
        for(int i = 0 ; i < this.variable.getCardinality() ; i++) {
            assignment[0] = i;
            probabilities[i] = this.factor.getProbability(assignment);
        }
        // sample
        return Toolkit.randomIndex(probabilities, 1.0, random);
    }
    
    
    // modification methods are package-private => users of the library cannot manipulate nodes directly
    
    void addParent(Node parent) throws BNLibInvalidStructuralModificationException {
        if(this.parents.contains(parent))
            throw new BNLibInvalidStructuralModificationException("The node is already in Parents(X).");
        this.parents.add(parent);
    }
    
    void addChild(Node child) throws BNLibInvalidStructuralModificationException {
        if(this.children.contains(child))
            throw new BNLibInvalidStructuralModificationException("The node is already in Children(X).");
        this.children.add(child);
    }
    
    void removeParent(Node parent) throws BNLibInvalidStructuralModificationException {
        if(!this.parents.contains(parent))
            throw new BNLibInvalidStructuralModificationException("The node is not in Parents(X).");
        this.parents.remove(parent);
    }
    
    void removeChild(Node child) throws BNLibInvalidStructuralModificationException {
         if(!this.children.contains(child))
            throw new BNLibInvalidStructuralModificationException("The node is not in Children(X).");
        this.children.remove(child);
    }
    
    void setProbabilityVector(double[] probs) throws BNLibIllegalCPDException {
        this.setFactor(new Factor(this.getScope(), probs));
    }
    
    void setFactor(Factor f) throws BNLibIllegalCPDException {
        if(f != null && !Toolkit.areEqual(f.getScope(), this.getScope()))
            throw new BNLibIllegalCPDException("Factor has invalid scope wrt. current parent nodes.");
        this.factor = f;
    }
    
    public boolean hasValidFactor() {
        // not null factor
        if(this.factor == null)
            return false;
        // consistent external cardinality
        int cardinalityByParents = this.variable.getCardinality();
        for(Node parent : this.parents)
            cardinalityByParents *= parent.getVariable().getCardinality();
        int cardinalityOfFactor = this.factor.getCardinality();
        if(cardinalityByParents != cardinalityOfFactor)
            return false;
        // consistent factor internally
        if(!this.factor.hasValidCardinality())
            return false;
        return true;
    }
    
    @Override
    /** Two nodes are considered equal if they hold equal variables. */
    public boolean equals(Object o) {
        if(o instanceof Node)
            return ((Node)o).variable.equals(this.variable);
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.variable);
        return hash;
    }
}
