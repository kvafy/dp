// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/21

package bna.view;


/**
 * Observer of the process of generating a layout for a network.
 */
public interface NetworkLayoutGeneratorObserver {
    public void notifyLayoutGeneratorStatus(long iteration, long maxIterations, double score, double scoreBest);
}
