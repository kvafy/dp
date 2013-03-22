// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/17

package bna.view;

import bna.bnlib.Node;
import java.awt.*;
import java.util.Arrays;


/**
 * Graphical wrapper of the BayesianNetwork class.
 */
public class GBayesianNetwork {
    private final GNode[] gnodes;
    
    // TODO kompletne jinak (uz NetworkLayoutGenerator vrati GBayesianNetwork)
    public GBayesianNetwork(GNode[] gnodes) {
        this.gnodes = Arrays.copyOf(gnodes, gnodes.length);
    }
    
    public GNode[] getGNodes() {
        return Arrays.copyOf(this.gnodes, this.gnodes.length);
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
}



/** General node in graphical representation of a BN (variable node or dummy node). */
abstract class GNode extends DraggableComponent {
    GNode[] parents, children;
    
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
}


/** Node representing a concrete variable as a labeled circle. */
class GNodeVariable extends GNode {
    public static final int RADIUS = 30;
    Node node;
    
    
    public GNodeVariable(Node node) {
        this.node = node;
        this.setSize(2 * GNodeVariable.RADIUS, 2 * GNodeVariable.RADIUS);
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
}


/** Dummy node to allow for twists of edges through multiple layers. */
class GNodeDummy extends GNode {
    public static final int SQUARE_SIZE = 8; // the node is a square of this size (for clicking, dragging etc.)
    
    public GNodeDummy() {
        this.setSize(GNodeDummy.SQUARE_SIZE, GNodeDummy.SQUARE_SIZE);
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
}
