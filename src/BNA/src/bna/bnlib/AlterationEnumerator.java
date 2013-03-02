// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/01

package bna.bnlib;

import java.util.Iterator;
import java.util.LinkedList;


/**
 * Iterator of all possible alteration actions of a BN.
 */
public class AlterationEnumerator implements Iterable<AlterationAction> {
    private LinkedList<AlterationAction> possibleActions;
    
    
    public AlterationEnumerator(BayesianNetwork bn) {
        this.possibleActions = this.determinePossibleActions(bn);
    }
    
    private LinkedList<AlterationAction> determinePossibleActions(BayesianNetwork bn) {
        LinkedList<AlterationAction> actions = new LinkedList<AlterationAction>();
        Node[] topsort = bn.topologicalSortNodes();
        final int NODE_COUNT = topsort.length;
        boolean[][] r = bn.adjacencyMatrix(topsort);
        boolean[][] rTrans = Toolkit.transitiveClosure(r);
        
        for(int i = 0 ; i < NODE_COUNT ; i++) {
            for(int j = 0 ; j < NODE_COUNT ; j++) {
                if(i == j)
                    continue;
                if(r[i][j] == true) {
                    // edge removal is always possible
                    actions.add(new AlterationActionRemoveEdge(topsort[i].getVariable(), topsort[j].getVariable()));
                    // edge reversal has more complicated conditions (see thesis for justification)
                    boolean reversalPossible = true;
                    for(int m = i + 1 ; m < j ; m++) {
                        if(rTrans[i][m] && rTrans[m][j]) {
                            reversalPossible = false;
                            break;
                        }
                    }
                    if(reversalPossible)
                        actions.add(new AlterationActionReverseEdge(topsort[i].getVariable(), topsort[j].getVariable()));
                }
                else {
                    // edge addition
                    if(rTrans[j][i] == false)
                        actions.add(new AlterationActionAddEdge(topsort[i].getVariable(), topsort[j].getVariable()));
                }
            }
        }
        
        return actions;
    }
    
    @Override
    public Iterator<AlterationAction> iterator() {
        return this.possibleActions.iterator();
    }
}
