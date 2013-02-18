// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/18

package bna.bnlib;

import java.util.ArrayList;

/**
 * Concrete implementation of sampler for Markov chain Monte-Carlo sampling.
 */
public class BayesianNetworkMCMCSampler extends BayesianNetworkSampler {
    private ArrayList<MCMCResamplingAction> resamplingActions = new ArrayList<>();
    
    public BayesianNetworkMCMCSampler(BayesianNetwork bn, Variable[] X, Variable[] Y, Variable[] E, int[] e) {
        super(bn, X, Y, E, e);
        this.generateResamplingActions();
    }
    
    private void generateResamplingActions() {
        for(Variable V : this.sampledVars) {
            if(!Toolkit.arrayContains(this.EVars, V)) {
                // we can resample any variable except evidence variables which
                // doesn't make sense
                MCMCResamplingAction action = new MCMCResamplingAction(this.sampledVars, V);
                this.resamplingActions.add(action);
            }
        }
    }

    /** Initializes the sample by a single weighted sampling pass. */
    @Override
    protected void initializeSample(int[] sampledVarsValues) {
        BayesianNetworkWeightedSampler weightedSampler = new BayesianNetworkWeightedSampler(
                this.bn, this.XVars, this.YVars, this.EVars, this.EVals);
        // the variable values are preserved after the following call
        weightedSampler.produceSample(sampledVarsValues);
    }

    /** Select a single resampling action at random and execute it. */
    @Override
    protected double produceSample(int[] sampledVarsValues) {
        int actionIndex = this.rand.nextInt(this.resamplingActions.size());
        MCMCResamplingAction action = this.resamplingActions.get(actionIndex);
        action.resample(sampledVarsValues);
        return 1.0; // MCMC doesn't really use weights for samples
    }

}



class MCMCResamplingAction {
    Variable[] MBVars; // Markov blanket of the variable to resample
    int[] MBVarIndexInSampledVars; // indices of variables from Markov blanket in the sampledVars array
    VariableSubsetMapper[] sampledVarsToMBVarParentsMappers; // for each variable of Markov blanket we have a mapper of sampledVars to Parents(MBVar)
    
    
    public MCMCResamplingAction(Variable[] sampledVars, Variable resampledVar) {
        // cache Markov blanket of resampledVar
        // for each variable Vmb in Markov blanket compute its index in sampledVars and a mapper sampledVars -> Parents(variable)
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void resample(int[] sampledVarsValues) {
        // for each possible assignmnet of variable resampledVar do:
        //     put the value in sampledVarsValues vector
        //     prob[assignment] = 1.0
        //     for each variable V in Markov blanket of variable resampledVar do:
        //         prob[assignment] *= probability of current assignment to V (according to sampledVars) given it's parents
        // resample resampledVar by prob vector and put the resampled value to sampledVarsValues
        throw new UnsupportedOperationException("Not supported yet.");
    }
}