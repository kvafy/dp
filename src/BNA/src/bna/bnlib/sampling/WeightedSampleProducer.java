// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/09

package bna.bnlib.sampling;

import bna.bnlib.*;
import bna.bnlib.misc.Toolkit;
import java.util.Arrays;
import java.util.LinkedList;


/**
 * Concrete implementation of sampler for weighted sampling.
 * The sampler works as follows: We compute a topological sort of all variables
 * in the network and prune nodes that are not in the set (X union Y union E)
 * nor any of their descendants is in this set.
 * One sample is produced by sampling all the variables left after pruning
 * (pruned in method filterVariablesToSample(...)) in topological order, one
 * variable at a time. Sampling action of a single variable is implemented as
 * an abstract WeightedSamplingAction. So, we perform all actions in samplingActions
 * array in order to produce one sample. The sampling action is dependent on
 * whether it is an evidence variable or other:
 *  (a) evidence variable E: extract assignment to Parents(E) from the current
 *      sampledVarsValues and modify sample weight. Also place the observed
 *      value of evidence into assignment vector in the sampling context.
 *  (b) non-evidence variable X: extract assignment to Parents(X) and sample
 *      the variable X. Put the sampled value into assignment vector in the
 *      sampling context.
 */
public class WeightedSampleProducer extends SampleProducer {
    private WeightedSamplingAction[] samplingActions;

    public WeightedSampleProducer(BayesianNetwork bn, Variable[] X, Variable[] Y, Variable[] E, int[] e) {
        super(bn, X, Y, E, e);
        this.generateSamplingActions();
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
    public WeightedSampleProducer(BayesianNetwork bn, String query) throws BNLibIllegalQueryException {
        this(new ParsedQuery(bn, query));
    }
    
    /** Just to solve the "call to this must be first statement" in the constructor above. */
    private WeightedSampleProducer(ParsedQuery query) {
        this(query.bn, query.X, query.Y, query.E, query.e);
    }
    
    private void generateSamplingActions() {
        this.samplingActions = new WeightedSamplingAction[this.sampledVars.length];
        for(int i = 0 ; i < this.sampledVars.length ; i++) {
            Variable varI = this.sampledVars[i];
            Node varINode = this.bn.getNode(varI.getName());
            WeightedSamplingAction actionI;
            
            if(Toolkit.arrayContains(this.EVars, varI)) {
                Variable[] IAndParentsVars = Toolkit.union(new Variable[]{varI}, varINode.getParentVariables());
                VariableSubsetMapper allVarsToIAndParentsMapper = new VariableSubsetMapper(this.sampledVars, IAndParentsVars);
                // action: adjust produceSample weight and write the evidence value
                int evidenceValue = this.EVals[Toolkit.indexOf(this.EVars, varI)];
                // - map allVarsValues to Parents(ENode) values
                // - add known value of evidence variable => EValue,parents(EValue) assignment
                // - read probability of that assignment from factor for ENode
                actionI = new WeightedSamplingEvidenceAction(varINode,
                                                             Toolkit.indexOf(this.sampledVars, varI),
                                                             evidenceValue,
                                                             allVarsToIAndParentsMapper);
            }
            else {
                VariableSubsetMapper allVarsToIParentsMapper = new VariableSubsetMapper(this.sampledVars, varINode.getParentVariables());
                // action: produceSample variable by it's parents current assignment
                // - map allVarsValues to Parents(X) values
                // - for assignment of parents produceSample value for X
                actionI = new WeightedSamplingVariableAction(varINode,
                                                             Toolkit.indexOf(this.sampledVars, varI),
                                                             allVarsToIParentsMapper);
            }
            this.samplingActions[i] = actionI;
        }
    }
    
    /**
     * In weighted sampling we can optimize and not to sample variables such
     * that they are not in (X union Y union E) and also none of their descendants
     * is in (X union Y union E). These variables are ommited and not sampled at all.
     */
    @Override
    protected Variable[] filterVariablesToSample(Variable[] unfilteredSampledVariables) {
        Variable[] XYE = Toolkit.union(this.XYVars, this.EVars);
        LinkedList<Variable> mustSampleVariables = new LinkedList<Variable>();
        // optimization: omit leaf variables not in (X union Y union E)
        //               and apply recursively until some variable can be ommited
        for(int i = unfilteredSampledVariables.length - 1 ; i >= 0 ; i--) {
            Variable varI = unfilteredSampledVariables[i];
            Node varINode = this.bn.getNode(varI.getName());
            Variable[] varIChildren = varINode.getChildVariables();
            if(Toolkit.arrayContains(XYE, varI))
                mustSampleVariables.addFirst(varI);
            else if(!Toolkit.areDisjoint(mustSampleVariables, Arrays.asList(varIChildren)))
                mustSampleVariables.addFirst(varI);
        }
        // we are done
        Variable[] mustSampleVariablesArray = new Variable[mustSampleVariables.size()];
        mustSampleVariables.toArray(mustSampleVariablesArray);
        //System.out.println(String.format("debug: #of variables really sampled is %d", mustSampleVariablesArray.length));
        return mustSampleVariablesArray;
    }
    
    @Override
    protected void initializeSample(SamplingContext context) {
        // all the work is done in produceSample
    }

    /**
     * One sample is in weighted sampling produced by sampling all the variables
     * in the network whilst accumulating the weight of the sample according to
     * evidence.
     * @param context Context to fill with a new sample.
     */
    @Override
    protected void produceSample(SamplingContext context) {
        context.sampleWeight = 1.0;
        for(WeightedSamplingAction action : this.samplingActions)
            action.sample(context);
        this.sampledVarsToXYVarsMapper.map(context.sampledVarsAssignment, context.XYVarsAssignment);
    }
}




/** The Command design pattern for sampling of all variables (evidence and non-evidence). */
abstract class WeightedSamplingAction {
    
