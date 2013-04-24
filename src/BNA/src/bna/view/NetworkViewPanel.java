// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/16

package bna.view;

import bna.bnlib.BayesianNetwork;
import bna.bnlib.learning.Dataset;
import bna.bnlib.misc.LRUCache;
import bna.bnlib.misc.Toolkit;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.JColorChooser;


/**
 * Graphical view of a Bayesian network in a JPanel.
 */
public class NetworkViewPanel extends javax.swing.JPanel implements ActiveDatasetObserver, ActiveNetworkObserver {
    private GBayesianNetwork gbn;
    private Dataset dataset;
    private boolean showEdgeWeightsFlag = false;
    private final LRUCache<BayesianNetwork, GBayesianNetwork> layoutCache = new LRUCache<BayesianNetwork, GBayesianNetwork>(10);
    private ArrayList<ActiveNetworkObserver> observers = new ArrayList<ActiveNetworkObserver>();
    
    
    public NetworkViewPanel() {
        this.gbn = null;
        this.dataset = null;
        this.addObserver(this);
    }
    
    public boolean hasNetwork() {
        return this.gbn != null;
    }
    
    public BayesianNetwork getNetwork() {
        if(this.gbn == null)
            return null;
        else
            return this.gbn.getNetwork();
    }

    class LayoutGeneratingThread extends Thread {
        private BayesianNetwork bn;
        
        public LayoutGeneratingThread(BayesianNetwork bn) {
            this.bn = bn;
        }
        
        @Override
        public void run() {
            GBayesianNetwork gbn = layoutCache.get(this.bn);
            if(gbn == null) {
                NetworkLayoutGeneratorObserver observer = null;
                gbn = NetworkLayoutGenerator.getLayout(this.bn, observer);
                layoutCache.put(this.bn, gbn);
            }
            setNetwork(gbn);
        }
    }
    
    public void setNetwork(BayesianNetwork bn) {
        this.setNetwork((GBayesianNetwork)null);
        this.notifyObservers();
        if(bn == null)
            return;
        
        // generate the layout in another thread which publishes the final product
        LayoutGeneratingThread workerThread = new LayoutGeneratingThread(bn);
        workerThread.start();
    }
    
    public void setNetwork(GBayesianNetwork bnNew) {
        GBayesianNetwork bnOld = this.gbn;
        if(bnNew != bnOld) {
            // network nodes are stand-alone JComponents => remove old ones and add new
            this.removeComponentsOfNetwork(bnOld);
            this.gbn = bnNew;
            if(bnNew != null) {
                Point gbnBottomRight = bnNew.getBottomRight();
                this.setPreferredSize(new Dimension(gbnBottomRight.x + NetworkLayoutGenerator.NETWORK_ABSOLUTE_MARGIN_X,
                                                    gbnBottomRight.y + NetworkLayoutGenerator.NETWORK_ABSOLUTE_MARGIN_Y));
                this.addComponentsOfNetwork(gbn);
            }
            else
                this.setPreferredSize(null); // automatic to fit in parent component
            this.notifyObservers();
            this.repaint();
        }
    }
    
     @Override
    public void notifyNewActiveDataset(Dataset d) {
        this.dataset = d;
        this.reconsiderEdgeWeights();
    }
     
     @Override
    public void notifyNewActiveNetwork(GBayesianNetwork gbn) {
        this.reconsiderEdgeWeights();
    }
    
    public void setShowEdgeWeights(boolean flag) {
        this.showEdgeWeightsFlag = flag;
        this.reconsiderEdgeWeights();
    }
    
    private void reconsiderEdgeWeights() {
        boolean canShowEdgeWeights = this.showEdgeWeightsFlag
                                  && this.dataset != null
                                  && this.gbn != null
                                  && this.dataset.containsVariables(this.gbn.getNetwork().getVariables());
        if(!canShowEdgeWeights)
            this.invalidateEdgeWeights();
        else
            this.computeEdgeWeights();
        this.repaint();
    }
    
    private void invalidateEdgeWeights() {
        if(this.gbn != null)
            this.gbn.invalidateEdgeWeights();
    }
    
    private void computeEdgeWeights() {
        if(this.gbn != null)
            this.gbn.recomputeEdgeWeights(this.dataset);
    }
    
    private void addComponentsOfNetwork(GBayesianNetwork gbn) {
        if(gbn != null) {
            // add JComponents of the network
            for(GNode gnode : gbn.getGNodes())
                this.add(gnode);
            this.validate();
        }
    }
    
    private void removeComponentsOfNetwork(GBayesianNetwork gbn) {
        // remove all JComponents of the network
        this.removeAll();
        this.validate();
    }
    
