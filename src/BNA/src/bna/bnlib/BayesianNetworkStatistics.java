// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/14

package bna.bnlib;


/**
 * Simple structure (no encapsulation) carrying statistical informatin regarding a BN.
 */
public class BayesianNetworkStatistics {
    public int inDegreeMin,
               inDegreeMax;
    public double inDegreeAve;
    public int outDegreeMin,
               outDegreeMax;
    public double outDegreeAve;
    public int nodes,
               edges;
    public int degreesOfFreedom;
    
    public BayesianNetworkStatistics() {
        this.inDegreeMin = this.inDegreeMax = this.outDegreeMin = this.outDegreeMax = 0;
        this.inDegreeAve = this.outDegreeAve = 0.0;
        this.nodes = this.edges = 0;
        this.degreesOfFreedom = 0;
    }
}
