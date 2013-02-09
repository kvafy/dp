// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/08

package bna.bnlib;

import java.util.Arrays;

/**
 * Generate saples for P(X | Y, E = e).
 * This is a general sampler using the template method design pattern; usable
 * for weighted sampling as well as for MCMC.
 */
public abstract class BayesianNetworkSampler {
    protected BayesianNetwork bn;
    private Variable[] sampleVars;     // X union Y
    protected Variable[] evidenceVars; // E
    protected int[] evidenceValues;    // which values should "E" variables have
    private AssignmentIndexMapper sampleMapper; // mapping of assignment sampleVars to index in sampleCounter
    private double[] sampleCounter; // for all instantiations of X,Y
    
    public BayesianNetworkSampler(BayesianNetwork bn, Variable[] XY, Variable[] E, int[] e) {
        // validate inputs
        Variable[] allVars = bn.getVariables();
        if(!Toolkit.isSubset(allVars, XY) || !Toolkit.isSubset(allVars, E) || !Toolkit.areDisjoint(XY, E))
            throw new BayesianNetworkRuntimeException("Invalid variables specified.");
        
        this.bn = bn;
        this.sampleVars = XY;
        this.evidenceVars = E;
        this.evidenceValues = e;
        // initialize sampleCounter
        this.sampleMapper = new AssignmentIndexMapper(this.sampleVars);
        this.sampleCounter = new double[Toolkit.cardinality(XY)];
        for(int i = 0 ; i < this.sampleCounter.length ; i++)
            this.sampleCounter[i] = 0.0;
    }
    
    /** Record a sample with given weight. */
    private void registerSample(int[] sampleVarsValues, double sampleWeight) {
        int assignmentIndex = this.sampleMapper.assignmentToIndex(sampleVarsValues);
        this.sampleCounter[assignmentIndex] += sampleWeight;
    }
    
    /** Perform sampling according to given controller. */
    public void sample(SamplingController controller) {
        Variable[] allVarsSorted = this.bn.topologicalSort();
        VariableSubsetMapper allVarsToSampleVarsMapper = new VariableSubsetMapper(allVarsSorted, this.sampleVars);
        
        int[] allVarsValues = new int[bn.getVariablesCount()]; // to this array variables are sampled
        int[] sampleVarsValues = new int[this.sampleVars.length];
        
        int sample = 0;
        this.initializeSample(allVarsValues);
        while(!controller.stopFlag() && sample < controller.maxSamples()) {
            double sampleWeight = this.sample(allVarsValues);
            allVarsToSampleVarsMapper.map(allVarsValues, sampleVarsValues);
            this.registerSample(sampleVarsValues, sampleWeight);
            sample++;
        }
    }
    
    /**
     * Get the samples counter for instantiations of X,Y variables.
     * Mapping between counter index and X,Y instantiation is defined by
     * sampleMapper accessible via getSamplesAssignmentIndexMapper() method.
     * @return 
     */
    public double[] getSamplesCounter() {
        return Arrays.copyOf(this.sampleCounter, this.sampleCounter.length);
    }
    
    public AssignmentIndexMapper getSamplesAssignmentIndexMapper() {
        return this.sampleMapper;
    }
    
    
    // Template method for weighted sampling / MCMC
    
    protected abstract void initializeSample(int[] allVarsValues);
    
    /**
     * Read values of currently assigned variables, write the one sampled and
     * and return weight change.
     * @param allVarsValues
     * @return 
     */
    protected abstract double sample(int[] allVarsValues);
}