    @Override
    public void paint(java.awt.Graphics gPlain) {
        Graphics2D g2D = (Graphics2D)gPlain;
        // nice background
        g2D.setPaint(
                new GradientPaint(
                    0f, 0f, new Color(0xeeeeee),
                    this.getWidth(), this.getHeight(), new Color(0xbbbbbb))
        );
        g2D.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        // network components (nodes, edges)
        // (CPDs are painted as tooltips of nodes automatically)
        if(this.gbn != null) {
            g2D.setColor(Color.DARK_GRAY);
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
            for(GNode child : this.gbn.getGNodes()) {
                // edges need to be painted separately (edge is not a JComponent)
                for(int i = 0 ; i < child.parents.length ; i++) {
                    GNode parent = child.parents[i];
                    Double edgeWeight = child.edgeWeightsToParents == null ? null : child.edgeWeightsToParents[i];
                    this.paintEdge(g2D, parent, child, edgeWeight);
                }
            }
            
            // nodes get painted as stand-alone components
            g2D.setColor(Color.BLACK);
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
            this.paintComponents(gPlain); // aka nodes
        }
        
        // frame of the whole area
        g2D.setColor(Color.DARK_GRAY);
        g2D.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
    }
    
    private void paintEdge(java.awt.Graphics2D g2D, GNode parent, GNode child, Double edgeWeight) {
        if(edgeWeight != null)
            g2D.setColor(this.getColorOnColdToHotScale(edgeWeight));
        Point pointStart = parent.getCenterOnCanvas(),
              pointEnd = child.getCenterOnCanvas();
        // line not from center to center but from border to border
        double angle = Toolkit.angleOfVector(pointStart, pointEnd);
        pointStart = parent.getBorderPointFromCenter(angle);
        pointEnd = child.getBorderPointFromCenter(angle - Math.PI);
        g2D.drawLine(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y); // TODO antialiasing
        
        // possibly pointy arrow at the end node (little triangle resembling)
        double ARROW_ROTATION_ANGLE = Math.PI / 9;
        int ARROW_LENGTH = 13; // TODO by size of system font?
        if(child instanceof GNodeVariable) {
            // the pointy triangle is defined by three vertices
            int arrowBeginX = pointEnd.x,
                arrowBeginY = pointEnd.y;
            double angleA = (angle - Math.PI) + ARROW_ROTATION_ANGLE;
            int arrowEndAX = (int)Math.round(arrowBeginX + Math.cos(angleA) * ARROW_LENGTH),
                arrowEndAY = (int)Math.round(arrowBeginY + Math.sin(angleA) * ARROW_LENGTH);
            double angleB = (angle - Math.PI) - ARROW_ROTATION_ANGLE;
            int arrowEndBX = (int)Math.round(arrowBeginX + Math.cos(angleB) * ARROW_LENGTH),
                arrowEndBY = (int)Math.round(arrowBeginY + Math.sin(angleB) * ARROW_LENGTH);
            g2D.fillPolygon(new int[]{arrowBeginX, arrowEndAX, arrowEndBX},
                            new int[]{arrowBeginY, arrowEndAY, arrowEndBY},
                            3);
        }
    }
    
    /** For a weight from the range [0,1] returns a color on the cold-to-hot color scale. */
    private Color getColorOnColdToHotScale(double weight) {
        float SQRT2 = (float)Math.sqrt(2);
        float r, g, b;
        // hot-to-cold ramp (http://paulbourke.net/texture_colour/colourspace/)
        float absPos = ((float)weight) * (1f + SQRT2 + 1f);
        if(absPos <= 1f) {
            // rgb(0,0,1) -> rgb(0,1,1)
            float relPos = absPos;
            r = 0;
            g = relPos;
            b = 1;
        }
        else if(absPos <= 1f + SQRT2) {
            // rgb(0,1,1) -> rgb(1,1,0)
            float relPos = (absPos - 1f) / SQRT2;
            r = relPos;
            g = 1;
            b = 1 - relPos;
        }
        else {
            // rgb(1,1,0) -> rgb(1,0,0)
            float relPos = (absPos - 1f - SQRT2) / 1f;
            r = 1;
            g = 1 - relPos;
            b = 0;
        }
        // compensate for float errors
        r = Math.max(Math.min(1.0f, r), 0f);
        g = Math.max(Math.min(1.0f, g), 0f);
        b = Math.max(Math.min(1.0f, b), 0f);
        return new Color(r, g, b);
    }
    
    public void addObserver(ActiveNetworkObserver observer) {
        if(!this.observers.contains(observer)) {
            this.observers.add(observer);
            observer.notifyNewActiveNetwork(this.gbn);
        }
    }
    
    public void removeObserver(ActiveNetworkObserver observer) {
        this.observers.remove(observer);
    }
    
    private void notifyObservers() {
        for(ActiveNetworkObserver observer : this.observers)
            observer.notifyNewActiveNetwork(this.gbn);
    }
}
