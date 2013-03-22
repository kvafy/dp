// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/16

package bna.view;

import bna.bnlib.BayesianNetwork;
import bna.bnlib.misc.Toolkit;
import java.awt.*;
import javax.swing.JLabel;


/**
 * Graphical view of a Bayesian network in a JPanel.
 */
public class NetworkViewPanel extends javax.swing.JPanel {
    private GBayesianNetwork gbn;
    // when not null, generating layout is in progress
    private RepaintingNetworkLayoutGeneratorObserver layoutGeneratorObserver = null; 
    
    public NetworkViewPanel() {
        this.gbn = null;
    }
    
    public boolean hasNetwork() {
        return this.gbn != null;
    }
    
    public void setNetwork(BayesianNetwork bn) {
        this.setNetwork((GBayesianNetwork)null);
        MainWindow.getInstance().enableComponentsByState();
        if(bn == null)
            return;
        
        // generate the layout in another thread and then place the final product
        final BayesianNetwork bnFinal = bn;
        final RepaintingNetworkLayoutGeneratorObserver observer = new RepaintingNetworkLayoutGeneratorObserver();
        Thread layoutThread = new Thread() {
            @Override
            public void run() {
                try {
                    observer.installObserver();
                    GBayesianNetwork gbn = NetworkLayoutGenerator.getLayout(bnFinal, observer);
                    for(long i = 0 ; i < 10 * 1000 * 1000 * 1000 ; i++) ;
                    observer.removeObserver();
                    setNetwork(gbn);
                }
                finally {
                    observer.removeObserver(); // in case of an error
                }
            }
        };
        layoutThread.setDaemon(true);
        java.awt.EventQueue.invokeLater(layoutThread);
    }
    
    public void setNetwork(GBayesianNetwork bnNew) {
        GBayesianNetwork bnOld = this.gbn;
        if(bnNew != bnOld) {
            this.removeComponentsOfNetwork(bnOld);
            this.gbn = bnNew;
            this.addComponentsOfNetwork(gbn);
            // TODO set new size of the canvas to accomodate for new network
            this.repaint();
            MainWindow.getInstance().enableComponentsByState();
        }
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
        
        // network components (nodes, edges, CPDs)
        if(this.gbn != null) {
            g2D.setColor(Color.DARK_GRAY);
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
            for(GNode parent : this.gbn.getGNodes()) {
                // edges need to be painted separately (edge is not a JComponent)
                for(GNode child : parent.children)
                    this.paintEdge(g2D, parent, child);
            }
            
            // nodes get painted as standalone components
            g2D.setColor(Color.BLACK);
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
            this.paintComponents(gPlain); // aka nodes
            
            // TODO CPDs
        }
        else if(this.layoutGeneratorObserver != null) {
            // components informing about layout generation process
            g2D.setColor(Color.BLACK);
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
            this.paintComponent(gPlain);
            System.out.printf("painted %d components (JLabels)\n", this.getComponents().length);
        }
        else
            System.out.println("not painted any component");
        
        // frame of the whole area
        g2D.setColor(Color.DARK_GRAY);
        g2D.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
    }
    
    private void paintEdge(java.awt.Graphics2D g2D, GNode parent, GNode child) {
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
    
    
    class RepaintingNetworkLayoutGeneratorObserver implements NetworkLayoutGeneratorObserver {
            long iteration, maxIterations;
            double score, scoreBest;
            JLabel labelIterations = new JLabel(),
                   labelScoreCurrent = new JLabel(),
                   labelScoreBest = new JLabel();
            
            @Override
            public void notifyLayoutGeneratorStatus(long iteration, long maxIterations, double score, double scoreBest) {
                labelIterations.setText(String.format("Iteration %d/%d", iteration, maxIterations));
                labelScoreCurrent.setText(String.format("Current score: %.3f", score));
                labelScoreBest.setText(String.format("Best score: %.3f", scoreBest));
                
                FontMetrics fontMetrics = getFontMetrics(getFont());
                int lineHeight = fontMetrics.getHeight(),
                    lineYMargin = Math.max(10, (int)(lineHeight * 0.25));
                labelIterations.setLocation(30, 30);
                labelScoreCurrent.setLocation(30, 30 + 1 * (lineHeight + lineYMargin));
                labelScoreBest.setLocation(30,    30 + 2 * (lineHeight + lineYMargin));
                repaint();
            }
            
            void installObserver() {
                layoutGeneratorObserver = this;
                add(labelIterations);
                add(labelScoreCurrent);
                add(labelScoreBest);
                validate();
                this.notifyLayoutGeneratorStatus(0, 0, 0, 0);
            }
            
            void removeObserver() {
                layoutGeneratorObserver = null;
                remove(labelIterations);
                remove(labelScoreBest);
                remove(labelScoreBest);
                validate();
            }
        }
}
