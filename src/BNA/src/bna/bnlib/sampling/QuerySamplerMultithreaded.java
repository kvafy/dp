// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/18

package bna.bnlib.sampling;

import bna.bnlib.*;


/**
 * Multithreaded implementation of QuerySampler.
 * In order for the sampling to be efficient, we need to create a sample producer
 * for each thread, so that they will not share their Random objects. This is
 * imperative:
 *    (1) For performance (aproximately 3x faster than with a shared Random object)
 *    (2) For result validity (java.util.Random doesn't appear to be threadsafe)
 */
public class QuerySamplerMultithreaded implements Sampler {
    private SampleProducer sampleProducer;
    private int threadcount;
    private Factor sampleCounter;
    
    
    public QuerySamplerMultithreaded(SampleProducer sampleProducer, int threadcount) {
        if(threadcount <= 0)
            throw new BayesianNetworkRuntimeException("Number of threads must be non-negative.");
        
        this.sampleProducer = sampleProducer;
        this.threadcount = threadcount;
    }

    @Override
    public void sample(SamplingController controller) {
        // initialize threads
        final SamplingController sharedController = controller;
        SamplingThread[] threadpool = new SamplingThread[this.threadcount];
        for(int i = 0 ; i < this.threadcount ; i++) {
            SamplingThread threadI = new SamplingThread() {
                @Override
                public void run() {
                    QuerySampler querySampler = new QuerySampler(sampleProducer.cloneWithNewRandomObject());
                    querySampler.sample(sharedController);
                    this.sampleCounter = querySampler.getSamplesCounter(); // store samples of this thread
                }
            };
            threadpool[i] = threadI;
        }
        // run all threads
        for(Thread t : threadpool)
            t.start();
        for(Thread t : threadpool) {
            try {
                t.join();
            }
            catch(InterruptedException iex) {
                iex.printStackTrace(); // TODO better?
            }
        }
        // combine the results of all threads
        this.sampleCounter = this.combineResults(threadpool);
    }
    
    private Factor combineResults(SamplingThread[] threads) {
        // extract the subresults as factors
        Factor[] subresults = new Factor[threads.length];
        for(int i = 0 ; i < threads.length ; i++)
            subresults[i] = threads[i].sampleCounter;
        // combine to produce the final result
        return Factor.sumFactors(subresults);
    }
    
    /**
     * Get the samples counter for instantiations of X,Y variables (just raw counters).
     */
    public Factor getSamplesCounter() {
        return this.sampleCounter;
    }
    
    /**
     * Get the samples counter for instantiations of X,Y variables (normalized for X variables).
     */
    public Factor getSamplesCounterNormalized() {
        return this.sampleCounter.normalizeByFirstNVariables(this.sampleProducer.XVars.length);
    }
}



/** A thread which stores results of sampling in an instance variable. */
class SamplingThread extends Thread {
    public Factor sampleCounter;
}