// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/16

package bna.view;

import bna.bnlib.BayesianNetwork;
import bna.bnlib.misc.Toolkit;
import java.awt.*;
import javax.swing.JLabel;
import javax.swing.SwingWorker;


/**
 * Graphical view of a Bayesian network in a JPanel.
 */
public class NetworkViewPanel extends javax.swing.JPanel {
    private GBayesianNetwork gbn;
    // when not null, generating layout is in progress
    private NetworkLayoutGeneratorObserver layoutGeneratorObserver = null; 
    
    public NetworkViewPanel() {
        this.gbn = null;
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
    
    public void setNetwork(BayesianNetwork bn) {
        this.setNetwork((GBayesianNetwork)null);
        MainWindow.getInstance().notifyActiveNetworkChange();
        if(bn == null)
            return;
        
        // generate the layout in another thread and then place the final product
        // worker thread will install labels for status updates and then remove them
        final BayesianNetwork bnFinal = bn;
        SwingWorker<GBayesianNetwork, StatusNotification> workerThread = new SwingWorker<GBayesianNetwork, StatusNotification>() {
            private JLabel labelIterations = new JLabel(),
                           labelScoreCurrent = new JLabel(),
                           labelScoreBest = new JLabel();
            
            void installStatusLabels(NetworkLayoutGeneratorObserver observerInst) {
                layoutGeneratorObserver = observerInst;
                add(labelIterations);
                add(labelScoreCurrent);
                add(labelScoreBest);
                FontMetrics fontMetrics = getFontMetrics(getFont());
                int lineHeight = fontMetrics.getHeight(),
                    lineYMargin = Math.max(10, (int)(lineHeight * 0.25));
                labelIterations.setLocation(30, 30);
                labelScoreCurrent.setLocation(30, 30 + 1 * (lineHeight + lineYMargin));
                labelScoreBest.setLocation(30,    30 + 2 * (lineHeight + lineYMargin));
                validate();
            }
            
            private void removeStatusLabels() {
                layoutGeneratorObserver = null;
                remove(labelIterations);
                remove(labelScoreBest);
                remove(labelScoreBest);
                validate();
            }
            
            @Override
            protected GBayesianNetwork doInBackground() throws Exception {
                final NetworkLayoutGeneratorObserver observer = new NetworkLayoutGeneratorObserver() {
                    @Override
                    public void notifyLayoutGeneratorStatus(long iteration, long maxIterations, double score, double scoreBest) {
                        publish(new StatusNotification(iteration, maxIterations, score, scoreBest));
                    }
                };
                this.installStatusLabels(observer);
                return NetworkLayoutGenerator.getLayout(bnFinal, observer);
            }
            
            @Override
            protected void done() {
                try {
                    removeStatusLabels();
                    setNetwork(get());
                }
                catch(Exception ignore) {
                    ignore.printStackTrace();
                }
            }
            
            @Override
            protected void process(java.util.List<StatusNotification> notifications) {
                StatusNotification notif = notifications.get(notifications.size() - 1);
                labelIterations.setText(String.format("Iteration %d/%d", notif.currentIteration, notif.maxIterations));
                labelScoreCurrent.setText(String.format("Current score: %.2f", notif.currentSore));
                labelScoreBest.setText(String.format("Best score: %.2f", notif.bestScore));
            }
        };
        workerThread.execute();
    }
    
    /** Carries currently observed status of layout generator. */
    class StatusNotification {
        long currentIteration, maxIterations;
        double currentSore, bestScore;
        public StatusNotification(long currentIter, long maxIters, double curScore, double bestScore) {
            this.currentIteration = currentIter;
            this.maxIterations = maxIters;
            this.currentSore = curScore;
            this.bestScore = bestScore;
        }
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
            this.repaint();
            MainWindow.getInstance().notifyActiveNetworkChange();
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
        }
        
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
}
