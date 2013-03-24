// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/17

// inspired by http://www.codeproject.com/Articles/116088/Draggable-Components-in-Java-Swing

package bna.view;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * A generic JComponent that supports dragging within its parent component.
 */
public class DraggableComponent extends javax.swing.JComponent {
    private Point anchor;
    // behaviour settings
    private boolean repaintParent = false; // on drag repaint just this component or the whole parent container?
    private boolean draggable = true;

    
    public DraggableComponent() {
        this.setClickListeners();
        this.setDragListeners();
    }
    
    public void setRepaintParent(boolean flag) {
        this.repaintParent = flag;
    }
    
    public void setDraggable(boolean flag) {
        this.draggable = flag;
    }
    
    private void setClickListeners() {
        final DraggableComponent component = this;
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!mouseInsideRealArea(e.getPoint()))
                    return;
                getParent().setComponentZOrder(component, 0);
                repaint();
            }
        });
    }
    
    private void setDragListeners() {
        final DraggableComponent component = this;
        
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                anchor = e.getPoint();
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if(!mouseInsideRealArea(anchor) || !draggable)
                    return;
                // m = p + c + a  =>  c = m - p - a
                Point mouseOnScreen = e.getLocationOnScreen(),
                      parentOnScreen = getParent().getLocationOnScreen();
                setLocation(
                        mouseOnScreen.x - parentOnScreen.x - anchor.x,
                        mouseOnScreen.y - parentOnScreen.y - anchor.y);
                // reset Z-order to make this component the topmost (if more
                // components overlap, the last one of them, that was dragged,
                // will get dragged
                getParent().setComponentZOrder(component, 0);
                if(repaintParent)
                    getParent().repaint();
                else
                    repaint(); // just this component
            }
        });
    }
    
    @Override
    public void paintComponent(Graphics g) {
    }
    
    /**
     * Compute whether he mouse is inside the component, ie. whether it can be dragged from this point.
     * The JComponent is rectangular, however we want out components to appear
     * to be any shape we chose and we want them to behave naturaly when
     * clicked or dragged.
     * @param p Point relative to the coordinates within the component.
     * @return True if the mouse is in the visible area of the component.
     */
    protected boolean mouseInsideRealArea(Point p) {
        return true;
    }
}
 