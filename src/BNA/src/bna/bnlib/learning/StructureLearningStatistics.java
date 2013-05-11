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
import java.util.Arrays;
import java.util.HashMap;


/**
 * Provides means of evaluation of structure learning.
 * This means keeping best-scoring networks, connection matrix etc.
 */
public class StructureLearningStatistics {
    // what structures have been found (clustered by equal score and sorted in decreasing order)
    private ArrayList<LearningRecord> bnLearntHitparade = new ArrayList<LearningRecord>();
    // for mean score
    private int allNetworksCount = 0;
    private double scoresSum = 0;
    // edge occurence frequency
    private Variable[] variableOrder; // index of a variable is the same as in edgeOccurences matrix
    private HashMap<Variable, Integer> variable2IndexMapping = new HashMap<Variable, Integer>();
    private int[][] edgeOccurences;
    
    
    /** Initialize structure learing statistics for learning of network with given set of variables. */
    public StructureLearningStatistics(Variable[] variables) {
        this.variableOrder = Arrays.copyOf(variables, variables.length);
        this.edgeOccurences = new int[this.variableOrder.length][this.variableOrder.length];
        for(int i = 0 ; i < this.variableOrder.length ; i++)
            this.variable2IndexMapping.put(this.variableOrder[i], new Integer(i));
    }
    
    /** Reflect the given network in this statistics. */
    public void registerLearntNetwork(BayesianNetwork bnLearnt, double score) {
        this.addLearntNetworkStructure(bnLearnt, score);
        
        this.updateEdgeFrequencies(bnLearnt, score);
        
        this.allNetworksCount++;
        this.scoresSum += score;
    }
    
    private void addLearntNetworkStructure(BayesianNetwork bnLearnt, double score) {
        for(int i = 0 ; i < this.bnLearntHitparade.size() ; i++) {
            LearningRecord iRecord = this.bnLearntHitparade.get(i);
            if(Toolkit.doubleEquals(score, iRecord.score, 1e-2)) { // high tolerance because of Bayesian score
                if(!iRecord.containsStructure(bnLearnt)) // want to keep only unique structures
                    iRecord.networks.add(bnLearnt);
                return;
            }
            else if(score > iRecord.score) {
                LearningRecord newScoreRecord = new LearningRecord(bnLearnt, score);
                this.bnLearntHitparade.add(i, newScoreRecord);
                return;
            }
        }
        LearningRecord newWorstRecord = new LearningRecord(bnLearnt, score);
        this.bnLearntHitparade.add(this.bnLearntHitparade.size(), newWorstRecord); // append as last
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
    
    /** Print textual report to stdout. */
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
    }
    
    /** All variables the learnt networks contain. */
    public Variable[] getVariables() {
        return Arrays.copyOf(this.variableOrder, this.variableOrder.length);
    }
    
    /** matrix[i][j] == count means that there has been count-times occurent of the edge (vertex_i, vertex_j). */
    public int[][] getEdgeCountMatrix() {
        int variableCount = this.variableOrder.length;
        int[][] copy = new int[variableCount][];
        for(int i = 0 ; i < variableCount ; i++)
            copy[i] = Arrays.copyOf(this.edgeOccurences[i], variableCount);
        return copy;
    }
    
    /** What is the highest score we have seen so far? */
    public Double getBestScoreSoFar() {
        if(this.bnLearntHitparade.isEmpty())
            return null;
        LearningRecord firstRecord = this.bnLearntHitparade.get(0);
        return firstRecord.score;
    }
    
    /** Get networks sharing the best score achieved. */
    public BayesianNetwork[] getBestScoringNetworks() {
        if(this.bnLearntHitparade.isEmpty())
            return null;
        LearningRecord firstRecord = this.bnLearntHitparade.get(0);
        ArrayList<BayesianNetwork> bestNetworksList = firstRecord.networks;
        BayesianNetwork[] bestNetworksArray = new BayesianNetwork[bestNetworksList.size()];
        bestNetworksList.toArray(bestNetworksArray);
        return bestNetworksArray;
    }
    
    /** Average score of the best structure found. */
    public double getMeanScore() {
        if(this.allNetworksCount > 0)
            return this.scoresSum / this.allNetworksCount;
        else
            return 0;
    }
    
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
                break;
            }
        }
        return bn;
    }
    
    /** For internal book-keeping within this class. */
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
