// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/24

package bna.bnlib.sampling;


/**
 * Observer of a sampling process.
 */
public interface QuerySamplerObserver {
    public void notifySample();
}
