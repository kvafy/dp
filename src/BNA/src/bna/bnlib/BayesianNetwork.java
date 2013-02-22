// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

import bna.bnlib.io.*;
import java.util.ArrayList;

/**
 *
 * @author kvafy
 */
public class BayesianNetwork {
    private Node[] nodes;
    
    
    public BayesianNetwork(Variable[] variables) {
        this.nodes = new Node[variables.length];
        for(int i = 0 ; i < variables.length ; i++)
            this.nodes[i] = new Node(variables[i]);
    }
    
    
    // methods for building a Bayesian network (see BayesianNetworkFileReader)
    
    public void addDependency(String parent, String child) throws BayesianNetworkException {
        this.addDependency(this.getNode(parent), this.getNode(child));
    }
    
    public void addDependency(Node parent, Node child) throws BayesianNetworkException {
        if(!this.containsVariable(child.getVariable()) || !this.containsVariable(parent.getVariable()))
            throw new BayesianNetworkException("One of the variables is not in the network.");
        
        parent.addChild(child);
        child.addParent(parent);
    }
    
    public void setCPT(String variable, double[] probs) throws BayesianNetworkException {
        Node node = this.getNode(variable);
        node.setProbabilityVector(probs);
    }
    
    public void setCPT(String variable, Double[] probs) throws BayesianNetworkException {
        // transform to "double[]" array
        double[] probsPrimitive = new double[probs.length];
        for(int i = 0 ; i < probs.length ; i++)
            probsPrimitive[i] = probs[i];
        // delegate
        this.setCPT(variable, probsPrimitive);
    }
    
    /**
     * Check consistency of the network.
     * Following conditions have to be met for a network to be valid:
     * <ul>
     *   <li> network acyclicity
     *   <li> variable uniqueness
     *   <li> factors with correct value-vector sizes wrt cardinality of each
     *        variable and its parents
     *   <li> normalized factors (optional for non-skeletal networks)
     * </ul>
     */
    public void validate() throws BayesianNetworkException {
        this.validateAcyclicity();
        this.validateVariableUniqueness();
        this.validateFactors();
    }
    
    private void validateAcyclicity() throws BayesianNetworkException {
        // create a general graph representation and test for acyclicity
        Digraph g = this.convertToDigraph();
        if(!g.isAcyclic())
            throw new BayesianNetworkException("The network is not acyclic.");
    }
    
    private void validateVariableUniqueness() throws BayesianNetworkException {
        ArrayList<String> names = new ArrayList<String>();
        for(Node n : this.nodes)
            names.add(n.getVariable().getName());
        if(!Toolkit.unique(names))
            throw new BayesianNetworkException("Variable names are not unique.");
    }
    
    private void validateFactors() throws BayesianNetworkException {
        for(Node n : this.nodes)
            if(!n.hasValidFactor())
                throw new BayesianNetworkException("Variable \"" + n.getVariable().getName() + "\" has invalid factor.");
    }
    
    public Digraph convertToDigraph() {
        Digraph g = new Digraph(this.nodes);
        for(Node u : this.nodes) {
            for(Node v : u.getChildNodes())
                g.addEdge(u, v);
        }
        return g;
    }
    
    public static BayesianNetwork loadFromFile(String filename) throws BayesianNetworkException {
        // all known file readers for various Bayesian network formats
        BayesianNetworkFileReader[] readers = {new BayesianNetworkNetFileReader(filename)};
        
        for(BayesianNetworkFileReader reader : readers) {
            try {
                BayesianNetwork bn = reader.load();
                bn.validate();
                return bn;
            }
            // read unsuccesfull => try another reader
            catch(BayesianNetworkException bnex) {}
            catch(BayesianNetworkRuntimeException bnex) {}
        }
        throw new BayesianNetworkException("Unable to read file \"" + filename + "\" (unknown format or corrupted file).");
    }
    
    public Variable getVariable(String variableName) {
        return this.getNode(variableName).getVariable();
    }
    
    public Variable[] getVariableParents(Variable variable) {
        return this.getNode(variable).getParentVariables();
    }
    
    public Variable[] getVariableChildren(Variable variable) {
        return this.getNode(variable).getChildVariables();
    }
    
    public int getVariableParentsCount(Variable variable) {
        return this.getNode(variable).getParentCount();
    }
    
    public int getVariableChildrenCount(Variable variable) {
        return this.getNode(variable).getChildrenCount();
    }
    
    public Variable[] getVariableMarkovBlanket(Variable variable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Node getNode(String variableName) {
        for(Node n : this.nodes)
            if(n.getVariable().getName().equals(variableName))
                return n;
        throw new BayesianNetworkRuntimeException("Node with variable \"" + variableName + "\" is not in the network.");
    }
    
    public Node getNode(Variable variable) {
        for(Node n : this.nodes)
            if(n.getVariable().equals(variable))
                return n;
        throw new BayesianNetworkRuntimeException("Node with variable \"" + variable.getName() + "\" is not in the network.");
    }
    
    /** Is there a node with variable v? */
    private boolean containsVariable(Variable v) {
        for(Node node : this.nodes)
            if(v == node.getVariable())
                return true;
        return false;
    }
    
    public Variable[] getVariables() {
        Variable[] vars = new Variable[this.nodes.length];
        for(int i = 0 ; i < this.nodes.length ; i++)
            vars[i] = this.nodes[i].getVariable();
        return vars;
    }
    
    public int getVariablesCount() {
        return this.nodes.length;
    }
    
    public Variable[] topologicalSort() {
        Digraph digraph = this.convertToDigraph();
        Object[] topologicalOrderObjects = digraph.topologicalSort();
        Variable[] topologicalOrderVariables = new Variable[topologicalOrderObjects.length];
        for(int i = 0 ; i < topologicalOrderObjects.length ; i++)
            topologicalOrderVariables[i] = ((Node)topologicalOrderObjects[i]).getVariable();
        return topologicalOrderVariables;
    }
}
