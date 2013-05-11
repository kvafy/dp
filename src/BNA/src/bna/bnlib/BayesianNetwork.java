// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

import bna.bnlib.io.*;
import bna.bnlib.misc.Digraph;
import bna.bnlib.misc.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Data representation of a Bayesian network.
 * The network consists of network nodes (instances of Node), each node has
 * associated a variable, collection of parents and children (Nodes) and
 * a factor holding CPT of this node.
 */
public class BayesianNetwork {
    private Node[] nodes;
    
    
    /**
     * Creates discrete Bayesian network with given variables.
     * @throws BNLibIllegalNetworkSpecificationException When the variables
     *         are not mutually unique.
     */
    public BayesianNetwork(Variable[] variables) throws BNLibIllegalNetworkSpecificationException {
        if(!Toolkit.unique(variables))
            throw new BNLibIllegalNetworkSpecificationException("The variables are not unique.");
        this.nodes = new Node[variables.length];
        for(int i = 0 ; i < variables.length ; i++)
            this.nodes[i] = new Node(variables[i]);
    }
    
    /** Create a deep copy of given network. */
    private BayesianNetwork(BayesianNetwork original, boolean copyStructure, boolean copyCPDs) throws BNLibInternalException {
        try {
            // duplicate nodes (for now, without connections and CPDs)
            this.nodes = new Node[original.nodes.length];
            for(int i = 0 ; i < this.nodes.length ; i++) {
                Node nodeOrig = original.nodes[i];
                this.nodes[i] = new Node(nodeOrig.getVariable());
            }
            if(!copyStructure)
                return;
            // duplicate structure
            for(Node nodeOrigParent : original.nodes) {
                String varParent = nodeOrigParent.getVariable().getName();
                for(Node nodeOrigChild : nodeOrigParent.getChildNodes()) {
                    String varChild = nodeOrigChild.getVariable().getName();
                    this.addDependency(varParent, varChild);
                }
            }
            if(!copyCPDs)
                return;
            // duplicate CPDs (the scope checking will pass as structure has been established)
            for(Node node : this.nodes) {
                Variable variable = node.getVariable();
                node.setFactor(original.getNode(variable).getFactor());
            }
            // else the Factors for CPDs will remain null
        }
        catch(BNLibException ex) {
            throw new BNLibInternalException("Internal error while replicating a network: " + ex.getMessage());
        }
    }
    
    /** Create a discrete Bayesian network (no edges) with the same variables as this network has. */
    public BayesianNetwork copyEmptyStructure() {
        return new BayesianNetwork(this, false, false);
    }
    
    /** Duplicate the network's structure, but set empty CPDs (as nulls). */
    public BayesianNetwork copyStructureWithEmptyCPDs() {
        return new BayesianNetwork(this, true, false);
    }
    
    /** Completely duplicate the network, ie. structure and CPDs. */
    public BayesianNetwork copyStructureAndCPDs() {
        return new BayesianNetwork(this, true, true);
    }
    
    
    // methods for building/editing a Bayesian network (see BayesianNetworkFileReader and AlterationAction)
    
    public void addDependency(String parent, String child) throws BNLibIllegalStructuralModificationException {
        this.addDependency(this.getNode(parent), this.getNode(child));
    }
    
    public void addDependency(Variable parent, Variable child) throws BNLibIllegalStructuralModificationException {
        this.addDependency(this.getNode(parent), this.getNode(child));
    }
    
    public void addDependency(Node parent, Node child) throws BNLibIllegalStructuralModificationException {
        if(!this.containsVariable(child.getVariable()) || !this.containsVariable(parent.getVariable()))
            throw new BNLibIllegalStructuralModificationException("One of the variables you want to connect is not in the network.");
        parent.addChild(child);
        child.addParent(parent);
    }
    
    public void removeDependency(Variable parent, Variable child) throws BNLibIllegalStructuralModificationException {
        this.removeDependency(this.getNode(parent), this.getNode(child));
    }
    
    public void removeDependency(Node parent, Node child) throws BNLibIllegalStructuralModificationException {
        if(!this.containsVariable(child.getVariable()) || !this.containsVariable(parent.getVariable()))
            throw new BNLibIllegalStructuralModificationException("One of the variables you want to disconnect is not in the network.");
        
        parent.removeChild(child);
        child.removeParent(parent);
    }
    
    public void reverseDependency(Variable parent, Variable child) throws BNLibIllegalStructuralModificationException {
        this.reverseDependency(this.getNode(parent), this.getNode(child));
    }
    
    public void reverseDependency(Node parent, Node child) throws BNLibIllegalStructuralModificationException {
        this.removeDependency(parent, child);
        this.addDependency(child, parent);
    }
    
