// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/08

package bna.bnlib;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Generate saples for P(X | Y, E = e).
 * This is a general sampler using the template method design pattern; usable
 * for weighted sampling as well as for MCMC.
 */
public abstract class BayesianNetworkSampler {
    protected BayesianNetwork bn;
    private Variable[] XVars, YVars;
    private Variable[] XYVars;     // XVars union YVars
    protected Variable[] evidenceVars; // E
    protected int[] evidenceValues;    // which values should "E" variables have
    protected Variable[] sampledVars;  // defines all variables that need to be sampled
                                       // and the sampling order
    // sampling statistics
    private AssignmentIndexMapper sampleMapper; // mapping of assignment XYVars to index in sampleCounter
    private double[] sampleCounter;             // for all instantiations of X,Y (ie. of XYVars)
    
    public BayesianNetworkSampler(BayesianNetwork bn, Variable[] X, Variable[] Y, Variable[] E, int[] e) {
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
        this.evidenceVars = E;
        this.evidenceValues = e;
        // initialize sampleCounter
        this.sampleMapper = new AssignmentIndexMapper(this.XYVars);
        this.sampleCounter = new double[Toolkit.cardinality(XY)];
        for(int i = 0 ; i < this.sampleCounter.length ; i++)
            this.sampleCounter[i] = 0.0;
        
        this.determineSamplingOrder();
    }
    
    /**
     * Sampling order must be topological order. Also we can optimize and
     * not to sample variables such that they are not in (X union Y union E)
     * and none of their descendants is in (X union Y union E).
     */
    private void determineSamplingOrder() {
        Variable[] XYE = Toolkit.union(this.XYVars, this.evidenceVars);
        Variable[] topsortedVariables = this.bn.topologicalSort();
        LinkedList<Variable> mustSampleVariables = new LinkedList<>();
        // optimization: omit leaf variables not in (X union Y union E)
        //               and apply recursively until some variable can be ommited
        for(int i = topsortedVariables.length - 1 ; i >= 0 ; i--) {
            Variable varI = topsortedVariables[i];
            Node varINode = this.bn.getNode(varI.getName());
            Variable[] varIChildren = varINode.getChildVariables();
            if(Toolkit.arrayContains(XYE, varI))
                mustSampleVariables.addFirst(varI);
            else if(!Toolkit.areDisjoint(mustSampleVariables, Arrays.asList(varIChildren)))
                mustSampleVariables.addFirst(varI);
        }
        // we are done, just copy result to this.sampledVars
        this.sampledVars = new Variable[mustSampleVariables.size()];
        mustSampleVariables.toArray(this.sampledVars);
        System.out.println(String.format("debug: #of variables really sampled is %d", this.sampledVars.length));
    }
    
    /** Record a sample with given weight. */
    private void registerSample(int[] sampleVarsValues, double sampleWeight) {
        int assignmentIndex = this.sampleMapper.assignmentToIndex(sampleVarsValues);
        this.sampleCounter[assignmentIndex] += sampleWeight;
    }
    
    /** Perform sampling according to given controller. */
    public void sample(SamplingController controller) {
        //Variable[] allVarsSorted = this.bn.topologicalSort();
        VariableSubsetMapper allVarsToSampleVarsMapper = new VariableSubsetMapper(this.sampledVars, this.XYVars);
        
        int[] sampledVarsValues = new int[this.sampledVars.length]; // to this array variables are sampled
        int[] XYVarsValues = new int[this.XYVars.length];
        
        int sample = 0;
        this.initializeSample(sampledVarsValues);
        while(!controller.stopFlag() && sample < controller.maxSamples()) {
            double sampleWeight = this.sample(sampledVarsValues);
            allVarsToSampleVarsMapper.map(sampledVarsValues, XYVarsValues);
            this.registerSample(XYVarsValues, sampleWeight);
            sample++;
        }
    }
    
    /**
     * Get the samples counter for instantiations of X,Y variables (just raw counters).
     */
    public Factor getSamplesCounter() {
        return new Factor(this.XYVars, sampleCounter);
    }
    
    /**
     * Get the samples counter for instantiations of X,Y variables (normalized for X variables).
     */
    public Factor getSamplesCounterNormalized() {
        return this.getSamplesCounter().normalizeByFirstNVariables(this.XVars.length);
    }
    
    
    // Template method for weighted sampling / MCMC
    
    protected abstract void initializeSample(int[] sampledVarsValues);
    
    /**
     * Read values of currently assigned variables, write the one sampled and
     * and return weight change.
     * @param allVarsValues
     * @return 
     */
    protected abstract double sample(int[] sampledVarsValues);
}
