// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/10

package bna.bnlib.learning;

import bna.bnlib.*;
import bna.bnlib.misc.Toolkit;
import org.apache.commons.math3.special.Gamma;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * Class implementing the Bayesian score with K2 prior.
 */
public class BayesianScoringMethod extends ScoringMethod {
    private DatasetInterface dataset;
    private double alpha; // equivalent sample size
    
    
    public BayesianScoringMethod(DatasetInterface dataset, double alpha) {
        this.dataset = dataset;
        this.alpha = alpha;
    }
    
    @Override
    public double absoluteScore(BayesianNetwork bn) {
        // the log P(D | G) part
        double log_P_D_given_G = 0;
        for(Node Xi : bn.getNodes()) {
            if(Xi.getParentCount() == 0) {
                Factor prior_Xi = this.getParameterPriorK2(Xi);
                Factor N_Xi = this.dataset.computeFactor(Xi.getScope());
                double alpha_Xi = 0;
                double N = 0;
                for(int assignment_x = 0 ; assignment_x < Xi.getVariable().getCardinality() ; assignment_x++) {
                    double alpha_x = prior_Xi.getProbability(assignment_x),
                           N_x = N_Xi.getProbability(assignment_x);
                    log_P_D_given_G += Gamma.logGamma(alpha_x + N_x) - Gamma.logGamma(alpha_x);
                    alpha_Xi += alpha_x;
                    N += N_x;
                }
                log_P_D_given_G += Gamma.logGamma(alpha_Xi) - Gamma.logGamma(alpha_Xi + N);
            }
            else {
                Factor prior_Xi_Pa = this.getParameterPriorK2(Xi);
                Factor N_Xi_Pa = this.dataset.computeFactor(Xi.getScope());
                int[] assignment_x_pa = new int[Xi.getScope().length];
                for(int[] assignment_pa : new Factor(Xi.getParentVariables(), 0)) { // create a factor of parents just to iterate over it
                    System.arraycopy(assignment_pa, 0, assignment_x_pa, 1, assignment_pa.length);
                    double alpha_Xi_pa = 0;
                    double N_pa = 0;
                    for(int assignment_x = 0 ; assignment_x < Xi.getVariable().getCardinality() ; assignment_x++) {
                        assignment_x_pa[0] = assignment_x;
                        double alpha_x_pa = prior_Xi_Pa.getProbability(assignment_x_pa),
                               N_x_pa = N_Xi_Pa.getProbability(assignment_x_pa);
                        log_P_D_given_G += Gamma.logGamma(alpha_x_pa + N_x_pa) - Gamma.logGamma(alpha_x_pa);
                        alpha_Xi_pa += alpha_x_pa;
                        N_pa += N_x_pa;
                    }
                    log_P_D_given_G += Gamma.logGamma(alpha_Xi_pa) - Gamma.logGamma(alpha_Xi_pa + N_pa);
                }
            }
        }
        // the dimension part
        final double C = 0.9;
        double dim = bn.getEdgeCount();
        double logStructureScore = dim * Math.log(C); // log(C ^ dim)
        return log_P_D_given_G + logStructureScore;
    }
    
    @Override
    public double deltaScore(BayesianNetwork bn, AlterationAction action) throws BNLibIllegalStructuralModificationException {
        /*double mutualInformationChange = this.getMutualInformationTermChange(bn, action);
        double dimensionTermChange = this.getDimensionTermChange(bn, action);
        return mutualInformationChange - dimensionTermChange;*/
        double origScore = this.absoluteScore(bn);
        action.apply(bn);
        double newScore = this.absoluteScore(bn);
        action.undo(bn);
        return newScore - origScore;
    }
    
    @Override
    public void notifyNetworkAlteration(AlterationAction actionTaken) {
    }
    
    private Factor getParameterPriorK2(Node n) {
        Variable[] scope = n.getScope();
        int noAssignmentsOfX = scope[0].getCardinality();
        return new Factor(scope, this.alpha / noAssignmentsOfX);
        //int noAssignmentsOfParents = Toolkit.cardinality(scope) / scope[0].getCardinality();
        //return new Factor(scope, this.alpha / noAssignmentsOfParents);
    }
}