    public void setCPT(String variable, double[] probs) throws BNLibIllegalCPDException {
        Node node = this.getNode(variable);
        node.setProbabilityVector(probs);
    }
    
    public void setCPT(String variable, Factor cpt) throws BNLibIllegalCPDException {
        Node node = this.getNode(variable);
        node.setFactor(cpt);
    }
    
    /** Do the two networks have exactly the same set of edges? */
    public boolean equalsStructurally(BayesianNetwork bn) {
        if(!Toolkit.areEqual(this.getVariables(), bn.getVariables()))
            return false;
        for(Node n : this.nodes) {
            Node nn = bn.getNode(n.getVariable());
            if(!Toolkit.areEqual(n.getChildVariables(), nn.getChildVariables()))
                return false;
        }
        return true;
    }
    
    /** Check whether "descendant" is an (indirect) descendant of "var". */
    public boolean hasDescendant(Variable var, Variable descendant) {
        LinkedList<Node> openQueue = new LinkedList<Node>();
        openQueue.add(this.getNode(var));
        while(!openQueue.isEmpty()) {
            Node firstNode = openQueue.removeFirst();
            if(firstNode.getVariable().equals(descendant))
                return true;
            for(Node childNode : firstNode.getChildNodes())
                openQueue.addLast(childNode);
        }
        return false;
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
     * @throws BNLibIllegalNetworkSpecificationException When some of the
     *         consistency criteria is broken.
     */
    public void validate() throws BNLibIllegalNetworkSpecificationException {
        this.validateAcyclicity();
        this.validateVariableUniqueness();
        this.validateFactors();
    }
    
    private void validateAcyclicity() throws BNLibIllegalNetworkSpecificationException {
        // create a general graph representation and test for acyclicity
        Digraph g = this.convertToDigraph();
        if(!g.isAcyclic())
            throw new BNLibIllegalNetworkSpecificationException("The network is not acyclic.");
    }
    
    private void validateVariableUniqueness() throws BNLibIllegalNetworkSpecificationException {
        ArrayList<String> names = new ArrayList<String>();
        for(Node n : this.nodes)
            names.add(n.getVariable().getName());
        if(!Toolkit.unique(names))
            throw new BNLibIllegalNetworkSpecificationException("Variable names are not unique.");
    }
    
    private void validateFactors() throws BNLibIllegalNetworkSpecificationException {
        for(Node n : this.nodes)
            if(!n.hasValidFactor())
                throw new BNLibIllegalNetworkSpecificationException("Variable \"" + n.getVariable().getName() + "\" has invalid factor.");
    }
    
    /**
     * Load Bayesian network from given file.
     * Currently only the .net format of files is recognized and supported.
     * @throws BNLibIOException When an IO exception occurs or the file
     *         is corrupted (syntactically or semantically wrong).
     */
    public static BayesianNetwork loadFromFile(String filename) throws BNLibIOException {
        int lastdotPos = filename.lastIndexOf('.');
        String extension = (lastdotPos == -1) ? "" : filename.substring(lastdotPos + 1);
        
        BayesianNetworkFileReader reader;
        if(extension.equals("net"))
            reader = new BayesianNetworkNetFileReader(filename); // throws BNLibIOException
        else {
            int dirSeparatorPos = filename.lastIndexOf(System.getProperty("file.separator"));
            String basename = (dirSeparatorPos == -1) ? filename : filename.substring(dirSeparatorPos + 1);
            String msg = String.format("Unknown format of file \"%s\" (by extension).", basename);
            throw new BNLibIOException(msg);
        }
        return reader.load();
    }
    
    /**
     * Get variable of the given name.
     * @throws BNLibNonexistentVariableException When no such variable exists
     *         in this network.
     */
    public Variable getVariable(String variableName) throws BNLibNonexistentVariableException {
        return this.getNode(variableName).getVariable();
    }
    
    /**
     * Get parent variables of the specified variable.
     * @throws BNLibNonexistentVariableException When no such variable exists
     *         in this network.
     */
    public Variable[] getVariableParents(Variable variable) throws BNLibNonexistentVariableException {
        return this.getNode(variable).getParentVariables();
    }
    
    /**
     * Get child variables of the specified variable.
     * @throws BNLibNonexistentVariableException When no such variable exists
     *         in this network.
     */
    public Variable[] getVariableChildren(Variable variable) throws BNLibNonexistentVariableException {
        return this.getNode(variable).getChildVariables();
    }
    
    /**
     * Get the number of parent variables of the specified variable.
     * @throws BNLibNonexistentVariableException When no such variable exists
     *         in this network.
     */
    public int getVariableParentsCount(Variable variable) throws BNLibNonexistentVariableException {
        return this.getNode(variable).getParentCount();
    }
    
    /**
     * Get the number of child variables of the specified variable.
     * @throws BNLibNonexistentVariableException When no such variable exists
     *         in this network.
     */
    public int getVariableChildrenCount(Variable variable) throws BNLibNonexistentVariableException {
        return this.getNode(variable).getChildrenCount();
    }
    
    /** Get all variables of this network. */
    public Variable[] getVariables() {
        Variable[] vars = new Variable[this.nodes.length];
        for(int i = 0 ; i < this.nodes.length ; i++)
            vars[i] = this.nodes[i].getVariable();
        return vars;
    }
    
    /** Get the numer of variables in this network. */
    public int getVariablesCount() {
        return this.getNodeCount();
    }
    
    /** Get the number of nodes (variables) of this network. */
    public int getNodeCount() {
        return this.nodes.length;
    }
    
    /**
     * Get node representing the given variable.
     * @throws BNLibNonexistentVariableException When no such variable exists
     *         in this network.
     */
    public Node getNode(String variableName) throws BNLibNonexistentVariableException {
        for(Node n : this.nodes)
            if(n.getVariable().getName().equals(variableName))
                return n;
        throw new BNLibNonexistentVariableException("Node with variable \"" + variableName + "\" is not in the network.");
    }
    
    /**
     * Get node representing the given variable.
     * @throws BNLibNonexistentVariableException When no such variable exists
     *         in this network.
     */
    public Node getNode(Variable variable) throws BNLibNonexistentVariableException {
        for(Node n : this.nodes)
            if(n.getVariable().equals(variable))
                return n;
        throw new BNLibNonexistentVariableException("Node with variable \"" + variable.getName() + "\" is not in the network.");
    }
    
    /** Get all nodes of this network. */
    public Node[] getNodes() {
        return Arrays.copyOf(this.nodes, this.nodes.length);
    }
    
    /** Check whether all the nodes have valid CPDs wrt the set of parents. */
    public boolean hasValidCPDs() {
        for(Node n : this.nodes)
            if(!n.hasValidFactor())
                return false;
        return true;
    }
    
    /** Is there a node with variable v in this network? */
    private boolean containsVariable(Variable v) {
        for(Node node : this.nodes)
            if(v == node.getVariable())
                return true;
        return false;
    }
    
     /** Return number of degrees of freedom of the network wrt CPD entries. */
    public int getDegreesOfFreedomInCPDs() {
        int degreesOfFreedom = 0;
        for(Node v : this.nodes) {
            int vCard = v.getVariable().getCardinality();
            int parentsCard = Toolkit.cardinality(v.getParentVariables());
            degreesOfFreedom += (vCard - 1) * parentsCard;
        }
        return degreesOfFreedom;
    }
    
    /** Return the number of edges in this network. */
    public int getEdgeCount() {
        int edgeCount = 0;
        for(Node v : this.nodes)
            edgeCount += v.getChildrenCount();
        return edgeCount;
    }
    
    /** Network dimension is considered to be the number of degress of freedom wrt CPD entries. */
    public int getNetworkDimension() {
        return this.getDegreesOfFreedomInCPDs();
    }
    
    /** Provides an overall statistics of this network (edge counts, min/max/average in/out-degree etc.). */
    public BayesianNetworkStatistics getStatistics() {
        BayesianNetworkStatistics statistics = new BayesianNetworkStatistics();
        boolean hasCPDs = this.hasValidCPDs();
        int inDegreeSum = 0,
            outDegreeSum = 0;
        
        statistics.nodes = this.getNodeCount();
        statistics.inDegreeMax = statistics.outDegreeMax = 0;
        statistics.inDegreeMin = statistics.outDegreeMin = Integer.MAX_VALUE;
        statistics.degreesOfFreedom = 0;
        
        for(Node n : this.nodes) {
            int inDegree = n.getParentCount(),
                outDegree = n.getChildrenCount();
            statistics.edges += outDegree;
            inDegreeSum += inDegree;
            outDegreeSum += outDegree;
            statistics.inDegreeMin = Math.min(statistics.inDegreeMin, inDegree);
            statistics.inDegreeMax = Math.max(statistics.inDegreeMax, inDegree);
            statistics.outDegreeMin = Math.min(statistics.outDegreeMin, outDegree);
            statistics.outDegreeMax = Math.max(statistics.outDegreeMax, outDegree);
            if(hasCPDs) {
                int nCard = n.getVariable().getCardinality();
                int parentsCard = Toolkit.cardinality(n.getParentVariables());
                statistics.degreesOfFreedom += (nCard - 1) * parentsCard;
            }
        }
        
        if(statistics.edges > 0) {
            statistics.inDegreeAve = ((double)inDegreeSum) / statistics.nodes;
            statistics.outDegreeAve = ((double)outDegreeSum) / statistics.nodes;
        }
        else {
            statistics.inDegreeAve = statistics.outDegreeAve = 0.0;
            statistics.inDegreeMin = statistics.outDegreeMin = 0; // compensate for Integer.MAX_VALUE
        }
        
        return statistics;
    }
    
    
    // Graph operations
    
    /**
     * Return variables of this network in topological order.
     * @throws BNLibIllegalNetworkSpecificationException When the network is not
     *         acyclic (therefore topological sort can't exist).
     */
    public Variable[] topologicalSort() throws BNLibIllegalNetworkSpecificationException {
        Node[] topologicalOrderNodes = this.topologicalSortNodes();
        Variable[] topologicalOrderVariables = new Variable[topologicalOrderNodes.length];
        for(int i = 0 ; i < topologicalOrderNodes.length ; i++)
            topologicalOrderVariables[i] = topologicalOrderNodes[i].getVariable();
        return topologicalOrderVariables;
    }
    
    /**
     * Return nodes of this network in topological order.
     * @throws BNLibIllegalNetworkSpecificationException When the network is not
     *         acyclic (therefore topological sort can't exist).
     */
    public Node[] topologicalSortNodes() throws BNLibIllegalNetworkSpecificationException {
        Digraph digraph = this.convertToDigraph();
        Object[] topologicalOrderObjects = digraph.topologicalSort();
        if(topologicalOrderObjects == null)
            throw new BNLibIllegalNetworkSpecificationException("Network is not acyclic.");
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
    
    /** Create a textual representation of the structure of this network. */
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
    
    /** Create textual representation of all the CPDs of this network. */
    public String dumpCPTs() {
        StringBuilder ret = new StringBuilder();
        boolean firstLine = true;
        for(Node node : this.topologicalSortNodes()) { // in a nice top-down manner
            if(!firstLine) {
                ret.append(System.lineSeparator());
                ret.append(System.lineSeparator());
            }
            ret.append(node.getFactor().toString());
            firstLine = false;
        }
        return ret.toString();
    }
    
    
    /** Data structure that captures structural differences of two networks. */
    public static class StructuralDifference {
        private ArrayList<Variable[]> missingEdges,
                                      reversedEdges,
                                      redundantEdges;
        
        public StructuralDifference(BayesianNetwork bnReferential, BayesianNetwork bnOther) {
            this.missingEdges = new ArrayList<Variable[]>();
            this.reversedEdges = new ArrayList<Variable[]>();
            this.redundantEdges = new ArrayList<Variable[]>();
            // broken edges (reversed or totally missing)
            for(Node nodeReferential : bnReferential.getNodes()) {
                Node nodeOther = bnOther.getNode(nodeReferential.getVariable());
                Variable[] nodeOtherNeighbours = Toolkit.union(nodeOther.getParentVariables(), nodeOther.getChildVariables());
                for(Variable childReferential : nodeReferential.getChildVariables()) {
                    if(!Toolkit.arrayContains(nodeOtherNeighbours, childReferential))
                        this.missingEdges.add(new Variable[] {nodeReferential.getVariable(), childReferential});
                    else if(!Toolkit.arrayContains(nodeOther.getChildVariables(), childReferential))
                        this.reversedEdges.add(new Variable[] {nodeReferential.getVariable(), childReferential});
                }
            }
            // redundant edges
            for(Node nodeOther : bnOther.getNodes()) {
                Node nodeReferential = bnReferential.getNode(nodeOther.getVariable());
                Variable[] nodeOrigNeighbours = Toolkit.union(nodeReferential.getParentVariables(), nodeReferential.getChildVariables());
                for(Variable childOther : nodeOther.getChildVariables()) {
                    if(!Toolkit.arrayContains(nodeOrigNeighbours, childOther)) {
                        this.redundantEdges.add(new Variable[] {nodeOther.getVariable(), childOther});
                    }
                }
            }
        }
        
        /** Returns missing edges u->v in the form of 2-tuples (arrays of length 2). */
        public List<Variable[]> getMissingEdges() {
            return Collections.unmodifiableList(this.missingEdges);
        }
        
        /** Returns reversed edges u->v in the form of 2-tuples (arrays of length 2). */
        public List<Variable[]> getReversedEdges() {
            return Collections.unmodifiableList(this.reversedEdges);
        }
        
        /** Returns redundant edges u->v in the form of 2-tuples (arrays of length 2). */
        public List<Variable[]> getRedundantEdges() {
            return Collections.unmodifiableList(this.redundantEdges);
        }
    }
}
