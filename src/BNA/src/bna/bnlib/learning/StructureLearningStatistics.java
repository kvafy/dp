// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/28

package bna.bnlib.learning;

import bna.bnlib.AlterationActionAddEdge;
import bna.bnlib.BayesianNetwork;
import bna.bnlib.Node;
import bna.bnlib.Variable;
import bna.bnlib.misc.TextualTable;
import bna.bnlib.misc.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Provides means of evaluation of structure learning.
 * This means keeping best scoring networks, connection matrix etc.
 */
public class StructureLearningStatistics {
    private final BayesianNetwork bnReferential;
    // what structures have been found (clustered by equal score)
    private ArrayList<LearningRecord> bnLearntHitparade = new ArrayList<LearningRecord>();
    // simple counting of the number of times the exact referential network is found
    private int countReferentialOccurences = 0;
    private int countAllOccurences = 0;
    // for mean score
    private double scoresSum = 0;
    // edge occurence frequency
    private Variable[] variableOrder; // index of a variable is the same as in edgeOccurences matrix
    private HashMap<Variable, Integer> variable2IndexMapping = new HashMap<Variable, Integer>();
    private int[][] edgeOccurences;
    
    
    public StructureLearningStatistics(BayesianNetwork referential) {
        this.bnReferential = referential.copyStructureWithEmptyCPDs();
        
        this.variableOrder = this.bnReferential.getVariables();
        this.edgeOccurences = new int[this.variableOrder.length][this.variableOrder.length];
        for(int i = 0 ; i < this.variableOrder.length ; i++)
            this.variable2IndexMapping.put(this.variableOrder[i], new Integer(i));
    }
    
    public void registerLearntNetwork(BayesianNetwork bnLearnt, double score) {
        this.addLearntNetworkStructure(bnLearnt, score);
        
        this.updateEdgeFrequencies(bnLearnt, score);
        
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
    
    private void updateEdgeFrequencies(BayesianNetwork bnLearnt, double score) {
        for(Node parentNode : bnLearnt.getNodes()) {
            Variable parent = parentNode.getVariable();
            Integer parentIndex = this.variable2IndexMapping.get(parent);
            for(Variable child : parentNode.getChildVariables()) {
                Integer childIndex = this.variable2IndexMapping.get(child);
                this.edgeOccurences[parentIndex][childIndex]++;
            }
        }
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
        String[] tableHeader = new String[this.variableOrder.length + 1];
        tableHeader[0] = "from/to";
        for(int i = 0 ; i < this.variableOrder.length ; i++)
            tableHeader[i + 1] = this.variableOrder[i].getName();
        TextualTable edgeFreqTable = new TextualTable(tableHeader, 2, true);
        for(int i = 0 ; i < this.variableOrder.length ; i++) {
            Object[] tableRow = new Object[this.variableOrder.length + 1];
            tableRow[0] = this.variableOrder[i].getName();
            for(int j = 0 ; j < this.variableOrder.length ; j++)
                tableRow[j + 1] = new Integer(this.edgeOccurences[i][j]);
            edgeFreqTable.addRow(tableRow);
        }
        System.out.println("Edge frequency table:");
        System.out.println(edgeFreqTable.toString());
        System.out.println("");
        
        // percentage discovered the true network
        System.out.printf("Correct network found in %.2f %% cases.\n", 100.0 * this.getSuccessFrequency());
    }
    
    /** Relative number of thimes the exact original structure was found. */
    public double getSuccessFrequency() {
        if(this.countAllOccurences > 0)
            return (double)this.countReferentialOccurences / this.countAllOccurences;
        else
            return 0;
    }
    
    /** Average score of the best structure found. */
    public double getMeanScore() {
        if(this.countAllOccurences > 0)
            return this.scoresSum / this.countAllOccurences;
        else
            return 0;
    }
    
    /*public BayesianNetwork[] getBestScroringNetworks() {
    }*/
    
    /**
     * By the edge occurence frequency determine the most probable and highest scoring structure.
     * The algorithm works as follows:
     *     order records of the edge-frequency matrix in descending order
     *     for (u,v) in edge-frequency matrix:
     *         if add(u, v) introduces a cycle: (u is descendant of v)
     *             continue
     *         try add(u, v), if score worsenes then break, otherwise accept change
     *     return the network
     */
    public BayesianNetwork getMostProbableNetwork(ScoringMethod scoringMethod) {
        final int VARIABLE_COUNT = this.variableOrder.length;
        BayesianNetwork bn = new BayesianNetwork(this.variableOrder);
        double currentScore = scoringMethod.absoluteScore(bn);
        boolean[][] edgeInspected = new boolean[VARIABLE_COUNT][VARIABLE_COUNT];
        
        // order records of the edge-frequency matrix in descending order
        while(true) {
            int maxEdgeFreq = 0,
                maxParentIndex = -1,
                maxChildIndex = -1;
            // find the next most frequent edge
            for(int i = 0 ; i < VARIABLE_COUNT ; i++) {
                for(int j = 0 ; j < VARIABLE_COUNT ; j++) {
                    if(!edgeInspected[i][j] && this.edgeOccurences[i][j] > maxEdgeFreq) {
                        maxEdgeFreq = this.edgeOccurences[i][j];
                        maxParentIndex = i;
                        maxChildIndex = j;
                    }
                }
            }
            if(maxEdgeFreq == 0) // no more edges to inspect
                break;
            // invalidate the current edge frequency
            edgeInspected[maxParentIndex][maxChildIndex] = true;
            Variable parent = this.variableOrder[maxParentIndex],
                     child = this.variableOrder[maxChildIndex];
            if(bn.hasDescendant(child, parent))
                continue; // we would introduce a cycle
            AlterationActionAddEdge action = new AlterationActionAddEdge(parent, child);
            action.apply(bn);
            double newScore = scoringMethod.absoluteScore(bn);
            if(newScore > currentScore) {
                currentScore = newScore;
            }
            else {
                action.undo(bn);
                break; // TODO or try other edges?
            }
        }
        return bn;
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
