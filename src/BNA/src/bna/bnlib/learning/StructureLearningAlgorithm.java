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
    protected ScoringMethod scoringMethod;
    
    public StructureLearningAlgorithm(ScoringMethod method) {
        this.scoringMethod = method;
    }
    
    public abstract BayesianNetwork learn(BayesianNetwork bnInitial, LearningController controller, StructuralConstraints constraints);
}
