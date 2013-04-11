// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/10

package bna.bnlib.learning;

import bna.bnlib.*;
import bna.bnlib.misc.Toolkit;
import org.apache.commons.math3.special.Gamma;


/**
 * Class implementing the Bayesian score with a uniform discrete BDe prior.
 * Super-class takes care of caching of delta family scores.
 */
public class BayesianScoringMethod extends DecomposableScoringMethod {
    private double alpha; // equivalent sample size for BDe discrete unifor prior
    
    
    public BayesianScoringMethod(DatasetInterface dataset, double alpha) {
        super(dataset);
        this.alpha = alpha;
    }
    
    @Override
    public double absoluteScore(BayesianNetwork bn) {
        // the log P(D | G) part
        // (notation is consistent with the formulas presented in thesis)
        double log_P_D_given_G = 0;
        for(Node Xi : bn.getNodes())
            log_P_D_given_G += this.computeLogXi_given_D_G(Xi);
        // the dimension part log P(G)
        double log_P_G = this.computeLog_P_G(bn);
        return log_P_D_given_G + log_P_G;
    }
    
    private double computeLogXi_given_D_G(Node Xi) {
        return this.computeFamilyScore(Xi);
    }
    
    @Override
    protected double computeFamilyScore(Node Xi) {
        // the log P(X | G)
        // (notation is consistent with the formulas presented in thesis)
        double log_P_X_given_G = 0;
        if(Xi.getParentCount() == 0) {
            Factor prior_Xi = this.getParameterPriorBDEUniform(Xi);
            Factor N_Xi = this.dataset.computeFactor(Xi.getScope());
            double alpha_Xi = 0;
            double N = 0;
            for(int assignment_x = 0 ; assignment_x < Xi.getVariable().getCardinality() ; assignment_x++) {
                double alpha_x = prior_Xi.getProbability(assignment_x),
                        N_x = N_Xi.getProbability(assignment_x);
                log_P_X_given_G += Gamma.logGamma(alpha_x + N_x) - Gamma.logGamma(alpha_x);
                alpha_Xi += alpha_x;
                N += N_x;
            }
            log_P_X_given_G += Gamma.logGamma(alpha_Xi) - Gamma.logGamma(alpha_Xi + N);
        }
        else {
            Factor prior_Xi_Pa = this.getParameterPriorBDEUniform(Xi);
            Factor N_Xi_Pa = this.dataset.computeFactor(Xi.getScope());
            int[] assignment_x_pa = new int[Xi.getScope().length];
            for(int[] assignment_pa : new Factor(Xi.getParentVariables(), Double.NaN)) { // create a factor of parents just to iterate over it
                System.arraycopy(assignment_pa, 0, assignment_x_pa, 1, assignment_pa.length);
                double alpha_Xi_pa = 0;
                double N_pa = 0;
                for(int assignment_x = 0 ; assignment_x < Xi.getVariable().getCardinality() ; assignment_x++) {
                    assignment_x_pa[0] = assignment_x;
                    double alpha_x_pa = prior_Xi_Pa.getProbability(assignment_x_pa),
                            N_x_pa = N_Xi_Pa.getProbability(assignment_x_pa);
                    log_P_X_given_G += Gamma.logGamma(alpha_x_pa + N_x_pa) - Gamma.logGamma(alpha_x_pa);
                    alpha_Xi_pa += alpha_x_pa;
                    N_pa += N_x_pa;
                }
                log_P_X_given_G += Gamma.logGamma(alpha_Xi_pa) - Gamma.logGamma(alpha_Xi_pa + N_pa);
            }
        }
        return log_P_X_given_G;
    }
    
    @Override
    protected double computeIncreaseOfComplexityPenalty(BayesianNetwork bn, AlterationAction action) throws BNLibIllegalStructuralModificationException {
        double dimCurrent = this.computeLog_P_G(bn);
        action.apply(bn);
        double dimNew = this.computeLog_P_G(bn);
        action.undo(bn);
        return dimNew - dimCurrent;
    }
    
    private Factor getParameterPriorBDEUniform(Node n) {
        Variable[] scope = n.getScope();
        // BDe prior as if the prior network were discrete (without any edge)
        // and all the variables had a uniform distribution
        // => ensures the same Bayesian score of I-equivalent structures
        return new Factor(scope, this.alpha / Toolkit.cardinality(scope));
    }
    
    private double computeLog_P_G(BayesianNetwork bn) {
        final double C = 0.9;
        double dim = bn.getEdgeCount();
        double logStructureScore = dim * Math.log(C); // log(C ** dim)
        return logStructureScore;
    }
}
