// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/18

package bna.bnlib;

import java.util.ArrayList;
import java.util.Random;




/**
 * Concrete implementation of sampler for Markov chain Monte-Carlo sampling.
 * Formally, in order to resample variable X given current sample (ie. assigment)
 * "a", we need to compute the distribution P(X | MB(X) = a) and sample this
 * distribution. However, only a subset of variables in MB(X) change their
 * probabilities with different values of X, namely variables X and Children(X);
 * the rest of the MB(X) doesn't need to be accounted for when computing
 * the distribution P(X | MB(X) = a).
 */
public class BayesianNetworkMCMCSampleProducer extends BayesianNetworkSampleProducer {
    private ArrayList<MCMCResamplingAction> resamplingActions = new ArrayList<>();
    
    public BayesianNetworkMCMCSampleProducer(BayesianNetwork bn, Variable[] X, Variable[] Y, Variable[] E, int[] e) {
        super(bn, X, Y, E, e);
        this.generateResamplingActions();
    }
    
    @Override
    public BayesianNetworkSampleProducer cloneWithNewRandomObject() {
        return new BayesianNetworkMCMCSampleProducer(this.bn, this.XVars, this.YVars, this.EVars, this.EVals);
    }
    
    private void generateResamplingActions() {
        for(Variable varToResample : this.sampledVars) {
            if(!Toolkit.arrayContains(this.EVars, varToResample)) {
                // we can resample any variable except evidence variables which
                // doesn't make sense
                MCMCResamplingAction action = new MCMCResamplingAction(this.bn, this.rand, this.sampledVars, varToResample);
                this.resamplingActions.add(action);
            }
        }
    }

    /** Initializes the sample by a single weighted sampling pass. */
    @Override
    protected void initializeSample(int[] sampledVarsValues) {
        BayesianNetworkWeightedSampleProducer weightedSampler = new BayesianNetworkWeightedSampleProducer(
                this.bn, this.XVars, this.YVars, this.EVars, this.EVals);
        // the variable values are preserved after the following call
        weightedSampler.produceSample(sampledVarsValues);
    }

    /** Select a single resampling action at random and execute it. */
    @Override
    protected double produceSample(int[] sampledVarsValues) {
        // TODO maybe sequentially instead of at random ??
        int actionIndex = this.rand.nextInt(this.resamplingActions.size());
        MCMCResamplingAction action = this.resamplingActions.get(actionIndex);
        action.resample(sampledVarsValues);
        return 1.0; // MCMC doesn't use weights for samples
    }
}



class MCMCResamplingAction {
    private Random rand;
    // cached values for faster computation
    // vector of probabilities P(resampledVar = 0,1,2,... | mb(resampledVar))
    private double[] resampledVarAssignmentProb;
    // index of the value, which is resampled by this action, in the sampledVarsValues array
    // passed to the resample(...) method
    private int resampledVarIndexInSampledVars;
    // variables from which are computed probabilities in resampledVarAssignmentProb
    // (resampledVar and its children) as product of probability of each variable
    // having the concrete value in current sample
    private Node[] significantNodes;
    // mapping for each variable varToResample in significantNodes: sampledVars -> varToResample union Parents(varToResample)
    private VariableSubsetMapper[] sampledVarsToSignificantVarAndParentsMappers;
    
    
    public MCMCResamplingAction(BayesianNetwork bn, Random rand, Variable[] sampledVars, Variable resampledVar) {
        this.rand = rand;
        
        this.resampledVarAssignmentProb = new double[resampledVar.getCardinality()];
        this.resampledVarIndexInSampledVars = Toolkit.indexOf(sampledVars, resampledVar);
        // nodes (variables) needed to compute the distribution P(resampledVar | currentSample)
        this.significantNodes = new Node[1 + bn.getVariableChildrenCount(resampledVar)];
        this.significantNodes[0] = bn.getNode(resampledVar);
        Variable[] resampledVarChildren = bn.getVariableChildren(resampledVar);
        for(int i = 0 ; i < resampledVarChildren.length ; i++)
            this.significantNodes[i + 1] = bn.getNode(resampledVarChildren[i]);
        this.sampledVarsToSignificantVarAndParentsMappers = new VariableSubsetMapper[significantNodes.length];
        for(int i = 0 ; i < significantNodes.length ; i++) {
            Variable probVarI = this.significantNodes[i].getVariable();
            Variable[] probVarIParents = bn.getVariableParents(probVarI);
            Variable[] probVarIAndParents = new Variable[1 + bn.getVariableParentsCount(probVarI)];
            probVarIAndParents[0] = probVarI;
            System.arraycopy(probVarIParents, 0, probVarIAndParents, 1, probVarIParents.length);
            VariableSubsetMapper mapper = new VariableSubsetMapper(sampledVars, probVarIAndParents);
            this.sampledVarsToSignificantVarAndParentsMappers[i] = mapper;
        }
    }
    
    public void resample(int[] sampledVarsValues) {
        // for each possible assignmnet i of variable resampledVar
        //     put the value i of resampledVar to sampledVarsValues vector
        //     prob[i] = P(resampledVar = i | parents(resampledVar)
        //     for each variable varToResample in Markov blanket of variable resampledVar do: (we don't really need to account for the whole blanket)
        //         prob[i] *= P(v | parents(varToResample))
        // resample resampledVar by prob vector and put the resampled value to sampledVarsValues
        
        double probSum = 0; // keep track of probabilities sum for sampling of the final distribution
        
        for(int i = 0 ; i < this.resampledVarAssignmentProb.length ; i++) {
            // ~ for each possible assignmnet i of variable resampledVar
            sampledVarsValues[this.resampledVarIndexInSampledVars] = i;
            this.resampledVarAssignmentProb[i] = 1.0;
            for(int j = 0 ; j < this.significantNodes.length ; j++) {
                Node nodeJ = this.significantNodes[j];
                VariableSubsetMapper sampledVarsTovarJAndParentsMapper = this.sampledVarsToSignificantVarAndParentsMappers[j];
                int[] probVarJAndParentsAssignment = sampledVarsTovarJAndParentsMapper.map(sampledVarsValues);
                this.resampledVarAssignmentProb[i] *= nodeJ.getProbability(probVarJAndParentsAssignment);
            }
            probSum += this.resampledVarAssignmentProb[i];
        }
        // finally resample the variable
        int resampledAssignment = Toolkit.randomIndex(this.resampledVarAssignmentProb, probSum, this.rand);
        sampledVarsValues[this.resampledVarIndexInSampledVars] = resampledAssignment;
    }
}