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
 * Mutable (but just from inside its own package by package-private methods).
 */
public class Node {
    private Variable variable;
    private ArrayList<Node> parents = new ArrayList<Node>();
    private ArrayList<Node> children = new ArrayList<Node>();
    private Factor factor; // CPD
    
    
    /** Create a node with empty CPD. */
    public Node(Variable variable) {
        this(variable, null);
    }
    
    /** Create a node with the given cpd. */
    public Node(Variable variable, Factor factor) {
        this.variable = variable;
        this.setFactor(factor);
    }
    
    /** Get variable represented by this node. */
    public Variable getVariable() {
        return this.variable;
    }
    
    /** Get CPD associated with this node. */
    public Factor getFactor() {
        return this.factor;
    }
    
    /** Get the number of parents of this node. */
    public int getParentCount() {
        return this.parents.size();
    }
    
    /** Get the number of children of this node. */
    public int getChildrenCount() {
        return this.children.size();
    }
    
    /** Get parent variables of this node. */
    public Variable[] getParentVariables() {
        Variable[] parentsArray = new Variable[this.parents.size()];
        for(int i = 0 ; i < this.parents.size() ; i++)
            parentsArray[i] = this.parents.get(i).getVariable();
        return parentsArray;
    }
    
    /** Get parent nodes of this variable. */
    public Node[] getParentNodes() {
        Node[] parentsArray = new Node[this.parents.size()];
        return this.parents.toArray(parentsArray);
    }
    
    /** Get child variables of this node. */
    public Variable[] getChildVariables() {
        Variable[] childrenArray = new Variable[this.children.size()];
        for(int i = 0 ; i < this.children.size() ; i++)
            childrenArray[i] = this.children.get(i).getVariable();
        return childrenArray;
    }
    
    /** Get child nodes of this variable. */
    public Node[] getChildNodes() {
        Node[] childrenArray = new Node[this.children.size()];
        return this.children.toArray(childrenArray);
    }
    
    /** Compute scope according to currently registered parent nodes. */
    public Variable[] getScope() {
        Variable[] scope = new Variable[1 + parents.size()];
        scope[0] = this.variable;
        for(int i = 0 ; i < this.parents.size() ; i++)
            scope[i + 1] = this.parents.get(i).getVariable();
        return scope;
    }
    
    /** Returns value of this factor associated with the given assignment. */
    public double getProbability(int[] assignment) {
        return this.factor.getProbability(assignment);
    }
    
    /**
     * Return a random assignment of this node from the distribution P(X | parents).
     * @throws BNLibInvalidInstantiationException When the given assignment
     *         is not a valid assignment of parent variables of this node.
     */
    public int sampleVariable(int[] assignmentOfParents, Random random) throws BNLibInvalidInstantiationException {
        if(!Toolkit.validateAssignment(this.getParentVariables(), assignmentOfParents)) // TODO defensive
            throw new BNLibInvalidInstantiationException("Invalid assignment of parents.");
        double[] probabilities = new double[this.variable.getCardinality()];
        int[] assignment = new int[1 + assignmentOfParents.length];
        System.arraycopy(assignmentOfParents, 0, assignment, 1, assignmentOfParents.length);
        // read probability of all possible assignments of the first variable in the factor given others
        double probabilitiesSum = 0;
        for(int i = 0 ; i < this.variable.getCardinality() ; i++) {
            assignment[0] = i;
            probabilities[i] = this.factor.getProbability(assignment);
            probabilitiesSum += probabilities[i];
        }
        if(probabilitiesSum == 0) {
            // TODO make uniform or something?
            java.util.Arrays.fill(probabilities, 1.0 / probabilities.length);
        }
        // sample
        return Toolkit.randomIndex(probabilities, 1.0, random);
    }
    
    
    // modification methods are package-private => users of the library cannot manipulate nodes directly
    
    void addParent(Node parent) throws BNLibIllegalStructuralModificationException {
        if(this.parents.contains(parent))
            throw new BNLibIllegalStructuralModificationException("The node is already in Parents(X).");
        if(this.variable.equals(parent.variable))
            throw new BNLibIllegalStructuralModificationException("Variable cannot be parent to itself.");
        this.parents.add(parent);
    }
    
    void addChild(Node child) throws BNLibIllegalStructuralModificationException {
        if(this.children.contains(child))
            throw new BNLibIllegalStructuralModificationException("The node is already in Children(X).");
        if(this.variable.equals(child.variable))
            throw new BNLibIllegalStructuralModificationException("Variable cannot be child to itself.");
        this.children.add(child);
    }
    
    void removeParent(Node parent) throws BNLibIllegalStructuralModificationException {
        if(!this.parents.contains(parent))
            throw new BNLibIllegalStructuralModificationException("The node is not in Parents(X).");
        this.parents.remove(parent);
    }
    
    void removeChild(Node child) throws BNLibIllegalStructuralModificationException {
         if(!this.children.contains(child))
            throw new BNLibIllegalStructuralModificationException("The node is not in Children(X).");
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
    
    /** Check whether the CPD of this node is legal wrt the set of parent variables. */
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
