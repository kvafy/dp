// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib;


/**
 * Action that reverses an edge in an existing Bayesian network.
 */
public class AlterationActionReverseEdge extends AlterationAction {
    private Variable parent, child;
    
    
    public AlterationActionReverseEdge(Variable parent, Variable child) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    public void apply(BayesianNetwork bnOrig) throws BayesianNetworkException {
        bnOrig.reverseDependency(this.parent, this.child);
    }

    @Override
    public String toString() {
        return String.format("AlterationActionReverseEdge(%s, %s)", this.parent.getName(),
                                                                    this.child.getName());
    }
}
