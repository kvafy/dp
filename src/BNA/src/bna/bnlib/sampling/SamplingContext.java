// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/25

package bna.bnlib.sampling;

import java.util.Random;


/**
 * Context of a SampleProducer (thread-local) - defines current state by last sample.
 * Instance of this class is used to carry a single sample produced by
 * a SampleProducer.
 */
public class SamplingContext {
    /** weight of the sample */
    double sampleWeight;
    /** current assignment of all variables whose assignment needs to be maintained */
    final int[] sampledVarsAssignment;
    /** current assignment of variables (X union Y) */
    final int[] XYVarsAssignment;
    /** thread-local Random object */
    final Random rand;
    
    
    public SamplingContext(int[] sampledVarsAssignment, int[] XYVarsAssignment, Random rand) {
        sampleWeight = 1.0;
        this.sampledVarsAssignment = sampledVarsAssignment;
        this.XYVarsAssignment = XYVarsAssignment;
        this.rand = rand;
    }
}
