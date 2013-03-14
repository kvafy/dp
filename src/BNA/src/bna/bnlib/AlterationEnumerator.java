// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/01

package bna.bnlib;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Random;


/**
 * Iterator of all possible alteration actions of a BN.
 */
public class AlterationEnumerator implements Iterable<AlterationAction> {
    private ArrayList<AlterationAction> possibleActions;
    
    
    public AlterationEnumerator(BayesianNetwork bn) {
        this.possibleActions = this.determinePossibleActions(bn);
    }
    
    private ArrayList<AlterationAction> determinePossibleActions(BayesianNetwork bn) {
        ArrayList<AlterationAction> actions = new ArrayList<AlterationAction>();
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
        //this.verifyAlterationCountHypotheses(NODE_COUNT, actions.size()); // TODO remove
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
            N * (N - 1) / 2  +  N * N / 4,
            N * (N - 1) / 2  +  (int)((2.0 / 3) * N * (N - 1) / 2),
            N * (N - 1) / 2  +  N * (N - 1) / 2 // must be always true
        };
        for(int i = 0 ; i < upperBounds.length ; i++)
            if(upperBounds[i] < alterationsCount)
                System.out.printf("max #of alterations: hypothesis %d broken by %.1f %%\n", i, 100.0 * (alterationsCount - upperBounds[i]) / alterationsCount);
        
    }
}
