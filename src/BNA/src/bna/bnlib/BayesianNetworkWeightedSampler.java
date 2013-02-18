// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/09

package bna.bnlib;

import java.util.Random;

/**
 * Concrete implementation of sampler for weighted sampling.
 * The sampler works as follows: We compute a topological sort of all variables
 * in the network and prune nodes that are not in X union Y union E nor any of
 * their descendants is in this set.
 * One same is produced by sampling all the variables left after pruning in
 * topological order, one variable at a time. Sampling action of a single
 * variable is implemented as an abstract WeightedSamplingSampleAction. So, we
 * perform all actions in samplingActions array in order to produce one sample.
 * The sampling action is dependent on whether it is an evidence variable or other:
 *  (a) evidence variable E: extract assignment to Parents(E) from the current
 *      sampledVarsValues and modify sample weight by returning potentially non-one
 *      double value (!= 1.0) from WeightedSamplingEvidenceSampleAction.produceSample(...).
 *      Also place the observed value of evidence to current assignment vector
 *      sampledVarsValues.
 *  (b) non-evidence variable X: extract assignment to Parents(X) and sample
 *      the variable X. Put the sampled value to current produceSample assignment
 *      sampledVarsValues.
 */
public class BayesianNetworkWeightedSampler extends BayesianNetworkSampler {
    private WeightedSamplingSampleAction[] samplingActions;

    public BayesianNetworkWeightedSampler(BayesianNetwork bn, Variable[] X, Variable[] Y, Variable[] E, int[] e) {
        super(bn, X, Y, E, e);
        // generate sampling actions for all variables that need to be sampled
        this.samplingActions = new WeightedSamplingSampleAction[this.sampledVars.length];
        for(int i = 0 ; i < this.sampledVars.length ; i++) {
            Variable varI = this.sampledVars[i];
            Node varINode = this.bn.getNode(varI.getName());
            VariableSubsetMapper allVarsToIParentsMapper = new VariableSubsetMapper(this.sampledVars, varINode.getParentVariables());
            WeightedSamplingSampleAction actionI;
            
            if(Toolkit.arrayContains(E, varI)) {
                // action: adjust produceSample weight and write the evidence value
                int varIValue = e[Toolkit.indexOf(E, varI)];
                // - map allVarsValues to Parents(ENode) values
                // - add known value of evidence variable => EValue,parents(EValue) assignment
                // - read probability of that assignment from factor for ENode
                actionI = new WeightedSamplingEvidenceSampleAction(varINode,
                                                                   Toolkit.indexOf(this.sampledVars, varI),
                                                                   varIValue,
                                                                   allVarsToIParentsMapper,
                                                                   this.rand);
            }
            else {
                // action: produceSample variable by it's parents current assignment
                // - map allVarsValues to Parents(X) values
                // - for assignment of parents produceSample value for X
                actionI = new WeightedSamplingVariableSampleAction(varINode,
                                                                   Toolkit.indexOf(this.sampledVars, varI),
                                                                   allVarsToIParentsMapper,
                                                                   this.rand);
            }
            this.samplingActions[i] = actionI;
        }
    }
    
    @Override
    protected void initializeSample(int[] sampledVarsValues) {
        // all the work is done in produceSample
    }

    /**
     * One produceSample is in weighted sampling produced by sampling all the variables
     * in the network whilst accumulating the produceSample weight according to evidence.
     * @param allVarsValues Assignment to all variables in the network.
     * @return Weight of the produceSample
     */
    @Override
    protected double produceSample(int[] sampledVarsValues) {
        double weight = 1.0;
        for(WeightedSamplingSampleAction action : this.samplingActions)
            weight *= action.sample(sampledVarsValues);
        return weight;
    }
}



/** The Command design pattern for sampling of all variables (evidence and non-evidence). */
abstract class WeightedSamplingSampleAction {
    protected Random rand;
    
    public WeightedSamplingSampleAction(Random rand) {
        this.rand = rand;
    }
    
    /**
     * Put value of a variable to the current produceSample allVarsValues and return
     * weight change, ie. 1.0 for a non-evidence variable and a general value
     * for an evidence variable.
     * @param allVarsValues Current produceSample (values of variables in the network
     *                      considered in topological order.
     * @return Weight change after sampling the variable.
     */
    public abstract double sample(int[] allVarsValues);
}

/**
 * "Sample" an evidence variable - return weight change and put the observed
 * evidence value to current produceSample.
 */
class WeightedSamplingEvidenceSampleAction extends WeightedSamplingSampleAction {
    private Node ENode;
    private int EValue;
    private int EIndex;
    private VariableSubsetMapper allVarsToEParentsMapper;
    
    public WeightedSamplingEvidenceSampleAction(Node evidenceNode, int evidenceVarIndex, int evidenceVal, VariableSubsetMapper allVarsToEParents, Random rand) {
        super(rand);
        this.ENode = evidenceNode;
        this.EValue = evidenceVal;
        this.EIndex = evidenceVarIndex;
        this.allVarsToEParentsMapper = allVarsToEParents;
    }
    
    @Override
    public double sample(int[] sampledVarsValues) {
        // for variable E determine assignment to Parents(E) from sampledVarsValues
        int[] parentsAssignment = this.allVarsToEParentsMapper.map(sampledVarsValues);
        int[] nodeAndParentsAssignment = new int[1 + parentsAssignment.length];
        nodeAndParentsAssignment[0] = this.EValue;
        System.arraycopy(parentsAssignment, 0, nodeAndParentsAssignment, 1, parentsAssignment.length);
        // determine weight change coefficient, ie. probability of P(E = e, parents(E))
        double weight = this.ENode.getProbability(nodeAndParentsAssignment);
        sampledVarsValues[this.EIndex] = this.EValue;
        return weight;
    }
}

/**
 * Sample a non-evidence variable by extracting assignment of it's parents,
 * sampling and placing the sampled value to current produceSample.
 */
class WeightedSamplingVariableSampleAction extends WeightedSamplingSampleAction {
    private Node XNode;
    private int XIndex;
    private VariableSubsetMapper allVarsToXParentsMapper;
    
    private int[] tmpXParentsAssignment; // to prevent frequent allocation during each produceSample(...)
    
    public WeightedSamplingVariableSampleAction(Node XNode, int XVarIndex, VariableSubsetMapper allVarsToXParents, Random rand) {
        super(rand);
        this.XNode = XNode;
        this.XIndex = XVarIndex;
        this.allVarsToXParentsMapper = allVarsToXParents;
        this.tmpXParentsAssignment = new int[XNode.getParentCount()];
    }

    @Override
    public double sample(int[] sampledVarsValues) {
        this.allVarsToXParentsMapper.map(sampledVarsValues, this.tmpXParentsAssignment);
        int XVal = this.XNode.sampleVariable(this.tmpXParentsAssignment, this.rand);
        sampledVarsValues[this.XIndex] = XVal;
        return 1.0; // no weight change
    }
}