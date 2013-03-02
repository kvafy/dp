// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib;


/**
 * Action that removes an edge from an existing Bayesian network.
 */
public class AlterationActionRemoveEdge extends AlterationAction {
    private Variable parent, child;
    
    
    public AlterationActionRemoveEdge(Variable parent, Variable child) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    public void apply(BayesianNetwork bnOrig) throws BayesianNetworkException {
        bnOrig.removeDependency(this.parent, this.child);
    }

    @Override
    public String toString() {
        return String.format("AlterationActionRemoveEdge(%s, %s)", this.parent.getName(),
                                                                   this.child.getName());
    }
}
