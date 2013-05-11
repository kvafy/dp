// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/01

package bna.bnlib;

import bna.bnlib.learning.StructuralConstraints;
import bna.bnlib.misc.Toolkit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


/**
 * Iterator of all possible alteration actions of a BN.
 * The implementation uses Warshall's algorithm and is based on theory
 * of binary relations and topological orderings (see text of the thesis for
 * a detailed explanation).
 */
public class AlterationEnumerator implements Iterable<AlterationAction> {
    private ArrayList<AlterationAction> possibleActions;
    
    
    /**
     * Computes all possible structural alterations that meet given constraints.
     * The alterations are saved within the object to allow repeated iteration.
     */
    public AlterationEnumerator(BayesianNetwork bn, StructuralConstraints constraints) {
        this.possibleActions = this.determinePossibleActions(bn, constraints);
    }
    
    private ArrayList<AlterationAction> determinePossibleActions(BayesianNetwork bn, StructuralConstraints constraints) {
        ArrayList<AlterationAction> actions = new ArrayList<AlterationAction>();
        Node[] topsort = bn.topologicalSortNodes();
        final int NODE_COUNT = topsort.length;
        boolean[][] r = bn.adjacencyMatrix(topsort);
        boolean[][] rTrans = Toolkit.transitiveClosure(r);
        
        for(int i = 0 ; i < NODE_COUNT ; i++) {
            for(int j = 0 ; j < NODE_COUNT ; j++) {
                if(i == j)
                    continue;
                Variable iVar = topsort[i].getVariable(),
                         jVar = topsort[j].getVariable();
                
                if(r[i][j] == true) {
                    // edge removal is always possible
                    actions.add(new AlterationActionRemoveEdge(iVar, jVar));
                    
                    // edge reversal has more complicated conditions (please, see thesis for justification)
                    boolean reversalPossible = constraints.isConnectionAllowed(jVar, iVar)
                                            && constraints.isOKParentsCount(topsort[i].getParentCount() + 1);
                    for(int m = i + 1 ; m < j && reversalPossible; m++) {
                        if(rTrans[i][m] && rTrans[m][j])
                            reversalPossible = false;
                    }
                    if(reversalPossible)
                        actions.add(new AlterationActionReverseEdge(iVar, jVar));
                }
                else {
                    // edge addition
                    if(rTrans[j][i] == false) {
                        boolean additionPossible = constraints.isConnectionAllowed(iVar, jVar)
                                                && constraints.isOKParentsCount(topsort[j].getParentCount() + 1);
                        if(additionPossible)
                            actions.add(new AlterationActionAddEdge(iVar, jVar));
                    }
                }
                
            }
        }
        return actions;
    }
    
    @Override
    public Iterator<AlterationAction> iterator() {
        return this.possibleActions.iterator();
    }
    
    /** Get number of all legal alterations wrt the given constraints. */
    public int getAlterationCount() {
        return this.possibleActions.size();
    }
    
    /** Randomly pick a single legal alteration. */
    public AlterationAction getRandomAlteration(Random rand) {
        int index = rand.nextInt(this.possibleActions.size());
        return this.possibleActions.get(index);
    }
}
