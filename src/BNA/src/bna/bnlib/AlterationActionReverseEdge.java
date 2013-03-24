// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib;


/**
 * Action that reverses an edge in an existing Bayesian network.
 */
public class AlterationActionReverseEdge extends AlterationAction {
    
    public AlterationActionReverseEdge(Variable parent, Variable child) {
        super(parent, child);
    }

    @Override
    public void apply(BayesianNetwork bnOrig) throws BNLibIllegalStructuralModificationException {
        bnOrig.reverseDependency(this.parent, this.child);
    }
    
    @Override
    public void undo(BayesianNetwork bn) throws BNLibIllegalStructuralModificationException {
        bn.reverseDependency(this.child, this.parent);
    }
    
    @Override
    public AlterationAction getUndoAction() {
        return new AlterationActionReverseEdge(this.child, this.parent);
    }

    @Override
    public String toString() {
        return String.format("AlterationActionReverseEdge(%s, %s)", this.parent.getName(),
                                                                    this.child.getName());
    }
}
