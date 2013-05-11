// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/18

package bna.bnlib.sampling;

import bna.bnlib.*;
import java.util.Collection;
import java.util.LinkedList;


/**
 * Multithreaded implementation of QuerySampler.
 * In order for the sampling to be efficient and correct, we need to have
 * a Random object for each thread separately. That is easily accomplished
 * by using java.util.concurrent.ThreadLocalRandom.
 */
public class QuerySamplerMultithreaded implements SamplerInterface {
    private SampleProducer sharedSampleProducer;
    private int threadcount;
    private Factor sampleCounter;
    
    
    /**
     * Create new sampler that uses theadcount parallel threads sharing the given sample producer.
     * @throws BNLibIllegalArgumentException When the threadcount or sharedSampleProducer is invalid.
     */
    public QuerySamplerMultithreaded(SampleProducer sharedSampleProducer, int threadcount) throws BNLibIllegalArgumentException {
        if(threadcount <= 0)
            throw new BNLibIllegalArgumentException("Number of threads must be non-negative.");
        if(sharedSampleProducer == null)
            throw new BNLibIllegalArgumentException("The sample producer may not be null");
        
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
        LinkedList<SamplingThread> threadpool = new LinkedList<SamplingThread>();
        for(int i = 0 ; i < this.threadcount ; i++) {
            SamplingThread threadI = new SamplingThread() {
                @Override
                public void run() {
                    QuerySampler querySampler = new QuerySampler(sharedSampleProducer);
                    querySampler.sample(sharedController);
                    this.sampleCounter = querySampler.getSamplesCounter(); // store samples of this thread
                }
            };
            threadpool.add(threadI);
        }
        // run all worker threads
        for(Thread t : threadpool)
            t.start();
        // wait for workers one by one
        LinkedList<SamplingThread> threadpoolWorking = new LinkedList<SamplingThread>(threadpool);
        while(!threadpoolWorking.isEmpty()) {
            try {
                threadpoolWorking.getFirst().join();
                threadpoolWorking.removeFirst();
            }
            catch(InterruptedException iex) {
                // on interrupt of the main thread stop the workers and combine
                // immediate results of worker threads
                controller.setStopFlag();
                // now the workers will finish quickly
            }
        }
        // combine the results of all threads
        this.sampleCounter = this.combineResults(threadpool);
    }
    
    private Factor combineResults(Collection<SamplingThread> threads) {
        // extract the subresults as factors
        Factor[] subresults = new Factor[threads.size()];
        int i = 0;
        for(SamplingThread t : threads) {
            subresults[i++] = t.sampleCounter;
        }
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