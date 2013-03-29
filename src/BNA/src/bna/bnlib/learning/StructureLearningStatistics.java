// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/28

package bna.bnlib.learning;

import bna.bnlib.BayesianNetwork;
import bna.bnlib.misc.Toolkit;
import java.util.ArrayList;


/**
 * Provides means of evaluation of structure learning.
 * This means keeping best scoring networks, connection matrix etc.
 */
public class StructureLearningStatistics {
    private final BayesianNetwork bnReferential;
    private ArrayList<LearningRecord> bnLearntHitparade = new ArrayList<LearningRecord>();
    // simple counting of the number of times the exact referential network is found
    private int countReferentialOccurences = 0;
    private int countAllOccurences = 0;
    // for mean score
    private double scoresSum = 0;
    
    
    public StructureLearningStatistics(BayesianNetwork referential) {
        this.bnReferential = referential.copyStructureWithEmptyCPDs();
    }
    
    public void registerLearntNetwork(BayesianNetwork bnLearnt, double score) {
        this.addLearntNetworkStructure(bnLearnt, score);
        
        this.countAllOccurences++;
        if(this.bnReferential.equalsStructurally(bnLearnt))
            this.countReferentialOccurences++;
        
        this.scoresSum += score;
    }
    
    private void addLearntNetworkStructure(BayesianNetwork bnLearnt, double score) {
        for(int i = 0 ; i < this.bnLearntHitparade.size() ; i++) {
            LearningRecord iRecord = this.bnLearntHitparade.get(i);
            if(Toolkit.doubleEquals(score, iRecord.score)) {
                if(!iRecord.containsStructure(bnLearnt)) // want to keep unique structures
                    iRecord.networks.add(bnLearnt);
                return;
            }
            else if(score > iRecord.score) {
                LearningRecord newBestRecord = new LearningRecord(bnLearnt, score);
                this.bnLearntHitparade.add(0, newBestRecord);
                return;
            }
        }
        LearningRecord newWorstRecord = new LearningRecord(bnLearnt, score);
        this.bnLearntHitparade.add(newWorstRecord);
    }
    
    public void report() {
        // best-scoring network(s) hitparade
        System.out.println("Best-scoring networks hitparade");
        for(LearningRecord record : this.bnLearntHitparade) {
            System.out.printf("- score %.2f for %d distinct network(s):\n", record.score, record.networks.size());
            for(int i = 0 ; i < record.networks.size() ; i++) {
                System.out.printf("(no %d)\n", i + 1);
                System.out.println(record.networks.get(i).dumpStructure());
            }
            System.out.println("");
        }
        System.out.println("");
        
        // connection matrix
        // TODO
        
        // percentage discovered the true network
        System.out.printf("Correct network found in %.2f %% cases.\n", 100.0 * this.getSuccessFrequency());
    }
    
    public double getSuccessFrequency() {
        if(this.countAllOccurences > 0)
            return (double)this.countReferentialOccurences / this.countAllOccurences;
        else
            return 0;
    }
    
    public double getMeanScore() {
        if(this.countAllOccurences > 0)
            return this.scoresSum / this.countAllOccurences;
        else
            return 0;
    }
    
    /*public BayesianNetwork[] getBestScroringNetworks() {
    }*/
    
    public BayesianNetwork getMostProbableNetwork() {
        // order records of the edge-frequency matrix in descending order
        // for (u,v) in edge-frequency matrix:
        //     if add(u, v) introduces a cycle: (u is descendant of v)
        //         continue
        //     try add(u, v), if score worsenes, break
        // return the network
        throw new UnsupportedOperationException();
    }
    
    /** For internal book-keeping within this file. */
    class LearningRecord {
        final double score;
        final ArrayList<BayesianNetwork> networks = new ArrayList<BayesianNetwork>();;
        
        public LearningRecord(BayesianNetwork bn, double score) {
            this.score = score;
            this.networks.add(bn);
        }
        
        public boolean containsStructure(BayesianNetwork bn) {
            for(BayesianNetwork bnHas : this.networks)
                if(bnHas.equalsStructurally(bn))
                    return true;
            return false;
        }
    }
}
