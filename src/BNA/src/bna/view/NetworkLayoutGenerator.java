// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/19

package bna.view;

import bna.bnlib.BayesianNetwork;
import bna.bnlib.Node;
import bna.bnlib.misc.Toolkit;
import java.util.*;


/**
 * For a Bayesian network determine an estetically pleasing layout.
 */
public class NetworkLayoutGenerator {
    
    final static long INLAYER_ITERATIONS = 10 * 1000; // number of iterations in the relative inlayer ordering optimization
    final static long ITERATIONS_TOTAL = INLAYER_ITERATIONS;
    
    
    public static GBayesianNetwork getLayout(BayesianNetwork bn, NetworkLayoutGeneratorObserver observer) {
        Node[] topsortNode = bn.topologicalSortNodes();
        // !!! for most methods it is important to work with topologically sorted nodes
        
        // create structurally equivalent representation (ie. with edges), only with LNodes
        // (set .node, .children, .parents properties)
        LNode[] topsortLNode = NetworkLayoutGenerator.copyNodeStructure(topsortNode);
        
        // assign nodes to layers (topsort forward & backward pass)
        // (set .layer property)
        NetworkLayoutGenerator.assignNodesToLayers(topsortLNode);
        System.out.println("after assignning layers:");
        for(LNode n : topsortLNode) {
            String label = n.node != null ? n.node.getVariable().getName() : "<dummy>";
            System.out.printf(" - variable %s, layer %d\n", label, n.layer);
        }
        
        // insert dummy nodes and adjust the links
        topsortLNode = NetworkLayoutGenerator.insertDummyNodes(topsortLNode);
        System.out.println("after inserting dummy nodes:");
        for(LNode n : topsortLNode) {
            String label = n.node != null ? n.node.getVariable().getName() : "<dummy>";
            System.out.printf(" - variable %s, layer %d\n", label, n.layer);
        }
        
        // preliminary node orderings (fully deterministic, for each node
        // in layer: append its children to the next layer if not already present)
        // (set .inlayerIndex property)
        LNode[][] relativeOrder = NetworkLayoutGenerator.preliminaryInlayerOrdering(topsortLNode);
        System.out.println("preliminary ordering:");
        for(int layer = 0 ; layer < relativeOrder.length ; layer++) {
            System.out.printf(" - layer %d: ", layer);
            for(LNode n : relativeOrder[layer]) {
                String label = n.node != null ? n.node.getVariable().getName() : "<dummy>";
                System.out.printf("%s  ", label);
            }
            System.out.println("");
        }
        
        // optimize crossings (relative order of nodes on layers)
        NetworkLayoutGenerator.optimizeInlayerOrdering(relativeOrder, observer);
        
        // provisory absolute placement
        NetworkLayoutGenerator.initialAbsoluteLayout(relativeOrder);
        
        // optimize edge lengths, number of different edge lengths (absolute grid positioning within layers)
        
        // convert LNodes to GNodes (resp. to one of its subclasses) and also convert the grid coordinates to canvas coordinates
        GNode[] gnodes = NetworkLayoutGenerator.lnodesTOgnodes(relativeOrder);
        
        return new GBayesianNetwork(gnodes);
    }
    
    private static LNode[] copyNodeStructure(Node[] nodes) {
        // prepare raw LNode instances
        LNode[] lnodes = new LNode[nodes.length];
        for(int i = 0 ; i < nodes.length ; i++)
            lnodes[i] = new LNode(nodes[i]);
        // inter-node links
        for(int i = 0 ; i < nodes.length ; i++) {
            // determine parents
            Node[] iParents = nodes[i].getParentNodes();
            Node[] iChildren = nodes[i].getChildNodes();
            LNode[] iLParents = new LNode[iParents.length];
            LNode[] iLChildren = new LNode[iChildren.length];
            // pass through all lnodes and if it holds a parent or a child, then insert it
            for(LNode lnode : lnodes) {
                int parentIndex = Toolkit.indexOf(iParents, lnode.node);
                if(parentIndex != -1)
                    iLParents[parentIndex] = lnode;
                int childIndex = Toolkit.indexOf(iChildren, lnode.node);
                if(childIndex != -1)
                    iLChildren[childIndex] = lnode;
            }
            lnodes[i].parents = iLParents;
            lnodes[i].children = iLChildren;
        }
        return lnodes;
    }
    
