// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/18

package bna.bnlib.sampling;

import bna.bnlib.*;
import bna.bnlib.misc.Toolkit;
import java.util.ArrayList;


/**
 * Concrete implementation of sampler for Markov chain Monte-Carlo sampling method.
 * Formally, in order to resample variable X given current sample (ie. assigment)
 * "s", we need to compute the distribution P(X | MB(X) = s) and sample this
 * distribution. However, only a subset of variables in MB(X) change their
 * probabilities with different values of X, namely variables (X union Children(X));
 * the rest of the MB(X) doesn't need to be accounted for when computing
 * the distribution P(X | MB(X) = s).
 */
public class MCMCSampleProducer extends SampleProducer {
    private ArrayList<MCMCResamplingAction> resamplingActions = new ArrayList<MCMCResamplingAction>();
    
    public MCMCSampleProducer(BayesianNetwork bn, Variable[] X, Variable[] Y, Variable[] E, int[] e) {
        super(bn, X, Y, E, e);
        this.generateResamplingActions();
    }
    
    private void generateResamplingActions() {
        for(Variable varToResample : this.sampledVars) {
            if(!Toolkit.arrayContains(this.EVars, varToResample)) {
                // we can resample any variable except evidence variables
                // (for evidence doesn't make sense)
                MCMCResamplingAction action = new MCMCResamplingAction(this.bn, this.sampledVars, varToResample);
                this.resamplingActions.add(action);
            }
        }
    }
    
    /**
     * Create a weighted sample producer based on a query of textual form.
     * @param bn Network to be sampled.
     * @param query Textual query of general form "P(X1, ..., Xn | Y1, ..., Ym, E1 = e1, ..., Ek = ek)".
     *              The names of variables as well as their values of evidence
     *              must conform exactly (case sensitively) to variables from
     *              given network and to their values.
     * @throws BayesianNetworkException When the query string is invalid.
     */
    public MCMCSampleProducer(BayesianNetwork bn, String query) throws BNLibIllegalQueryException {
        this(new ParsedQuery(bn, query));
    }
    
    /** Just to solve the "call to this must be first statement" in the constructor above. */
    private MCMCSampleProducer(ParsedQuery query) {
        this(query.bn, query.X, query.Y, query.E, query.e);
    }

    /** Initializes the sample by seting the evidence variables. */
    @Override
    protected void initializeSample(SamplingContext context) {
        for(int i = 0 ; i < this.EVars.length ; i++) {
            Variable EVar = this.EVars[i];
            int EVal = this.EVals[i];
            int EIndex = Toolkit.indexOf(this.sampledVars, EVar);
            context.sampledVarsAssignment[EIndex] = EVal;
        }
        context.sampleWeight = 1.0;
        // TODO maybe produce a few samples to get the network into a more "normal" state

        /*WeightedSampleProducer weightedSampler = new WeightedSampleProducer(
                this.bn, this.XVars, this.YVars, this.EVars, this.EVals);
        // the variable values are preserved after the following call
        weightedSampler.produceSample(context); // evidence is correctly set
        context.sampleWeight = 1.0;*/
    }

    /** Select a single resampling action at random and execute it. */
    @Override
    protected void produceSample(SamplingContext context) {
        context.sampleWeight = 1.0;
        // TODO maybe sequentially instead of at random ??
        int actionIndex = context.rand.nextInt(this.resamplingActions.size());
        MCMCResamplingAction action = this.resamplingActions.get(actionIndex);
        action.resample(context);
        this.sampledVarsToXYVarsMapper.map(context.sampledVarsAssignment, context.XYVarsAssignment);
    }
}



class MCMCResamplingAction {
    // index of the value, which is resampled by this action, in the sampledVarsValues array
    // passed to the resample(...) method
    private int resampledVarIndexInSampledVars;
    // variable to be resampled by this MCMC action
    private Variable resampledVar;
    // variables from which are computed probabilities in resampledVarAssignmentProb
    // (resampledVar and its children) as product of probability of each variable
    // having the concrete value in current sample
    private Node[] significantNodes;
    // mapping for each variable varToResample in significantNodes: sampledVars -> varToResample union Parents(varToResample)
    private VariableSubsetMapper[] sampledVarsToSignificantVarAndParentsMappers;
    
    
    public MCMCResamplingAction(BayesianNetwork bn, Variable[] sampledVars, Variable resampledVar) {
        this.resampledVar = resampledVar;
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
    
    public void resample(SamplingContext context) {
        // for each possible assignmnet i of variable resampledVar
        //     put the value i of resampledVar to sampledVarsValues vector
        //     prob[i] = P(resampledVar = i | parents(resampledVar)
        //     for each variable varToResample in Markov blanket of variable resampledVar do: (we don't really need to account for the whole blanket)
        //         prob[i] *= P(v | parents(varToResample))
        // resample resampledVar by prob vector and put the resampled value to sampledVarsValues
        
        // keep track of probabilities sum for quicker sampling of the final distribution
        double probSum = 0;
        // vector of probabilities P(resampledVar = 0,1,2,... | mb(resampledVar))
        double[] resampledVarAssignmentProb = new double[resampledVar.getCardinality()];
        
        for(int i = 0 ; i < resampledVarAssignmentProb.length ; i++) {
            // ~ for each possible assignmnet i of variable resampledVar
            context.sampledVarsAssignment[this.resampledVarIndexInSampledVars] = i;
            resampledVarAssignmentProb[i] = 1.0;
            for(int j = 0 ; j < this.significantNodes.length ; j++) {
                Node nodeJ = this.significantNodes[j];
                VariableSubsetMapper sampledVarsTovarJAndParentsMapper = this.sampledVarsToSignificantVarAndParentsMappers[j];
                int[] probVarJAndParentsAssignment = sampledVarsTovarJAndParentsMapper.map(context.sampledVarsAssignment);
                resampledVarAssignmentProb[i] *= nodeJ.getProbability(probVarJAndParentsAssignment);
            }
            probSum += resampledVarAssignmentProb[i];
        }
        // finally resample the variable
        int resampledAssignment = Toolkit.randomIndex(resampledVarAssignmentProb, probSum, context.rand);
        context.sampledVarsAssignment[this.resampledVarIndexInSampledVars] = resampledAssignment;
    }
}