// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/08

package bna.bnlib.sampling;

import bna.bnlib.*;
import java.util.Arrays;


/**
 * Generates samples for a probabilistic query P(X | Y, E = e) and computes the statistics as a factor with scope (X union Y).
 */
public class QuerySampler extends Sampler {
    // sampling statistics
    private AssignmentIndexMapper XYIndexMapper; // mapping of assignment XYVars to index into the array sampleCounter
    private double[] XYSampleCounter;            // for all instantiations of X,Y (ie. of this.XYVars)
    
    public QuerySampler(SampleProducer sampleProducer) {
        super(sampleProducer);
        // initialize sampleCounter
        this.XYIndexMapper = new AssignmentIndexMapper(this.XYVars);
        this.XYSampleCounter = new double[Toolkit.cardinality(this.XYVars)];
        Arrays.fill(this.XYSampleCounter, 0.0);
    }
    
    
    /** Record a sampleNumber with given weight in our statistics. */
    @Override
    protected void registerSample(int[] XYVarsValues, double sampleWeight) {
        int assignmentIndex = this.XYIndexMapper.assignmentToIndex(XYVarsValues);
        this.XYSampleCounter[assignmentIndex] += sampleWeight;
    }
    
    @Override
    protected void presamplingActions() {
        // no need to do anything
    }

    @Override
    protected void postsamplingActions() {
        // no need to do anything
    }
    
    /**
     * Get the samples counter for instantiations of X,Y variables (just raw counters).
     */
    public Factor getSamplesCounter() {
        return new Factor(this.XYVars, XYSampleCounter);
    }
    
    /**
     * Get the samples counter for instantiations of X,Y variables (normalized for X variables).
     */
    public Factor getSamplesCounterNormalized() {
        return this.getSamplesCounter().normalizeByFirstNVariables(this.sampleProducer.XVars.length);
    }
}