    private static void assignNodesToLayers(LNode[] topsort) {
        // forward pass
        for(int i = 0 ; i < topsort.length ; i++) {
            topsort[i].layer = 0; // what layer is the lowest parent of node topsort[i] on
            for(LNode parent : topsort[i].parents) {
                topsort[i].layer = Math.max(topsort[i].layer, parent.layer + 1);
            }
        }
        // move nodes without parents as close to their children as possible
        for(int i = 0 ; i < topsort.length ; i++) {
            if(topsort[i].parents.length > 0 || topsort[i].children.length == 0)
                continue;
            int highestChildLayer = Integer.MAX_VALUE; // what layer is the highest child of node topsort[i] on
            for(LNode child : topsort[i].children) {
                highestChildLayer = Math.min(highestChildLayer, child.layer);
            }
            topsort[i].layer = highestChildLayer - 1;
        }
    }
    
    private static LNode[] insertDummyNodes(LNode[] topsort) {
        // we will need to insert dummy nodes => dynamic structure
        ArrayList<LNode> proper = new ArrayList<LNode>();
        proper.addAll(Arrays.asList(topsort));
        // for each connection inspect whether it doesn't pass through a layer
        for(int i = 0 ; i < proper.size() ; i++) {
            LNode lnode = proper.get(i);
            for(LNode child : lnode.children) {
                if(child.layer - lnode.layer > 1) {
                    LNode dummyNode = new LNode(null);
                    dummyNode.layer = lnode.layer + 1;
                    dummyNode.parents = new LNode[]{lnode};
                    dummyNode.children = new LNode[]{child};
                    // change the linkage
                    int childIndex = Toolkit.indexOf(lnode.children, child);
                    lnode.children[childIndex] = dummyNode;
                    int parentIndex = Toolkit.indexOf(child.parents, lnode);
                    child.parents[parentIndex] = dummyNode;
                    proper.add(i + 1, dummyNode);
                }
            }
        }
        // convert list back to array
        LNode[] properArray = new LNode[proper.size()];
        return proper.toArray(properArray);
    }
    
    private static LNode[][] preliminaryInlayerOrdering(LNode[] topsort) {
        // count layers
        int bottomLayer = 0;
        for(LNode n : topsort)
            bottomLayer = Math.max(bottomLayer, n.layer);
        int layerCount = bottomLayer + 1; // the topmost layer has index 0
        // determine set of nodes for each layer
        HashSet[] notPlacedNodes = new HashSet[layerCount];
        for(int i = 0 ; i < layerCount ; i++)
            notPlacedNodes[i] = new HashSet();
        for(LNode n : topsort)
            notPlacedNodes[n.layer].add(n);
        // generate sets of nodes on each layer
        LNode[][] inlayerOrder = new LNode[layerCount][];
        for(int i = 0 ; i < layerCount ; i++)
            inlayerOrder[i] = new LNode[notPlacedNodes[i].size()];
        // place all nodes to layers
        for(int layer = 0 ; layer < layerCount ; layer++) {
            // place any leftover nodes (without parents) of this layer
            for(Object notPlacedNodeObj : notPlacedNodes[layer]) {
                LNode notPlacedNode = (LNode)notPlacedNodeObj;
                NetworkLayoutGenerator.appendArray(inlayerOrder[layer], notPlacedNode);
            }
            // now all nodes on "layer" are placed, so place also their children in this order
            for(int i = 0 ; i < inlayerOrder[layer].length ; i++) {
                LNode placedNode = inlayerOrder[layer][i];
                placedNode.inlayerIndex = i; // now indices of nodes within layers are known
                for(LNode child : placedNode.children) {
                    if(notPlacedNodes[layer + 1].contains(child)) {
                        NetworkLayoutGenerator.appendArray(inlayerOrder[layer + 1], child);
                        notPlacedNodes[layer + 1].remove(child);
                    }
                }
            }
        }
        return inlayerOrder;
    }
    