    /**
     * Put value of a variable to the context.sampledVarsAssignment and 
     * possibly modify the context.sampleWeight.
     * @param context Context to modify.
     */
    public abstract void sample(SamplingContext context);
}

/**
 * "Sample" an evidence variable - perform weight change and put the observed
 * evidence value to sampling context.
 */
class WeightedSamplingEvidenceAction extends WeightedSamplingAction {
    private Node ENode;
    private int EValue;
    private int EIndex;
    private VariableSubsetMapper allVarsToEAndParentsMapper;
    
    public WeightedSamplingEvidenceAction(Node evidenceNode, int evidenceVarIndex, int evidenceVal, VariableSubsetMapper allVarsToEAndParents) {
        this.ENode = evidenceNode;
        this.EValue = evidenceVal;
        this.EIndex = evidenceVarIndex;
        this.allVarsToEAndParentsMapper = allVarsToEAndParents;
    }
    
    @Override
    public void sample(SamplingContext context) {
        // for variable E determine assignment to Parents(E) from sampledVarsValues
        int[] nodeAndParentsAssignment = this.allVarsToEAndParentsMapper.map(context.sampledVarsAssignment);
        // determine weight change coefficient, ie. probability of P(E = e, parents(E))
        double eProb = this.ENode.getProbability(nodeAndParentsAssignment);
        context.sampleWeight *= eProb;
        context.sampledVarsAssignment[this.EIndex] = this.EValue;
    }
}

/**
 * Sample a non-evidence variable by extracting assignment of it's parents,
 * sampling and placing the sampled value to current produceSample.
 */
class WeightedSamplingVariableAction extends WeightedSamplingAction {
    private Node XNode;
    private int XIndex;
    private VariableSubsetMapper allVarsToXParentsMapper;
    
    public WeightedSamplingVariableAction(Node XNode, int XVarIndex, VariableSubsetMapper allVarsToXParentsMapper) {
        this.XNode = XNode;
        this.XIndex = XVarIndex;
        this.allVarsToXParentsMapper = allVarsToXParentsMapper;
    }

    @Override
    public void sample(SamplingContext context) {
        int[] XParentsAssignment = this.allVarsToXParentsMapper.map(context.sampledVarsAssignment);
        int XVal = this.XNode.sampleVariable(XParentsAssignment, context.rand);
        context.sampledVarsAssignment[this.XIndex] = XVal;
    }
}