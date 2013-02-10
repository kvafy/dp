// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/09

package bna.bnlib;

import java.util.Random;

/**
 * Concrete sampler for weithted sampling.
 * The sampler works as follows: One sample is produced by sampling all the
 * variables in the network. So, we perform all actions from samplingActions
 * in order to produce one sample. samplingActions contains an action for each
 * network variable when variables are considered in topological order.
 * The action is dependent on whether it is an evidence variable or other:
 *  (a) evidence variable E: extract assignment to Parents(E) from the current
 *      sample and modify sample weight by returning potentially non-one
 *      double value from WeightedSamplingEvidenceSampleAction.sample(...).
 *      Also place the observed value of evidence to current assignment vector.
 *  (b) non-evidence variable X: extract assignment to Parents(X) and sample
 *      the variable X. Put the sampled value to current sample assignment.
 */
public class BayesianNetworkWeightedSampler extends BayesianNetworkSampler {
    private WeightedSamplingSampleAction[] samplingActions;

    public BayesianNetworkWeightedSampler(BayesianNetwork bn, Variable[] X, Variable[] Y, Variable[] E, int[] e) {
        super(bn, X, Y, E, e);
        Random rand = new Random();
        // generate sampling actions
        this.samplingActions = new WeightedSamplingSampleAction[this.sampledVars.length];
        for(int i = 0 ; i < this.sampledVars.length ; i++) {
            Variable varI = this.sampledVars[i];
            Node varINode = this.bn.getNode(varI.getName());
            VariableSubsetMapper allVarsToIParentsMapper = new VariableSubsetMapper(this.sampledVars, varINode.getParentVariables());
            WeightedSamplingSampleAction actionI;
            
            if(Toolkit.arrayContains(E, varI)) {
                // action: adjust sample weight and write the evidence value
                int varIValue = e[Toolkit.indexOf(E, varI)];
                // - map allVarsValues to Parents(ENode) values
                // - add known value of evidence variable => EValue,parents(EValue) assignment
                // - read probability of that assignment from factor for ENode
                actionI = new WeightedSamplingEvidenceSampleAction(varINode,
                                                                   Toolkit.indexOf(this.sampledVars, varI),
                                                                   varIValue,
                                                                   allVarsToIParentsMapper,
                                                                   rand);
            }
            else {
                // action: sample variable by it's parents current assignment
                // - map allVarsValues to Parents(X) values
                // - for assignment of parents sample value for X
                actionI = new WeightedSamplingVariableSampleAction(varINode,
                                                                   Toolkit.indexOf(this.sampledVars, varI),
                                                                   allVarsToIParentsMapper,
                                                                   rand);
            }
            this.samplingActions[i] = actionI;
        }
    }
    
    @Override
    protected void initializeSample(int[] sampledVarsValues) {
    }

    /**
     * One sample is in weighted sampling produced by sampling all the variables
     * in the network whilst accumulating the sample weight according to evidence.
     * @param allVarsValues Assignment to all variables in the network.
     * @return Weight of the sample
     */
    @Override
    protected double sample(int[] sampledVarsValues) {
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
     * Put value of a variable to the current sample allVarsValues and return
     * weight change, ie. 1.0 for a non-evidence variable and a general value
     * for an evidence variable.
     * @param allVarsValues Current sample (values of variables in the network
     *                      considered in topological order.
     * @return Weight change after sampling the variable.
     */
    public abstract double sample(int[] allVarsValues);
}

/**
 * "Sample" an evidence variable - return weight change and put the observed
 * evidence value to current sample.
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
    public double sample(int[] allVarsValues) {
        // for variable E determine assignment to Parents(E) from allVarsValues
        int[] parentsAssignment = this.allVarsToEParentsMapper.map(allVarsValues);
        int[] nodeAndParentsAssignment = new int[1 + parentsAssignment.length];
        nodeAndParentsAssignment[0] = this.EValue;
        System.arraycopy(parentsAssignment, 0, nodeAndParentsAssignment, 1, parentsAssignment.length);
        // determine weight change coefficient, ie. probability of P(E = e, parents(E))
        double weight = this.ENode.getProbability(nodeAndParentsAssignment);
        allVarsValues[this.EIndex] = this.EValue;
        return weight;
    }
}

/**
 * Sample a non-evidence variable by extracting assignment of it's parents,
 * sampling and placing the sampled value to current sample.
 */
class WeightedSamplingVariableSampleAction extends WeightedSamplingSampleAction {
    private Node XNode;
    private int XIndex;
    private VariableSubsetMapper allVarsToXParentsMapper;
    
    private int[] tmpXParentsAssignment; // to prevent frequent allocation during each sample(...)
    
    public WeightedSamplingVariableSampleAction(Node XNode, int XVarIndex, VariableSubsetMapper allVarsToXParents, Random rand) {
        super(rand);
        this.XNode = XNode;
        this.XIndex = XVarIndex;
        this.allVarsToXParentsMapper = allVarsToXParents;
        this.tmpXParentsAssignment = new int[XNode.getParentCount()];
    }

    @Override
    public double sample(int[] allVarsValues) {
        this.allVarsToXParentsMapper.map(allVarsValues, this.tmpXParentsAssignment);
        int XVal = this.XNode.sampleVariable(this.tmpXParentsAssignment, this.rand);
        allVarsValues[this.XIndex] = XVal;
        return 1.0; // no weight change
    }
}