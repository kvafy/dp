// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib.misc;

import java.util.ArrayList;

/**
 * A general digraph with valued nodes (each node has some associated data).
 */
public class Digraph {
    DigraphNode[] nodes;
    
    public Digraph(Object[] dataNodes) {
        this.nodes = new DigraphNode[dataNodes.length];
        for(int i = 0 ; i < dataNodes.length ; i++)
            this.nodes[i] = new DigraphNode(dataNodes[i]);
    }
    
    public void addEdge(Object uData, Object vData) {
        DigraphNode u = this.findNodeByData(uData),
                    v = this.findNodeByData(vData);
        u.out.add(v);
        v.in.add(u);
    }
    
    public boolean isAcyclic() {
        return this.topologicalSort() != null;
    }
    
    /**
     * Compute topological sort of given graph.
     * @return Return the node-associated data if the nodes are arranged in
     *         topological order. If the graph is not acyclic, returns null.
     */
    public Object[] topologicalSort() {
        Integer markNone = 0,
                markTemporary = 1,
                markFinal = 2;
        for(DigraphNode n : this.nodes)
            n.algorithmData = markNone;
        ArrayList order = new ArrayList();
        for(DigraphNode n : this.nodes)
            if(!this.topologicalSortVisit(n, order))
                return null;
        return order.toArray();
    }
    
    private boolean topologicalSortVisit(DigraphNode n, ArrayList order) {
        Integer markNone = 0,
                markTemporary = 1,
                markFinal = 2;
        if(n.algorithmData == markTemporary)
            return false; // not a DAG
        else if(n.algorithmData == markNone) {
            n.algorithmData = markTemporary;
            for(DigraphNode parent : n.in) {
                if(!this.topologicalSortVisit(parent, order))
                    return false;
            }
            n.algorithmData = markFinal;
            order.add(n.data);
        }
        return true;
    }
    
    private DigraphNode findNodeByData(Object data) {
        for(DigraphNode node : this.nodes)
            if(node.data.equals(data))
                return node;
        return null;
    }
}

class DigraphNode {
    ArrayList<DigraphNode> in = new ArrayList<DigraphNode>();
    ArrayList<DigraphNode> out = new ArrayList<DigraphNode>();
    Object data; // the user data
    Object algorithmData;
    
    public DigraphNode(Object data) {
        this.data = data;
    }
}
