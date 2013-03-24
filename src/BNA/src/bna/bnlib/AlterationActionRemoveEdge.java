// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib;


/**
 * Action that removes an edge from an existing Bayesian network.
 */
public class AlterationActionRemoveEdge extends AlterationAction {
    
    public AlterationActionRemoveEdge(Variable parent, Variable child) {
        super(parent, child);
    }

    @Override
    public void apply(BayesianNetwork bnOrig) throws BNLibIllegalStructuralModificationException {
        bnOrig.removeDependency(this.parent, this.child);
    }
    
    @Override
    public void undo(BayesianNetwork bn) throws BNLibIllegalStructuralModificationException {
        bn.addDependency(this.parent, this.child);
    }
    
    @Override
    public AlterationAction getUndoAction() {
        return new AlterationActionAddEdge(this.parent, this.child);
    }

    @Override
    public String toString() {
        return String.format("AlterationActionRemoveEdge(%s, %s)", this.parent.getName(),
                                                                   this.child.getName());
    }
}
