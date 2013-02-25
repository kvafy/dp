// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/09

package bna.bnlib.sampling;

/**
 * To control the sampling process, possibly in context of more threads.
 */
public class SamplingController {
    private long maxSamples;
    private boolean stopFlag;
    
    public SamplingController(long maxSamples) {
        this.maxSamples = maxSamples;
        this.stopFlag = false;
    }
    
    public long maxSamples() {
        return this.maxSamples;
    }
    
    public boolean stopFlag() {
        return this.stopFlag;
    }
    
    public void setStopFlag() {
        this.stopFlag = true;
    }
}
