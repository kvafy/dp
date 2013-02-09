/*
 * // Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
 * // Author:  David Chaloupka (xchalo09)
 * // Created: 2013/xx/xx
 */

package bna.bnlib;

import java.util.Random;

/**
 *
 */
public class BayesianNetworkWeightedSampler extends BayesianNetworkSampler {
    private WeightedSamplingSampleAction[] samplingActions;

    public BayesianNetworkWeightedSampler(BayesianNetwork bn, Variable[] XY, Variable[] E, int[] e) {
        super(bn, XY, E, e);
        Random rand = new Random();
        Variable[] allVarsSorted = bn.topologicalSort();
        // generate sampling actions
        this.samplingActions = new WeightedSamplingSampleAction[bn.getVariablesCount()];
        for(int i = 0 ; i < allVarsSorted.length ; i++) {
            Variable varI = allVarsSorted[i];
            Node varINode = this.bn.getNode(varI.getName());
            VariableSubsetMapper allVarsToIParentsMapper = new VariableSubsetMapper(allVarsSorted, varINode.getParentVariables());
            WeightedSamplingSampleAction actionI;
            
            if(Toolkit.arrayContains(E, varI)) {
                // action: adjust sample weight and write the evidence value
                int varIValue = e[Toolkit.indexOf(E, varI)];
                // - map allVarsValues to Parents(ENode) values
                // - add known value of evidence variable => EValue,parents(EValue) assignment
                // - read probability of that assignment from factor for ENode
                actionI = new WeightedSamplingEvidenceSampleAction(varINode,
                                                                   Toolkit.indexOf(allVarsSorted, varI),
                                                                   varIValue,
                                                                   allVarsToIParentsMapper,
                                                                   rand);
            }
            else {
                // action: sample variable by it's parents current assignment
                // - map allVarsValues to Parents(X) values
                // - for assignment of parents sample value for X
                actionI = new WeightedSamplingVariableSampleAction(varINode,
                                                                   Toolkit.indexOf(allVarsSorted, varI),
                                                                   allVarsToIParentsMapper,
                                                                   rand);
            }
            this.samplingActions[i] = actionI;
        }
    }
    
    @Override
    protected void initializeSample(int[] allVarsValues) {
    }

    @Override
    protected double sample(int[] allVarsValues) {
        double weight = 1.0;
        for(WeightedSamplingSampleAction action : this.samplingActions)
            weight *= action.sample(allVarsValues);
        return weight;
    }

}



abstract class WeightedSamplingSampleAction {
    protected Random rand;
    
    public WeightedSamplingSampleAction(Random rand) {
        this.rand = rand;
    }
    
    public abstract double sample(int[] allVarsValues);
}

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
        int[] parentsAssignment = this.allVarsToEParentsMapper.map(allVarsValues);
        int[] nodeAndParentsAssignment = new int[1 + parentsAssignment.length];
        nodeAndParentsAssignment[0] = this.EValue;
        System.arraycopy(parentsAssignment, 0, nodeAndParentsAssignment, 1, parentsAssignment.length);
        // determine weight change coefficient
        double weight = this.ENode.getProbability(nodeAndParentsAssignment);
        allVarsValues[this.EIndex] = this.EValue;
        return weight;
    }
}

class WeightedSamplingVariableSampleAction extends WeightedSamplingSampleAction {
    private Node XNode;
    private int XIndex;
    private VariableSubsetMapper allVarsToXParentsMapper;
    
    public WeightedSamplingVariableSampleAction(Node XNode, int XVarIndex, VariableSubsetMapper allVarsToXParents, Random rand) {
        super(rand);
        this.XNode = XNode;
        this.XIndex = XVarIndex;
        this.allVarsToXParentsMapper = allVarsToXParents;
    }

    @Override
    public double sample(int[] allVarsValues) {
        int[] XParentsAssignemnt = this.allVarsToXParentsMapper.map(allVarsValues);
        int XVal = this.XNode.sampleVariable(XParentsAssignemnt, this.rand);
        allVarsValues[this.XIndex] = XVal;
        return 1.0; // no weight change
    }
}