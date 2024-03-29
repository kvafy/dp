// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/01

package bna.bnlib.sampling;

import bna.bnlib.*;


/**
 * Generates samples from the distribution P(X | Y, E = e) and subclass
 * defines what to do with them.
 * Template method design pattern is used.
 */
public abstract class Sampler implements SamplerInterface {
    protected SampleProducer sampleProducer;
    /** Assignment of these variables comes from the sampleProducer to registerSample() method. */
    protected Variable[] XYVars;
    
    
    /** Create a generic sampler that user sampleProducer to get a single sample. */
    public Sampler(SampleProducer sampleProducer) {
        this.sampleProducer = sampleProducer;
        this.XYVars = this.sampleProducer.XYVars;
    }
    
    /** Perform sampling that can be stopped given controller. */
    @Override
    public final void sample(SamplingController controller) {
        this.presamplingActions();
        
        SamplingContext context = this.sampleProducer.createSamplingContext();
        long sampleNumber = 0;
        this.sampleProducer.initializeSample(context);
        
        while(!controller.shouldStop(sampleNumber)) {
            this.sampleProducer.produceSample(context);
            this.registerSample(context.XYVarsAssignment, context.sampleWeight);
            sampleNumber++;
        }
        
        this.postsamplingActions();
    }
    
    
    // template method pattern: abstract methods used in the sample(...) method
    
    /** A sample has just been generated, subclass, do something with it. */
    protected abstract void registerSample(int[] XYVarsValues, double sampleWeight);
    
    /** We may prepare for incomming samples, eg. open a file. */
    protected abstract void presamplingActions();
    
    /** Perform actions associated with finished sampling, eg. close a file. */
    protected abstract void postsamplingActions();
}