    private static void optimizeInlayerOrdering(LNode[][] ordering, NetworkLayoutGeneratorObserver observer) {
        Random rand = new Random();
        int currentCrossings = NetworkLayoutGenerator.computeEdgeCrossingsToChildren(ordering), 
            bestCrossings = Integer.MAX_VALUE;
        
        int nodesTotal = 0;
        for(LNode[] layer : ordering)
            nodesTotal += layer.length;
        
        for(long i = 0 ; i < INLAYER_ITERATIONS ; i++) {
            // perturbe current ordering
            // generate two random indices on the same layer => swap / reinsert
            int nodeNO = rand.nextInt(nodesTotal), // layer with more nodes will get picked more often
                perturbeLayer,
                perturbePos1 = -1,
                perturbePos2 = -1;
            int tmp = 0; // to determine on which layer the node is
            for(perturbeLayer = 0 ; perturbeLayer < ordering.length ; perturbeLayer++) {
                if(nodeNO < tmp + ordering[perturbeLayer].length) {
                    // we have found the layer
                    perturbePos1 = rand.nextInt(ordering[perturbeLayer].length);
                    perturbePos2 = rand.nextInt(ordering[perturbeLayer].length);
                    break;
                }
                tmp += ordering[perturbeLayer].length;
            }
            if(perturbePos1 == perturbePos2)
                continue;
            
            InlayerPerturbationAction action;
            if(rand.nextBoolean())
                action = new SwapAction(perturbeLayer, perturbePos1, perturbePos2, ordering);
            else
                action = new ReinsertAction(perturbeLayer, perturbePos1, perturbePos2, ordering);
            action.apply();
            
            // evaluate score (count number of crossed edges) and possibly accept (similar to simulated annealing)
            int newCrossings = NetworkLayoutGenerator.computeEdgeCrossingsToChildren(ordering);
            double acceptWorseProb = (INLAYER_ITERATIONS - i) / (double)INLAYER_ITERATIONS;
            boolean accept = newCrossings < currentCrossings || rand.nextDouble() < acceptWorseProb;
            if(accept)
                currentCrossings = newCrossings;
            else
                action.undo();
            
            // consistency check
            for(LNode[] layer : ordering)
                for(int j = 0 ; j < layer.length ; j++)
                    if(layer[j].inlayerIndex != j)
                        throw new RuntimeException("invalid inlayer index after " + action.getClass().getName() + ", accept = " + accept);
            
            // if new best, record the current ordering
            if(currentCrossings < bestCrossings) {
                bestCrossings = currentCrossings;
                for(LNode[] layer : ordering)
                    for(LNode lnode : layer)
                        lnode.inlayerIndexBest = lnode.inlayerIndex;
            }
            observer.notifyLayoutGeneratorStatus(i, ITERATIONS_TOTAL, -currentCrossings, -bestCrossings);
            
            if(bestCrossings == 0) // we found structure without any crossings
                break;
        }
        
        // restore the overall best ordering seen
        Comparator<LNode> lnodeInlayerComparator = new Comparator<LNode>() {
            @Override
            public int compare(LNode o1, LNode o2) {
                return Integer.compare(o1.inlayerIndex, o2.inlayerIndex);
            }
        };
        for(LNode[] layer : ordering) {
            for(LNode lnode : layer)
                lnode.inlayerIndex = lnode.inlayerIndexBest;
            // ensure that nodes in each layer are sorted by the inlayerIndex
            Arrays.sort(layer, lnodeInlayerComparator);
        }
        System.out.printf("optimizeInlayerOrdering best number of crossings: %d\n", bestCrossings);
    }
    
    // perturbation actions
    static abstract class InlayerPerturbationAction {
        int layer, pos1, pos2;
        LNode[][] ordering;
        public InlayerPerturbationAction(int layer, int pos1, int pos2, LNode[][] ordering) {
            this.layer = layer;
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.ordering = ordering;
        }
        public abstract void apply();
        public abstract void undo();
    }
    
    static class SwapAction extends InlayerPerturbationAction {
        public SwapAction(int layer, int pos1, int pos2, LNode[][] ordering) {
            super(layer, pos1, pos2, ordering);
        }
        public void apply() {
            LNode lnode1 = ordering[layer][pos1],
                  lnode2 = ordering[layer][pos2];
            lnode1.inlayerIndex = pos2;
            ordering[layer][pos2] = lnode1;
            lnode2.inlayerIndex = pos1;
            ordering[layer][pos1] = lnode2;
        }
        public void undo() {
            apply(); // happens to be the same action
        }
    }
    
    static class ReinsertAction extends InlayerPerturbationAction {
        public ReinsertAction(int layer, int pos1, int pos2, LNode[][] ordering) {
            super(layer, pos1, pos2, ordering);
        }
        public void apply() {
            this.move(layer, pos1, pos2);
        }
        public void undo() {
            this.move(layer, pos2, pos1);
        }
        private void move(int layer, int src, int dst) {
            LNode lnode = ordering[layer][src]; 
            if(src > dst) {
                for(int i = src ; i > dst ; i--) {
                    ordering[layer][i] = ordering[layer][i - 1];
                    ordering[layer][i].inlayerIndex++;
                }
            }
            else {
                for(int i = src ; i < dst ; i++) {
                    ordering[layer][i] = ordering[layer][i + 1];
                    ordering[layer][i].inlayerIndex--;
                }
            }
            ordering[layer][dst] = lnode;
            lnode.inlayerIndex = dst;
        }
    }
    
