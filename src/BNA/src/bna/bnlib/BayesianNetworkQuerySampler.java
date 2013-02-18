// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/08

package bna.bnlib;

import java.util.Arrays;


/**
 * Generates samples for a probabilistic query P(X | Y, E = e) and computes the statistics as a factor of variables X union Y.
 */
public class BayesianNetworkQuerySampler implements BayesianNetworkSampler {
    private BayesianNetworkSampleProducer sampleProducer;
    // sampling statistics
    private AssignmentIndexMapper sampleMapper; // mapping of assignment XYVars to index in sampleCounter
    private double[] sampleCounter;             // for all instantiations of X,Y (ie. of XYVars)
    
    public BayesianNetworkQuerySampler(BayesianNetworkSampleProducer sampleProducer) {
        this.sampleProducer = sampleProducer;
        // initialize sampleCounter
        this.sampleMapper = new AssignmentIndexMapper(this.sampleProducer.XYVars);
        this.sampleCounter = new double[Toolkit.cardinality(this.sampleProducer.XYVars)];
        Arrays.fill(this.sampleCounter, 0.0);
    }
    
    
    /** Record a sampleNumber with given weight. */
    private void registerSample(int[] sampleVarsValues, double sampleWeight) {
        int assignmentIndex = this.sampleMapper.assignmentToIndex(sampleVarsValues);
        this.sampleCounter[assignmentIndex] += sampleWeight;
    }
    
    /** Perform sampling according to given controller. */
    @Override
    public void sample(SamplingController controller) {
        VariableSubsetMapper sampledVarsToXYVarsMapper = new VariableSubsetMapper(this.sampleProducer.sampledVars, this.sampleProducer.XYVars);
        
        int[] sampledVarsValues = new int[this.sampleProducer.sampledVars.length]; // to this array variables are sampled
        int[] XYVarsValues = new int[this.sampleProducer.XYVars.length];
        
        int sampleNumber = 0;
        this.sampleProducer.initializeSample(sampledVarsValues);
        while(!controller.stopFlag() && sampleNumber < controller.maxSamples()) {
            double sampleWeight = this.sampleProducer.produceSample(sampledVarsValues);
            sampledVarsToXYVarsMapper.map(sampledVarsValues, XYVarsValues);
            this.registerSample(XYVarsValues, sampleWeight);
            sampleNumber++;
        }
    }
    
    /**
     * Get the samples counter for instantiations of X,Y variables (just raw counters).
     */
    public Factor getSamplesCounter() {
        return new Factor(this.sampleProducer.XYVars, sampleCounter);
    }
    
    /**
     * Get the samples counter for instantiations of X,Y variables (normalized for X variables).
     */
    public Factor getSamplesCounterNormalized() {
        return this.getSamplesCounter().normalizeByFirstNVariables(this.sampleProducer.XVars.length);
    }
}
