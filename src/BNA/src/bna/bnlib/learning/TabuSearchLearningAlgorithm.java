// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/11

package bna.bnlib.learning;

import bna.bnlib.*;
import java.util.LinkedList;


/**
 *
 */
public class TabuSearchLearningAlgorithm extends HillClimbLearningAlgorithm {
    // tabu list
    private int tabuListSize;
    private LinkedList<AlterationAction> tabuList = new LinkedList<AlterationAction>();
    

    public TabuSearchLearningAlgorithm(ScoringMethod method, int tabulistSize) {
        super(method);
        this.tabuListSize = tabulistSize;
    }

    private void insertIntoTabuList(AlterationAction action) {
        this.tabuList.addLast(action);
        while(this.tabuList.size() > this.tabuListSize)
            this.tabuList.removeFirst();
    }
    
    private boolean isTabuAction(AlterationAction action) {
        return this.tabuList.contains(action);
    }
    
    private void reduceTabuList(double fraction) {
        int newSize = (int)(fraction * this.tabuList.size());
        while(this.tabuList.size() > newSize)
            this.tabuList.removeFirst();
    }
    
    
    // hooks redefinition
    
    @Override
    protected boolean isAllowedAction(AlterationAction action) {
        return super.isAllowedAction(action) && !this.isTabuAction(action);
    }
    
    @Override
    protected void tookBestAction(AlterationAction actionTaken) {
        super.tookBestAction(actionTaken);
        this.insertIntoTabuList(actionTaken.getUndoAction());
    }
    
    @Override
    protected void noActionPossible() {
        super.noActionPossible();
        this.reduceTabuList(0.5);
    }
}
