// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/18

package bna.bnlib.sampling;

/**
 * The most general sampler of a BN.
 */
public interface SamplerInterface {

    public void sample(SamplingController controller);
}
