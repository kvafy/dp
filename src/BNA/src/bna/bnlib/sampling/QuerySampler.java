// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/08

package bna.bnlib.sampling;

import bna.bnlib.*;


/**
 * Generates samples for a probabilistic query P(X | Y, E = e) and computes the statistics as a factor with scope (X union Y).
 */
public class QuerySampler extends Sampler {
    // sampling statistics
    private Counter XYCounter;
    
    public QuerySampler(SampleProducer sampleProducer) {
        super(sampleProducer);
        // initialize counter
        this.XYCounter = new Counter(this.XYVars);
    }
    
    
    /** Record a sampleNumber with given weight in our statistics. */
    @Override
    protected void registerSample(int[] XYVarsValues, double sampleWeight) {
        this.XYCounter.add(XYVarsValues, sampleWeight);
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
        return this.XYCounter.toFactor();
    }
    
    /**
     * Get the samples counter for instantiations of X,Y variables (normalized for X variables).
     */
    public Factor getSamplesCounterNormalized() {
        return this.getSamplesCounter().normalizeByFirstNVariables(this.sampleProducer.XVars.length);
    }
}
