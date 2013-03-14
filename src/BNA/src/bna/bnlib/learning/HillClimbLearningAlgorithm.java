// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib.learning;

import bna.bnlib.*;
import java.util.ArrayList;
import java.util.Random;


/**
 * A general class for structure learning algorithms based on greedy local search.
 */
public class HillClimbLearningAlgorithm extends StructureLearningAlgorithm {
    protected Random rand = new Random();
    
    
    public HillClimbLearningAlgorithm(ScoringMethod scoringMethod) {
        super(scoringMethod);
    }
    
    @Override
    public BayesianNetwork learn(BayesianNetwork bnInitial, LearningController controller) { 
        // currently inspected network
        BayesianNetwork bnCurrent = bnInitial.copyWithEmptyCPDs(); // so that the initial network won't be affected
        // to keep the best scoring network
        BayesianNetwork bnBest = bnCurrent;
        double bnBestScore = this.scoringMethod.absoluteScore(bnCurrent);
        
        try {
            long iteration = 0;
            while(!controller.shouldStop(iteration)) {
                // single step of local search
                AlterationAction bestAlteration = this.findBestAction(bnCurrent);
                if(bestAlteration != null) {
                    System.out.println(String.format("[iteration %d] applying alteration %s", iteration, bestAlteration.toString()));
                    bestAlteration.apply(bnCurrent);
                    this.tookBestAction(bestAlteration);
                    this.scoringMethod.notifyNetworkAlteration(bestAlteration);
                    // keep track of the overall best structure seen so far
                    double bnCurrentScore = this.scoringMethod.absoluteScore(bnCurrent);
                    if(bnBestScore < bnCurrentScore) {
                        bnBestScore = bnCurrentScore;
                        bnBest = bnCurrent.copyWithEmptyCPDs(); // the parameters have not been learnt yet
                    }
                }
                else {
                    System.out.println(String.format("[iteration %d] no alteration possible! => making precautions", iteration));
                    this.noActionPossible();
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
    
    private AlterationAction findBestAction(BayesianNetwork bnCurrent) throws BayesianNetworkException {
        // keeping of the best actions for single step of local search
        ArrayList<AlterationAction> bestActions = new ArrayList<AlterationAction>();
        double bestGain = Double.NEGATIVE_INFINITY;
        
        // inspect all possibilities
        AlterationEnumerator alterations = new AlterationEnumerator(bnCurrent);
        for(AlterationAction alteration : alterations) {
            if(!this.isAllowedAction(alteration))
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
    
    // hooks of learn(...) method and its submethods
    // IMPORTANT a subclass HAS to invoke these methods too if it overrides them !!!
    
    /** Is the action allowed? Its always legal wrt keeping the network a DAG. */
    protected boolean isAllowedAction(AlterationAction action) {
        return true;
    }
    
    /** Notification what action has been taken in current iteration of greedy search. */
    protected void tookBestAction(AlterationAction actionTaken) {
    }
    
    /** What to do when no action is possible (by network structure and by isAllowedAction(...) method). */
    protected void noActionPossible() {
    }
}
