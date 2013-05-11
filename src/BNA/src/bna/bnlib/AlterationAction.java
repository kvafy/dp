// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib;


/**
 * Abstract class of an action that takes an existing Bayesian network and modifies it.
 * Command design pattern is used, including the "undo" operation.
 */
public abstract class AlterationAction {
    protected final Variable parent, child;
    
    
    public AlterationAction(Variable parent, Variable child) {
        this.parent = parent;
        this.child = child;
    }
    
    /**
     * Apply this alteration action on given network, in-place.
     * @throws BNLibIllegalStructuralModificationException When the modification
     *         is not legal.
     */
    public abstract void apply(BayesianNetwork bn) throws BNLibIllegalStructuralModificationException;
    
    /**
     * Apply this alteration action that has been previously performed on given network, in-place.
     * @throws BNLibIllegalStructuralModificationException When the modification
     *         is not legal.
     */
    public abstract void undo(BayesianNetwork bn) throws BNLibIllegalStructuralModificationException;
    
    public abstract AlterationAction getUndoAction();
    
    /** Get parent variable that was passed to the constructor. */
    public Variable getParentVariable() {
        return this.parent;
    }
    
    /** Get child variable that was passed to the constructor. */
    public Variable getChildVariable() {
        return this.child;
    }
    
    @Override
    public boolean equals(Object o) {
        if(o instanceof AlterationAction) {
            Variable oParent = ((AlterationAction)o).parent,
                     oChild = ((AlterationAction)o).child;
            return oParent.equals(this.parent) && oChild.equals(this.child);
        }
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.parent != null ? this.parent.hashCode() : 0);
        hash = 79 * hash + (this.child != null ? this.child.hashCode() : 0);
        return hash;
    }
}
