/*
 * // Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
 * // Author:  David Chaloupka (xchalo09)
 * // Created: 2013/xx/xx
 */

package bna.bnlib;

/**
 * To control the sampling process, possibly in context of more threads.
 */
public class SamplingController {
    private int maxSamples;
    private boolean stopFlag;
    
    public SamplingController(int maxSamples) {
        this.maxSamples = maxSamples;
        this.stopFlag = false;
    }
    
    public int maxSamples() {
        return this.maxSamples;
    }
    
    public boolean stopFlag() {
        return this.stopFlag;
    }
    
    public void setStopFlag() {
        this.stopFlag = true;
    }

}