    private static void appendArray(LNode[] array, LNode lnode) {
        for(int i = 0 ; i < array.length ; i++) {
            if(array[i] == null) {
                array[i] = lnode;
                return;
            }
        }
        throw new InternalCheckException("Array already full!"); // should never come to this
    }
    
    private static void initialAbsoluteLayout(LNode[][] relativeOrder) {
        for(int layer = 0 ; layer < relativeOrder.length ; layer++) {
            for(int i = 0 ; i < relativeOrder[layer].length ; i++) {
                relativeOrder[layer][i].gridY = layer;
                relativeOrder[layer][i].gridX = 2 * i; // one space always in between
            }
        }
    }
    
    private static GNode[] lnodesTOgnodes(LNode[][] relativeOrder) {
        int PADDING_X = GNodeVariable.RADIUS,
            PADDING_Y = GNodeVariable.RADIUS;
        int dx = GNodeVariable.RADIUS * 2,
            dy = GNodeVariable.RADIUS * 4;
        // place LNodes to a linear structure => corresponding GNodes will be on the same indices
        ArrayList<LNode> lnodes = new ArrayList<LNode>();
        for(LNode[] layer : relativeOrder)
            lnodes.addAll(Arrays.asList(layer));
        // create GNode instances and place them (absolutely on canvas)
        ArrayList<GNode> gnodes = new ArrayList<GNode>();
        for(LNode lnode : lnodes) {
            GNode gnode;
            // create instance of correct subclass of GNode
            if(lnode.node != null)
                gnode = new GNodeVariable(lnode.node);
            else
                gnode = new GNodeDummy();
            // absolute placement of GNode
            int canvasX = PADDING_X + dx / 2 + lnode.gridX * dx,
                canvasY = PADDING_Y + dy / 2 + lnode.gridY * dy;
            gnode.setLocationByCenter(canvasX, canvasY);
            gnodes.add(gnode);
        }
        // transform LNode links structure (parents, children) to GNode links
        for(int i = 0 ; i < gnodes.size() ; i++) {
            GNode gnode = gnodes.get(i);
            LNode lnode = lnodes.get(i);
            gnode.children = new GNode[lnode.children.length];
            for(int j = 0 ; j < gnode.children.length ; j++) {
                LNode lchild = lnode.children[j];
                int childIndex = lnodes.indexOf(lchild);
                gnode.children[j] = gnodes.get(childIndex);
            }
            gnode.parents = new GNode[lnode.parents.length];
            for(int j = 0 ; j < gnode.parents.length ; j++) {
                LNode lparent = lnode.parents[j];
                int parentIndex = lnodes.indexOf(lparent);
                gnode.parents[j] = gnodes.get(parentIndex);
            }
        }
        GNode[] gnodesArray = new GNode[gnodes.size()];
        return gnodes.toArray(gnodesArray);
    }
    
    private static int computeEdgeCrossingsToChildren(LNode[][] ordering) {
        // evaluate score (count number of crossed edges)
        int crossings = 0;
        for(int layer = 0 ; layer < ordering.length ; layer++) {
            for(int j = 0 ; j < ordering[layer].length - 1 ; j++) {
                LNode jNode = ordering[layer][j];
                for(int k = j + 1 ; k < ordering[layer].length ; k++) {
                    LNode kNode = ordering[layer][k];
                    crossings += NetworkLayoutGenerator.computeEdgeCrossingsToChildren(jNode, kNode);
                }
            }
        }
        return crossings;
    }
    
    /** Use a modified merge-sort algorithm with counting inversions. */
    private static int computeEdgeCrossingsToChildren(LNode left, LNode right) {
        int crossings = 0;
        for(LNode leftChild : left.children) {
            for(LNode rightChild : right.children) {
                if(leftChild.inlayerIndex > rightChild.inlayerIndex)
                    crossings++;
            }
        }
        return crossings;
    }
    
    
    /** Structure only for computations of the layout (only within this file). */
    static class LNode {
        Node node; // null for dummy nodes
        int layer;
        int inlayerIndex;
        int gridX, gridY;
        LNode[] parents, children;
        // to keep "best-so-far" values during optimization
        int inlayerIndexBest;
        int gridXBest, gridYBest;

        public LNode(Node node) {
            this.node = node;
            // for all other attributes set invalid values
            this.layer = -1;
            this.inlayerIndex = -1;
            this.gridX = this.gridY = -1;
            this.parents = this.children = null;
            this.inlayerIndexBest = this.gridXBest = this.gridYBest = -1;
        }
    }
}
