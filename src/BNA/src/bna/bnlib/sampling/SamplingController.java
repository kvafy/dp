// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/09

package bna.bnlib.sampling;


/**
 * Controls the sampling process, possibly in context of more threads.
 * The termination criteria are: (a) stopFlag is set, (b) maximum number of
 * iterations has been reached.
 */
public class SamplingController {
    private Long maxSamples;
    private boolean stopFlag;
    
    
    /** Create controller of sampling possibly limited by samples count (if not null). */
    public SamplingController(Long maxSamples) {
        this.maxSamples = maxSamples;
        this.stopFlag = false;
    }
    
    /** Should sampling stop now? */
    public boolean shouldStop(long currentSample) {
        return this.stopFlag || (this.maxSamples != null && currentSample >= this.maxSamples);
    }
    
    /** Is the stopFlag set? */
    public boolean getStopFlag() {
        return this.stopFlag;
    }
    
    /** Set the stopFlag. */
    public void setStopFlag() {
        this.stopFlag = true;
    }
}
