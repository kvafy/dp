// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/17

package bna.view;

import bna.bnlib.BayesianNetwork;
import bna.bnlib.Factor;
import bna.bnlib.Node;
import bna.bnlib.Variable;
import bna.bnlib.learning.Dataset;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


/**
 * Graphical wrapper of the BayesianNetwork class.
 * Nodes (dummy as well as variable nodes) are represented as a JComponent
 * that gets inserted into the NetworkViewPanel. The GNode class also reponds
 * to dragging by mouse. Edges must be painted by hand.
 */
public class GBayesianNetwork {
    private final BayesianNetwork bn;
    private GNode[] gnodes;
    
    
    public GBayesianNetwork(BayesianNetwork bn, GNode[] gnodes) {
        this.bn = bn;
        this.gnodes = Arrays.copyOf(gnodes, gnodes.length);
        for(GNode gnode : this.gnodes)
            gnode.ownerBN = this;
        this.notifyCPDsChange();
    }
    
    public BayesianNetwork getNetwork() {
        return this.bn;
    }
    
    GNode[] getGNodes() {
        return Arrays.copyOf(this.gnodes, this.gnodes.length);
    }
    
    /** Remove a dummy node by connecting its only parent and its only child directly. */
    void removeDummyNode(GNode node) {
        if(node == null || !(node instanceof GNodeDummy) || !bna.bnlib.misc.Toolkit.arrayContains(this.gnodes, node))
            return;
        GNodeDummy dnode = (GNodeDummy)node;
        // break the connections (make transitive)
        for(GNode dnodeParent : dnode.parents)
            dnodeParent.removeDummyChild(dnode);
        for(GNode dnodeChild : dnode.children)
            dnodeChild.removeDummyParent(dnode);
        // remove the node from this.gnodes array
        GNode[] gnodesNew = new GNode[this.gnodes.length - 1];
        int j = 0;
        for(int i = 0 ; i < this.gnodes.length ; i++) {
            if(this.gnodes[i] == dnode)
                continue;
            gnodesNew[j++] = this.gnodes[i];
        }
        
        this.gnodes = gnodesNew;
        java.awt.Container parentComponent = dnode.getParent();
        if(parentComponent != null) {
            parentComponent.remove(dnode);
            parentComponent.repaint();
        }
    }
    
    void invalidateEdgeWeights() {
        for(GNode gnode : this.gnodes)
            gnode.edgeWeightsToParents = null;
    }
    
    void recomputeEdgeWeights(Dataset dataset) {
        if(dataset == null || !dataset.containsVariables(this.bn.getVariables())) {
            this.invalidateEdgeWeights();
            return;
        }
        double edgeWeightMin = Double.MAX_VALUE,
               edgeWeightMax = -Double.MAX_VALUE;
        // compute for each variable node mutual information with each of his parents
        for(GNode gnode : this.gnodes) {
            if(!(gnode instanceof GNodeVariable))
                continue;
            gnode.edgeWeightsToParents = new double[gnode.parents.length];
            Node node = ((GNodeVariable)gnode).node;
            for(int i = 0 ; i < gnode.parents.length ; i++) {
                GNode gparent = gnode.parents[i];
                while(gparent instanceof GNodeDummy) // follow links to the GNodeVariable parent
                    gparent = gparent.parents[0];
                Node parent = ((GNodeVariable)gparent).node;
                double mutualInformation = dataset.mutualInformation(new Variable[]{node.getVariable()},
                                                                     new Variable[]{parent.getVariable()});
                gnode.edgeWeightsToParents[i] = mutualInformation;
                edgeWeightMin = Math.min(edgeWeightMin, mutualInformation);
                edgeWeightMax = Math.max(edgeWeightMax, mutualInformation);
            }
        }
        // transform to relative edge weights [0,1] and propagate the edge weights to dummy nodes upward
        for(GNode gnode : this.gnodes) {
            if(!(gnode instanceof GNodeVariable))
                continue;
            for(int i = 0 ; i < gnode.parents.length ; i++) {
                gnode.edgeWeightsToParents[i] = (gnode.edgeWeightsToParents[i] - edgeWeightMin)
                                              / (edgeWeightMax - edgeWeightMin);
                GNode gparent = gnode.parents[i];
                while(gparent instanceof GNodeDummy) {
                    // follow links to the GNodeVariable parent
                    gparent.edgeWeightsToParents = new double[] {gnode.edgeWeightsToParents[i]};
                    gparent = gparent.parents[0];
                }
            }
        }
    }
    
