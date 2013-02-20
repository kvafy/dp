// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/18

package bna.bnlib.sampling;

import bna.bnlib.*;


/**
 * Multithreaded implementation of QuerySampler.
 * In order for the sampling to be efficient and correct, we need to have
 * a Random object for each thread separately. That is easily accomplished
 * by using java.util.concurrent.ThreadLocalRandom.
 */
public class QuerySamplerMultithreaded implements Sampler {
    private SampleProducer sharedSampleProducer;
    private int threadcount;
    private Factor sampleCounter;
    
    
    public QuerySamplerMultithreaded(SampleProducer sharedSampleProducer, int threadcount) {
        if(threadcount <= 0)
            throw new BayesianNetworkRuntimeException("Number of threads must be non-negative.");
        
        this.sharedSampleProducer = sharedSampleProducer;
        this.threadcount = threadcount;
    }

    /** A sampling thread which stores results of sampling in an instance variable. */
    class SamplingThread extends Thread {
        public Factor sampleCounter;
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
                    QuerySampler querySampler = new QuerySampler(sharedSampleProducer);
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
        return this.sampleCounter.normalizeByFirstNVariables(this.sharedSampleProducer.XVars.length);
    }
}