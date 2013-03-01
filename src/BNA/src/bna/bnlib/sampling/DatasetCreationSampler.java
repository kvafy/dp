// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/01


package bna.bnlib.sampling;

import bna.bnlib.*;
import bna.bnlib.learning.*;


/**
 * Sampler for creating an artificial dataset from given BN and storing it into memory as Dataset instance.
 */
public class DatasetCreationSampler extends Sampler {
    private Dataset dataset;

    
    public DatasetCreationSampler(BayesianNetwork bn) {
        // generate a weighted sample producer that will sample all variables in the network,
        // ie. for a query P(AllVariables)
        super(new WeightedSampleProducer(
                bn,
                // X = AllVariables ~ the SampleProducer will surely sample all variables
                bn.getVariables(),
                new Variable[0],
                new Variable[0],
                new int[0])
              );
        this.dataset = new Dataset(this.XYVars);
    }
    
    @Override
    protected void registerSample(int[] XYVarsValues, double sampleWeight) {
        if(sampleWeight != 1.0)
            throw new BayesianNetworkRuntimeException("Internal error: Weight of a sample for dataset is not 1.0.");
        this.dataset.addRecord(XYVarsValues);
    }

    @Override
    protected void presamplingActions() {
        // no need to do anything
    }

    @Override
    protected void postsamplingActions() {
        // no need to do anything
    }
    
    public Dataset getDataset() {
        return this.dataset;
    }
}
