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
 */
public class AlterationEnumerator implements Iterable<AlterationAction> {
    private ArrayList<AlterationAction> possibleActions;
    
    
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
        this.verifyAlterationCountHypotheses(NODE_COUNT, actions.size()); // TODO remove
        return actions;
    }
    
    @Override
    public Iterator<AlterationAction> iterator() {
        return this.possibleActions.iterator();
    }
    
    public int getAlterationCount() {
        return this.possibleActions.size();
    }
    
    public AlterationAction getRandomAlteration(Random rand) {
        int index = rand.nextInt(this.possibleActions.size());
        return this.possibleActions.get(index);
    }
    
    private void verifyAlterationCountHypotheses(int nodesCount, int alterationsCount) {
        int N = nodesCount;
        // TODO remove
        int[] upperBounds = {
            //N * (N - 1) / 2  +  N * N / 4,
            //N * (N - 1) / 2  +  (int)((2.0 / 3) * N * (N - 1) / 2),
            N * (N - 1) / 2  +  N * (N - 1) / 2 // must be always true
        };
        for(int i = 0 ; i < upperBounds.length ; i++)
            if(upperBounds[i] < alterationsCount)
                System.out.printf("max #of alterations: hypothesis %d broken by %.1f %%\n", i, 100.0 * (alterationsCount - upperBounds[i]) / alterationsCount);
        
    }
}