    int getVariableNodesCount() {
        int count = 0;
        for(GNode gnode : this.gnodes)
            if(gnode instanceof GNodeVariable)
                count++;
        return count;
    }
    
    int getDummyNodesCount() {
        int count = 0;
        for(GNode gnode : this.gnodes)
            if(gnode instanceof GNodeDummy)
                count++;
        return count;
    }
    
    public Point getTopLeft() {
        int xMin = Integer.MAX_VALUE,
            yMin = Integer.MAX_VALUE;
        for(GNode gnode : this.gnodes) {
            Point gnodeLocation = gnode.getLocation();
            xMin = Math.min(xMin, gnodeLocation.x);
            yMin = Math.min(yMin, gnodeLocation.y);
        }
        return new Point(xMin, yMin);
    }
    
    public Point getBottomRight() {
        int xMax = Integer.MIN_VALUE,
            yMax = Integer.MIN_VALUE;
        for(GNode gnode : this.gnodes) {
            Point gnodeLocation = gnode.getLocation();
            xMax = Math.max(xMax, gnodeLocation.x + gnode.getWidth());
            yMax = Math.max(yMax, gnodeLocation.y + gnode.getHeight());
        }
        return new Point(xMax, yMax);
    }
    
    public void notifyCPDsChange() {
        for(GNode gnode : this.gnodes) {
            if(!(gnode instanceof GNodeVariable))
                continue;
            String cpdAsText;
            GNodeVariable gnodev = (GNodeVariable)gnode;
            Factor cpd = gnodev.node.getFactor();
            if(cpd == null)
                cpdAsText = null;
            else
                cpdAsText = "<html><pre><font face=\"monospace\">"
                          + this.quoteHTMLChars(cpd.toString())
                          + "</font></pre></html>";
            gnodev.setToolTipText(cpdAsText);
        }
    }
    
    private String quoteHTMLChars(String toHtml) {
       return toHtml.replace("&", "&quot;")
                    .replace(">", "&gt;")
                    .replace("<", "&lt;");
    }
}



/** General node in graphical representation of a BN (variable node or dummy node). */
abstract class GNode extends DraggableComponent {
    GBayesianNetwork ownerBN;
    GNode[] parents, children;
    double[] edgeWeightsToParents = null; // null means the are no edge weights (ie. default)
    
    public GNode() {
        this.setRepaintParent(true); // because of graph edges we need to repaint everything
    }
    
    public Point getCenterOnCanvas() {
        Point topleftOnCanvas = this.getLocation();
        return new Point(topleftOnCanvas.x + this.getWidth() / 2,
                         topleftOnCanvas.y + this.getHeight() / 2);
    }
    
    /** If we where to go from the center of this component to its edge in specified direction, at which point do we hit the border? */
    public abstract Point getBorderPointFromCenter(double angle);
    
    /** Place the node so that its center is at the specified location relative to parent. */
    public void setLocationByCenter(int cx, int cy) {
        this.setLocation(cx - this.getWidth() / 2, cy - this.getHeight() / 2);
    }
    
    public void removeDummyParent(GNode parentToRemove) {
        GNode parentTransitive = parentToRemove.parents[0]; // exactly one parent
        for(int i = 0 ; i < this.parents.length ; i++) {
            if(this.parents[i].equals(parentToRemove)) {
                this.parents[i] = parentTransitive;
                return;
            }
        }
    }
    
    public void removeDummyChild(GNode childToRemove) {
        GNode childTransitive = childToRemove.children[0]; // exactly one child
        for(int i = 0 ; i < this.children.length ; i++) {
            if(this.children[i].equals(childToRemove)) {
                this.children[i] = childTransitive;
                return;
            }
        }
    }
}


