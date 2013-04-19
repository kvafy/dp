// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/18

package bna.bnlib.sampling;

//import java.util.concurrent.ThreadLocalRandom;
import bna.bnlib.io.ParsedQuery;
import bna.bnlib.*;
import bna.bnlib.misc.ThreadLocalRandom;
import bna.bnlib.misc.Toolkit;
import java.util.Random;


/**
 * Abstract sampler that can produce a single sample.
 * Generate samples for computing approximation of the distribution P(X | Y, E = e).
 * This is a general sampler using the template method design pattern; usable
 * for weighted sampling and MCMC sampling.
 */
public abstract class SampleProducer {
    // !!! beware: members of this class may be accessed within classes QuerySampler
    //             and DatasetCreationSampler, so be careful with changing their
    //             semantics
    protected BayesianNetwork bn;
    protected Variable[] XVars, YVars;
    protected Variable[] XYVars; // XVars union YVars
    protected Variable[] EVars;  // evidence variables
    protected int[] EVals;       // concrete values of evidence variables
    protected Variable[] sampledVars;  // defines (1) all variables for which we need
                                       // to keep track of their current assignment
                                       // and (2) the sampling order of these variables
    protected VariableSubsetMapper sampledVarsToXYVarsMapper; // mapping of sampledVars assignment to XYVars
    
    
    /**
     * Create a sample producer for the query P(X | Y, E = e).
     * The X,Y,E,e arguments may not be null (use an 0-length array). The X argument
     * must contain at least one variable. X, Y and E have to be disjoint and
     * all have to be variables contained in the given network.
     * @throws BNLibIllegalArgumentException When conditions of valid query aren't met.
     */
    public SampleProducer(BayesianNetwork bn, Variable[] X, Variable[] Y, Variable[] E, int[] e) throws BNLibIllegalArgumentException {
        if(X == null || Y == null || E == null || e == null)
            throw new BNLibIllegalArgumentException("None of the arrays can be null.");
        if(X.length == 0)
            throw new BNLibIllegalArgumentException("The X array must be non-empty.");
        Variable[] allVars = bn.getVariables();
        Variable[] XY = Toolkit.union(X, Y);
        // validate inputs
        if(!Toolkit.areDisjoint(X, Y) || !Toolkit.areDisjoint(XY, E)
                || !Toolkit.isSubset(allVars, XY) || !Toolkit.isSubset(allVars, E))
            throw new BNLibIllegalArgumentException("Invalid variables specified.");
        
        this.bn = bn;
        this.XVars = X;
        this.YVars = Y;
        this.XYVars = XY;
        this.EVars = E;
        this.EVals = e;
        this.determineSamplingOrder(); // fills sampledVars
        this.sampledVarsToXYVarsMapper = new VariableSubsetMapper(this.sampledVars, this.XYVars);
    }
    
    /**
     * Create a sample producer based on a query of textual form.
     * @param bn Network to be sampled.
     * @param query Textual query of general form "P(X1, ..., Xn | Y1, ..., Ym, E1 = e1, ..., Ek = ek)".
     *              The names of variables as well as their values of evidence
     *              must conform exactly (case sensitively) to variables from
     *              given network and to their values.
     * @throws BNLibIllegalQueryException When the query string is invalid.
     */
    public SampleProducer(BayesianNetwork bn, String query) throws BNLibIllegalQueryException {
        this(new ParsedQuery(bn, query));
    }
    
    /** Just to solve the "call to this must be first statement" in the constructor above. */
    private SampleProducer(ParsedQuery query) {
        this(query.bn, query.X, query.Y, query.E, query.e);
    }
    
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
    
    /**
     * Obtain a new sampling context for the current thread.
     */
    final SamplingContext createSamplingContext() {
        int[] sampledVarsAssignment = new int[this.sampledVars.length];
        int[] XYVarsAssignment = new int[this.XYVars.length];
        Random rand = ThreadLocalRandom.current();
        return new SamplingContext(sampledVarsAssignment, XYVarsAssignment, rand);
    }
    
    
    // Template method pattern for weighted sampling / MCMC
    
    /**
     * Determine variables that really need to be sampled (this is default implementation).
     * Hook of the template method determineSamplingOrder().
     * The method must preserve relative order of given variables.
     */
    protected Variable[] filterVariablesToSample(Variable[] unfilteredSampledVariables) {
        Variable[] mustSampleVariables = unfilteredSampledVariables;
        return mustSampleVariables;
    }
    
    protected abstract void initializeSample(SamplingContext context);
    
    /**
     * Produce new sample with some weights to the given context object.
     * Depending on sampling method, one to all values variables may change.
     * @param context Context to fill with a new sample.
     */
    protected abstract void produceSample(SamplingContext context);
}
