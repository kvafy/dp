// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/09

package bna.bnlib.sampling;


/**
 * To control the sampling process, possibly in context of more threads.
 */
public class SamplingController {
    private Long maxSamples;
    private boolean stopFlag;
    
    
    /** Create controller of sampling possibly limited by samples count (if not null). */
    public SamplingController(Long maxSamples) {
        this.maxSamples = maxSamples;
        this.stopFlag = false;
    }
    
    public boolean shouldStop(long currentSample) {
        return this.stopFlag || (this.maxSamples != null && currentSample >= this.maxSamples);
    }
    
    public void setStopFlag() {
        this.stopFlag = true;
    }
}
