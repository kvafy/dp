// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib;


/**
 * Action that adds an edge to an existing Bayesian network.
 */
public class AlterationActionAddEdge extends AlterationAction {

    public AlterationActionAddEdge(Variable parent, Variable child) {
        super(parent, child);
    }

    @Override
    public void apply(BayesianNetwork bnOrig) throws BNLibIllegalStructuralModificationException {
        bnOrig.addDependency(this.parent, this.child);
    }
    
    @Override
    public void undo(BayesianNetwork bn) throws BNLibIllegalStructuralModificationException {
        bn.removeDependency(this.parent, this.child);
    }
    
    @Override
    public AlterationAction getUndoAction() {
        return new AlterationActionRemoveEdge(this.parent, this.child);
    }
    
    @Override
    public String toString() {
        return String.format("AlterationActionAddEdge(%s, %s)", this.parent.getName(),
                                                                this.child.getName());
    }
}
