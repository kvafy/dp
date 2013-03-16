// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/11

package bna.bnlib.learning;

import bna.bnlib.misc.Toolkit;
import bna.bnlib.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;


public class TabuSearchLearningAlgorithm extends StructureLearningAlgorithm {
    protected Random rand = new Random();
    // tabu list
    private int tabuListSize;
    private LinkedList<AlterationAction> tabuList = new LinkedList<AlterationAction>();
    // random restart parameters
    private int randomRestartSteps;
    

    public TabuSearchLearningAlgorithm(ScoringMethod method, int tabulistSize, int randomRestartSteps) {
        super(method);
        this.tabuListSize = tabulistSize;
        this.randomRestartSteps = randomRestartSteps;
    }
    
    @Override
    public BayesianNetwork learn(BayesianNetwork bnInitial, LearningController controller) { 
        // currently inspected network
        BayesianNetwork bnCurrent = bnInitial.copyStructureWithEmptyCPDs(); // so that the initial network won't be affected
        // to keep the best scoring network
        BayesianNetwork bnBest = bnCurrent;
        double bnBestScore = this.scoringMethod.absoluteScore(bnCurrent);
        
        try {
            long iteration = 0;
            int randomStepsToGo = 0; // how many random steps to take
            while(!controller.shouldStop(iteration)) {
                // single step of local search
                AlterationAction selectedAteration;
                if(randomStepsToGo == 0)
                    selectedAteration = this.getBestAlteration(bnCurrent);
                else {
                    selectedAteration = this.getRandomAlteration(bnCurrent);
                    randomStepsToGo--;
                }
                if(selectedAteration != null) {
                    //System.out.println(String.format("[iteration %d] applying alteration %s", iteration, selectedAteration.toString()));
                    selectedAteration.apply(bnCurrent);
                    this.insertIntoTabuList(selectedAteration.getUndoAction());
                    this.scoringMethod.notifyNetworkAlteration(selectedAteration);
                    // keep track of the overall best structure seen so far
                    double bnCurrentScore = this.scoringMethod.absoluteScore(bnCurrent);
                    if(bnBestScore < bnCurrentScore) {
                        bnBestScore = bnCurrentScore;
                        bnBest = bnCurrent.copyStructureWithEmptyCPDs(); // the parameters have not been learnt yet
                    }
                    else if(randomStepsToGo == 0 && !Toolkit.doubleEquals(bnBestScore, bnCurrentScore)) {
                        // local maxima => random restart
                        //randomStepsToGo = this.randomRestartSteps;
                        //System.out.println(String.format("[iteration %d] random restart", iteration));
                    }
                }
                else {
                    System.out.println(String.format("[iteration %d] no alteration possible! => making precautions", iteration));
                    this.reduceTabuList(0.5);
                }
                iteration++;
            }
        }
        catch(BayesianNetworkException bnex) {
            // network is somehow inconsistent (bestAlteration.apply threw this exception
            // meaning that the nodes, whose connection was to be changed, aren't
            // actually in the network)
            throw new BayesianNetworkRuntimeException("Internal error during learing: " + bnex.getMessage());
        }
        
        return bnBest;
    }
    
    private AlterationAction getBestAlteration(BayesianNetwork bnCurrent) throws BayesianNetworkException {
        // for keeping of the best actions for single step of local search
        ArrayList<AlterationAction> bestActions = new ArrayList<AlterationAction>();
        double bestGain = Double.NEGATIVE_INFINITY;
        // inspect all possibilities
        AlterationEnumerator alterations = new AlterationEnumerator(bnCurrent);
        for(AlterationAction alteration : alterations) {
            if(this.isTabuAction(alteration))
                continue;
            double gain = this.scoringMethod.deltaScore(bnCurrent, alteration);
            if(gain >= bestGain) {
                if(Toolkit.doubleEquals(gain, bestGain))
                    bestActions.add(alteration);
                else {
                    bestActions.clear();
                    bestActions.add(alteration);
                    bestGain = gain;
                    // TODO drop all actions not equal (within tolerance) to the new best
                }
            }
        }
        // return the best action (if more, pick one at random)
        if(bestActions.isEmpty())
            return null;
        else if(bestActions.size() == 1)
            return bestActions.get(0);
        else {
            int rndIndex = this.rand.nextInt(bestActions.size());
            return bestActions.get(rndIndex);
        }
    }
    
    private AlterationAction getRandomAlteration(BayesianNetwork bnCurrent) {
        AlterationEnumerator alterations = new AlterationEnumerator(bnCurrent);
        if(alterations.getAlterationCount() == 0)
            return null;
        else
            return alterations.getRandomAlteration(this.rand);
    }

    
    // tabu list access and management
    
    private void insertIntoTabuList(AlterationAction action) {
        this.tabuList.addLast(action);
        while(this.tabuList.size() > this.tabuListSize)
            this.tabuList.removeFirst();
    }
    
    private void reduceTabuList(double fraction) {
        int newSize = (int)(fraction * this.tabuList.size());
        while(this.tabuList.size() > newSize)
            this.tabuList.removeFirst();
    }
    
    private boolean isTabuAction(AlterationAction action) {
        return this.tabuList.contains(action);
    }
}
