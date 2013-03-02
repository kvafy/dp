// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

import bna.bnlib.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * General Bayesian network class.
 * The network consists of network nodes (Node class), each node has associated
 * variable, collection of parents and children, a factor holding CPT of this
 * node.
 */
public class BayesianNetwork {
    private Node[] nodes;
    
    
    public BayesianNetwork(Variable[] variables) {
        this.nodes = new Node[variables.length];
        for(int i = 0 ; i < variables.length ; i++)
            this.nodes[i] = new Node(variables[i]);
    }
    
    /** Create a deep copy of given network. */
    public BayesianNetwork(BayesianNetwork original) {
        try {
            // duplicate nodes (without connections)
            this.nodes = new Node[original.nodes.length];
            for(int i = 0 ; i < this.nodes.length ; i++) {
                Node nodeOrig = original.nodes[i];
                this.nodes[i] = new Node(nodeOrig.getVariable(), nodeOrig.getFactor());
            }
            // duplicate structure
            for(Node nodeOrigParent : original.nodes) {
                String varParent = nodeOrigParent.getVariable().getName();
                for(Node nodeOrigChild : nodeOrigParent.getChildNodes()) {
                    String varChild = nodeOrigChild.getVariable().getName();
                    this.addDependency(varParent, varChild);
                }
            }
        }
        catch(BayesianNetworkException bnex) {
            throw new RuntimeException("Internal error while replicating a network.");
        }
    }
    
    
    // methods for building/editing a Bayesian network (see BayesianNetworkFileReader or AlterationAction)
    
    public void addDependency(String parent, String child) throws BayesianNetworkException {
        this.addDependency(this.getNode(parent), this.getNode(child));
    }
    
    public void addDependency(Variable parent, Variable child) throws BayesianNetworkException {
        this.addDependency(this.getNode(parent), this.getNode(child));
    }
    
    public void addDependency(Node parent, Node child) throws BayesianNetworkException {
        if(!this.containsVariable(child.getVariable()) || !this.containsVariable(parent.getVariable()))
            throw new BayesianNetworkException("One of the variables is not in the network.");
        parent.addChild(child);
        child.addParent(parent);
    }
    
    public void removeDependency(Variable parent, Variable child) throws BayesianNetworkException {
        this.removeDependency(this.getNode(parent), this.getNode(child));
    }
    
    public void removeDependency(Node parent, Node child) throws BayesianNetworkException {
        if(!this.containsVariable(child.getVariable()) || !this.containsVariable(parent.getVariable()))
            throw new BayesianNetworkException("One of the variables is not in the network.");
        
        parent.removeChild(child);
        child.removeParent(parent);
    }
    
    public void reverseDependency(Variable parent, Variable child) throws BayesianNetworkException {
        this.reverseDependency(this.getNode(parent), this.getNode(child));
    }
    
    public void reverseDependency(Node parent, Node child) throws BayesianNetworkException {
        this.removeDependency(parent, child);
        this.addDependency(child, parent);
    }
    
    public void setCPT(String variable, double[] probs) throws BayesianNetworkRuntimeException {
        Node node = this.getNode(variable);
        node.setProbabilityVector(probs);
    }
    
    public void setCPT(String variable, Factor cpt) {
        Node node = this.getNode(variable);
        node.setFactor(cpt);
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
    
    public Variable[] getVariables() {
        Variable[] vars = new Variable[this.nodes.length];
        for(int i = 0 ; i < this.nodes.length ; i++)
            vars[i] = this.nodes[i].getVariable();
        return vars;
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
    
    public Node[] getNodes() {
        return Arrays.copyOf(this.nodes, this.nodes.length);
    }
    
    /** Is there a node with variable v? */
    private boolean containsVariable(Variable v) {
        for(Node node : this.nodes)
            if(v == node.getVariable())
                return true;
        return false;
    }
    
    public int getVariablesCount() {
        return this.nodes.length;
    }
    
    
    // Graph operations
    
    public Variable[] topologicalSort() {
        Node[] topologicalOrderNodes = this.topologicalSortNodes();
        Variable[] topologicalOrderVariables = new Variable[topologicalOrderNodes.length];
        for(int i = 0 ; i < topologicalOrderNodes.length ; i++)
            topologicalOrderVariables[i] = topologicalOrderNodes[i].getVariable();
        return topologicalOrderVariables;
    }
    
    public Node[] topologicalSortNodes() {
        Digraph digraph = this.convertToDigraph();
        Object[] topologicalOrderObjects = digraph.topologicalSort();
        Node[] topologicalOrderNodes = new Node[topologicalOrderObjects.length];
        for(int i = 0 ; i < topologicalOrderObjects.length ; i++)
            topologicalOrderNodes[i] = (Node)topologicalOrderObjects[i];
        return topologicalOrderNodes;
    }
    
    private Digraph convertToDigraph() {
        Digraph g = new Digraph(this.nodes);
        for(Node u : this.nodes) {
            for(Node v : u.getChildNodes())
                g.addEdge(u, v);
        }
        return g;
    }
    
    /**
     * Compute adjacency matrix.
     * Indices of variables/nodes in the resulting matrix are the same as in
     * the nodeOrder parameter.
     */
    boolean[][] adjacencyMatrix(Node[] nodeOrder) {
        final int NODE_COUNT = nodeOrder.length;
        boolean[][] matrix = new boolean[NODE_COUNT][NODE_COUNT];
        for(int i = 0 ; i < NODE_COUNT ; i++) {
            Node ithNode = nodeOrder[i];
            for(Node child : ithNode.getChildNodes()) {
                int j = Toolkit.indexOf(nodeOrder, child);
                matrix[i][j] = true;
            }
        }
        return matrix;
    }
    
    public String dumpStructure() {
        StringBuilder ret = new StringBuilder();
        for(Node node : this.topologicalSortNodes()) { // in a nice top-down manner
            ret.append(node.getVariable().getName());
            ret.append(" <- ");
            ret.append("[");
            boolean first = true;
            for(Node parent : node.getParentNodes()) {
                if(!first)
                    ret.append(", ");
                first = false;
                ret.append(parent.getVariable().getName());
            }
            ret.append("]");
            ret.append(System.lineSeparator());
        }
        return ret.toString();
    }
    
    public String dumpCPTs() {
        StringBuilder ret = new StringBuilder();
        boolean firstLine = true;
        for(Node node : this.topologicalSortNodes()) { // in a nice top-down manner
            if(!firstLine)
                ret.append(System.lineSeparator());
            ret.append(node.getFactor().toString());
            firstLine = false;
        }
        return ret.toString();
    }
}
