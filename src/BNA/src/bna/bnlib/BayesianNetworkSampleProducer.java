// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/18

package bna.bnlib;

import java.util.Random;


/**
 * Abstract sampler that can produce a single sample.
 * Generate samples for computing the distribution P(X | Y, E = e).
 * This is a general sampler using the template method design pattern; usable
 * for weighted sampling and MCMC sampling.
 */
public abstract class BayesianNetworkSampleProducer {
    // !!! beware: members of this class are accessed within classes BayesianNetworkQuerySampler
    //             and BayesianNetworkDatasetCreationSampler, so be careful
    //             with changing their semantics
    protected BayesianNetwork bn;
    protected Variable[] XVars, YVars;
    protected Variable[] XYVars; // XVars union YVars
    protected Variable[] EVars; // evidence variables
    protected int[] EVals;      // concrete values of evidence variables
    protected Variable[] sampledVars;  // defines all variables for that we need
                                       // to keep track of their current assignment
                                       // and the sampling order of these variables
    protected Random rand = new Random();
    
    public BayesianNetworkSampleProducer(BayesianNetwork bn, Variable[] X, Variable[] Y, Variable[] E, int[] e) {
        Variable[] allVars = bn.getVariables();
        Variable[] XY = Toolkit.union(X, Y);
        // validate inputs
        if(!Toolkit.areDisjoint(X, Y) || !Toolkit.areDisjoint(XY, E)
                || !Toolkit.isSubset(allVars, XY) || !Toolkit.isSubset(allVars, E))
            throw new BayesianNetworkRuntimeException("Invalid variables specified.");
        
        this.bn = bn;
        this.XVars = X;
        this.YVars = Y;
        this.XYVars = XY;
        this.EVars = E;
        this.EVals = e;
        
        this.determineSamplingOrder();
    }
    
    /**
     * Most of the variables of a sample producer are read-only and created within constructor.
     * However the radnom generator cannot be effectively shared among multiple
     * threads, so we implement this method, that clones sample producer
     * which is guaranteed to have a brand new Radnom object.
     */
    public abstract BayesianNetworkSampleProducer cloneWithNewRandomObject();
    
    /**
     * Sampling order defines list of variables that need to be sampled.
     * The sampling order primarily has be topological order. Other criteria
     * are method-specific.
     */
    private void determineSamplingOrder() {
        Variable[] topsortedVariables = this.bn.topologicalSort();
        // optimization: certain sampling methods may allow for certain variables
        //               to be ommited in the sampling process (as if they
        //               we not present in the BN)
        Variable[] mustSampleVariables = this.filterVariablesToSample(topsortedVariables);
        this.sampledVars = mustSampleVariables;
    }
    
    
    // Template method pattern for weighted sampling / MCMC
    
    /**
     * Determine variables that really need to be sampled (this is default implementation).
     * The method must preserve relative order of given variables.
     */
    protected Variable[] filterVariablesToSample(Variable[] unfilteredSampledVariables) {
        Variable[] mustSampleVariables = unfilteredSampledVariables;
        return mustSampleVariables;
    }
    
    protected abstract void initializeSample(int[] sampledVarsValues);
    
    /**
     * Read values of currently assigned variables, write the one sampled and
     * and return weight change.
     * @param allVarsValues
     * @return 
     */
    protected abstract double produceSample(int[] sampledVarsValues);
}
