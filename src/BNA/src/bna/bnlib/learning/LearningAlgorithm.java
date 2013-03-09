// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib.learning;

import bna.bnlib.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;


/**
 * A general class for structure learning algorithms.
 */
public class LearningAlgorithm {
    protected ScoringMethod scoringMethod;
    
    
    public LearningAlgorithm(ScoringMethod scoringMethod) {
        this.scoringMethod = scoringMethod;
    }
    
    public BayesianNetwork learn(BayesianNetwork bnInitial, LearningController controller) {
        Random rand = new Random();
        BayesianNetwork bnCurrent = bnInitial;
        double bnCurrentScore = this.scoringMethod.absoluteScore(bnCurrent);
        // to keep the best scoring network
        BayesianNetwork bnBest = bnCurrent;
        double bnBestScore = bnCurrentScore;
        
        try {
            ArrayList<AlterationAction> bestGainActions = new ArrayList<AlterationAction>();
            double bestGain;
            
            long iteration = 0;
            while(!controller.shouldStop(iteration)) {
                bestGain = Double.NEGATIVE_INFINITY;
                bestGainActions.clear(); // for current step of local search
                System.out.println("========================== new iteration =============================");
                AlterationEnumerator alterations = new AlterationEnumerator(bnCurrent);
                //Toolkit.dumpCollection("Possible alterations of BN", alterations);
                for(AlterationAction alteration : alterations) {
                    double gain = this.scoringMethod.deltaScore(bnCurrent, alteration);
                    if(gain > bestGain) {
                        bestGain = gain;
                        bestGainActions.clear();
                        bestGainActions.add(alteration);
                    }
                    else if(gain == bestGain)
                        bestGainActions.add(alteration);
                }
                // perform the best action (if more are possible, perform one arbitrarily)
                AlterationAction bestAlteration;
                switch(bestGainActions.size()) {
                    case 1:
                        bestAlteration = bestGainActions.get(0); // avoid expensive random number generation
                        break;
                    case 0:
                        System.out.println("Structure learning: no alteration possible!");
                        bestAlteration = null;
                        break;
                    default:
                        int bestAlterationIndex = rand.nextInt(bestGainActions.size());
                        bestAlteration = bestGainActions.get(bestAlterationIndex);
                }
                System.out.println(" => applying alteration " + bestAlteration.toString());
                bestAlteration.apply(bnCurrent);
                
                // keep track of the overall best structure seen so far
                bnCurrentScore += bestGain;
                if(bnBestScore < bnCurrentScore) {
                    bnBestScore = bnCurrentScore;
                    bnBest = new BayesianNetwork(bnCurrent);
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
        // TODO make this class abstract
    }
}
