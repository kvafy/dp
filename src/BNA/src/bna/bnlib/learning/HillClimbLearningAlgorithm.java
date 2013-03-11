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
    // keeping of the best actions for current step of local search
    private ArrayList<AlterationAction> stepBestActions = new ArrayList<AlterationAction>();
    private double stepBestGain;
    
    
    public HillClimbLearningAlgorithm(ScoringMethod scoringMethod) {
        super(scoringMethod);
    }
    
    @Override
    public BayesianNetwork learn(BayesianNetwork bnInitial, LearningController controller) { 
        // currently inspected network
        BayesianNetwork bnCurrent = bnInitial.copyWithEmptyCPDs(); // so that the initial network won't be affected
        double bnCurrentScore = this.scoringMethod.absoluteScore(bnCurrent);
        // to keep the best scoring network
        BayesianNetwork bnBest = bnCurrent;
        double bnBestScore = bnCurrentScore;
        
        try {
            long iteration = 0;
            while(!controller.shouldStop(iteration)) {
                // single step of local search
                this.clearBestActionMemory();
                AlterationEnumerator alterations = new AlterationEnumerator(bnCurrent);
                //Toolkit.dumpCollection("Possible alterations of BN", alterations);
                for(AlterationAction alteration : alterations) {
                    if(!this.isAllowedAction(alteration))
                        continue;
                    double gain = this.scoringMethod.deltaScore(bnCurrent, alteration);
                    this.possibleNewBestAction(alteration, gain);
                }
                // perform the best action (if more are possible, perform one arbitrarily)
                AlterationAction bestAlteration = this.getOneOfTheBestActions();
                if(bestAlteration != null) {
                    System.out.println(String.format("[iteration %d] applying alteration %s", iteration, bestAlteration.toString()));
                    bestAlteration.apply(bnCurrent);
                    this.tookBestAction(bestAlteration);
                    // keep track of the overall best structure seen so far
                    bnCurrentScore += this.stepBestGain;
                    if(bnBestScore < bnCurrentScore) {
                        bnBestScore = bnCurrentScore;
                        bnBest = bnCurrent.copyWithEmptyCPDs(); // the parameters have not been learnt yet
                    }
                }
                else {
                    System.out.println(String.format("[iteration %d] no alteration possible!", iteration));
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
    
    private void clearBestActionMemory() {
        this.stepBestActions.clear();
        this.stepBestGain = Double.NEGATIVE_INFINITY;
    }
    
    private void possibleNewBestAction(AlterationAction action, double gain) {
        if(this.stepBestGain <= gain) {
            if(Toolkit.doubleEquals(this.stepBestGain, gain)) // allow for a slight inequality
                this.stepBestActions.add(action);
            else {
                this.stepBestActions.clear();
                this.stepBestActions.add(action);
                this.stepBestGain = gain;
            }
        }
    }
    
    private AlterationAction getOneOfTheBestActions() {
        switch(this.stepBestActions.size()) {
            case 1:
                return this.stepBestActions.get(0); // avoid expensive random number generation
            case 0:
                return null;
            default:
                int bestAlterationIndex = this.rand.nextInt(this.stepBestActions.size());
                return this.stepBestActions.get(bestAlterationIndex);
        }
    }
    
    
    // hooks of learn(...) method
    // IMPORTANT a subclass HAS to invoke these methods too if it overrides them !!!
    
    /** Is the action allowed? Its always legal wrt keeping the network a DAG. */
    protected boolean isAllowedAction(AlterationAction action) {
        return true;
    }
    
    /** Notification that we took given action in current iteration of greedy search. */
    protected void tookBestAction(AlterationAction actionTaken) {
        this.scoringMethod.notifyNetworkAlteration(actionTaken);
    }
    
    /** What to do when no action is possible (by network structure and by isAllowedAction(...) method). */
    protected void noActionPossible() {
    }
}
