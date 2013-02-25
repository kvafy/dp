// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/08

package bna.bnlib.sampling;

import bna.bnlib.*;
import java.util.Arrays;


/**
 * Generates samples for a probabilistic query P(X | Y, E = e) and computes the statistics as a factor with scope (X union Y).
 */
public class QuerySampler implements Sampler {
    private SampleProducer sampleProducer;
    // sampling statistics
    private AssignmentIndexMapper XYIndexMapper; // mapping of assignment XYVars to index to the array sampleCounter
    private double[] sampleCounter;              // for all instantiations of X,Y (ie. of sampleProducer.XYVars)
    
    public QuerySampler(SampleProducer sampleProducer) {
        this.sampleProducer = sampleProducer;
        // initialize sampleCounter
        this.XYIndexMapper = new AssignmentIndexMapper(this.sampleProducer.XYVars);
        this.sampleCounter = new double[Toolkit.cardinality(this.sampleProducer.XYVars)];
        Arrays.fill(this.sampleCounter, 0.0);
    }
    
    
    /** Record a sampleNumber with given weight. */
    private void registerSample(int[] XYVarsValues, double sampleWeight) {
        int assignmentIndex = this.XYIndexMapper.assignmentToIndex(XYVarsValues);
        this.sampleCounter[assignmentIndex] += sampleWeight;
    }
    
    /** Perform sampling according to given controller. */
    @Override
    public void sample(SamplingController controller) {
        SamplingContext context = this.sampleProducer.createSamplingContext();
        
        long sampleNumber = 0;
        this.sampleProducer.initializeSample(context);
        while(!controller.stopFlag() && sampleNumber < controller.maxSamples()) {
            this.sampleProducer.produceSample(context);
            this.registerSample(context.XYVarsAssignment, context.sampleWeight);
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