/** Node representing a concrete variable as a labeled circle. */
class GNodeVariable extends GNode {
    public static final int RADIUS = 30;
    Node node;
    
    
    public GNodeVariable(Node node) {
        this.node = node;
        this.setSize(2 * GNodeVariable.RADIUS, 2 * GNodeVariable.RADIUS);
        
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3)
                    onRightMouse(e);
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
                    onDoubleLeftMouse(e);
            }
        });
    }
    
    @Override
    public Point getBorderPointFromCenter(double angle) {
        double dx = Math.cos(angle),
               dy = Math.sin(angle);
        Point center = this.getCenterOnCanvas();
        int edgeX = (int)Math.round(center.x + dx * RADIUS),
            edgeY = (int)Math.round(center.y + dy * RADIUS);
        return new Point(edgeX, edgeY);
    }
    
    @Override
    /** Redefine hook of the DragableComponent to fit circular area of this component. */
    protected boolean mouseInsideRealArea(Point p) {
        int r = GNodeVariable.RADIUS,
            cx = GNodeVariable.RADIUS,
            cy = GNodeVariable.RADIUS;
        return (r * r) >= (cx - p.x) * (cx - p.x) + (cy - p.y) * (cy - p.y);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        // edged circle representing the variable
        Graphics2D g2D = (Graphics2D)g;
        g2D.setPaint(
                new GradientPaint(
                    0f, 0f, new Color(0x20cc70),
                    0f, this.getHeight() / 2f, new Color(0x109920),
                    true)
        );
        g.fillOval(0, 0, this.getWidth() - 1, this.getHeight() - 1);
        g.setColor(Color.BLACK);
        g.drawOval(0, 0, this.getWidth() - 1, this.getHeight() - 1);
        // name of the variable in the center of oval
        // (inspired by http://docs.oracle.com/javase/tutorial/2d/text/measuringtext.html)
        String text = node.getVariable().getName();
        FontMetrics metrics = g.getFontMetrics();
        int textHeight = metrics.getHeight() + metrics.getDescent();
        int textWidth = metrics.stringWidth(text);
        int textBottomCorner = (this.getHeight() + textHeight) / 2 - metrics.getDescent();
        int textLeftCorner = (this.getWidth() - textWidth) / 2;
        textLeftCorner = Math.max(2, textLeftCorner); // preserve at leas the beginning of the variable name
        g.drawString(text, textLeftCorner, textBottomCorner);
    }
    
    private void onRightMouse(MouseEvent e) {
        // create popup menu
        JMenuItem menuShowDetails = new JMenuItem("Show details");
        menuShowDetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { showDetailsDialog(); }
        });
        JPopupMenu menu = new JPopupMenu("Options of variable node");
        menu.add(menuShowDetails);
        // show menu
        menu.show(this, e.getPoint().x, e.getPoint().y);
    }
    
    private void onDoubleLeftMouse(MouseEvent e) {
        showDetailsDialog();
    }
    
    private void showDetailsDialog() {
        DialogNodeInfo dialog = new DialogNodeInfo(MainWindow.getInstance(), true, this.node);
        dialog.setVisible(true);
    }
}


/** Dummy node to allow for twists of edges through multiple layers. */
class GNodeDummy extends GNode {
    public static final int SQUARE_SIZE = 8; // the node is a square of this size (for clicking, dragging etc.)
    
    public GNodeDummy() {
        this.setSize(GNodeDummy.SQUARE_SIZE, GNodeDummy.SQUARE_SIZE);
        
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3)
                    onRightMouse(e);
            }
        });
    }
    
    @Override
    public Point getBorderPointFromCenter(double angle) {
        return this.getCenterOnCanvas();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        // do nothing (dummy node is transparent)
        g.setColor(Color.RED);
        g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
    }
    
    private void onRightMouse(MouseEvent e) {
        final GNode thisNode = this;
        // create popup menu
        JMenuItem menuRemoveNode = new JMenuItem("Remove node");
        menuRemoveNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { ownerBN.removeDummyNode(thisNode); }
        });
        JPopupMenu menu = new JPopupMenu("Options of dummy node");
        menu.add(menuRemoveNode);
        // show menu
        menu.show(this, e.getPoint().x, e.getPoint().y);
    }
}
