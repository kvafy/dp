// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/01

package bna.bnlib.learning;


/**
 * Via this object user can instantly stop learning process and set the maximal number of iterations.
 */
public class LearningController {
    private Long maxIterations;
    private boolean stopFlag;
    
    
    /** Create controller of structure learning possibly limited by iterations count (if not null). */
    public LearningController(Long maxIterations) {
        this.maxIterations = maxIterations;
        this.stopFlag = false;
    }
    
    /**
     * Returns true if the learning in current iteration should be stopped.
     * There are two reasons for stopping: stop flag is set or the maximum
     * number of iterations has been exceeded.
     */
    public boolean shouldStop(long currentIteration) {
        return this.stopFlag || (this.maxIterations != null && currentIteration >= this.maxIterations);
    }
    
    /** Is the stop flag set?. */
    public boolean getStopFlag() {
        return this.stopFlag;
    }
    
    /** Set the stop flag. */
    public void setStopFlag() {
        this.stopFlag = true;
    }
}
