// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/11

package bna.bnlib.learning;

import bna.bnlib.BayesianNetwork;


/**
 * General interface of a structure learning algorithm that takes initial structure,
 * learning controller and learns the new network.
 */
public abstract class StructureLearningAlgorithm {
    protected DecomposableScoringMethod scoringMethod;
    
    
    /**
     * Creates an instance of the learning algorithm that uses the given scoring
     * for evaluation of candidate solutions.
     */
    public StructureLearningAlgorithm(DecomposableScoringMethod method) {
        this.scoringMethod = method;
    }
    
    /**
     * Runs the structure learning process.
     * @param bnInitial Structure to start the learning from.
     * @param controller Specifies the maximum number of iterations and can be
     *                   used to prematurely stop the learning process.
     * @param constraints Structural contraints that have to be met by the
     *                    resulting structure as well as by every immediate
     *                    structure during the structure learning.
     */
    public abstract BayesianNetwork learn(BayesianNetwork bnInitial,
                                          LearningController controller,
                                          StructuralConstraints constraints);
}
