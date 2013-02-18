
package bna;

import bna.bnlib.*;
import bna.bnlib.sampling.*;

/**
 * Class for on-the-fly testing and playing with the bnlib package.
 */
public class BNA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        long timeStart, timeEnd;
        try {
            // load BN from file
            timeStart = System.currentTimeMillis();
            System.out.println("Loading a network from file...");
            BayesianNetwork bn = BayesianNetwork.loadFromFile("../../networks/sprinkler.net");
            timeEnd = System.currentTimeMillis();
            System.out.println(String.format("(action took %.2f seconds)\n", (timeEnd - timeStart) / 1000.0));
            
            
            // sampling and its variants
            final int SAMPLES_COUNT = 10000000;
            final int THREAD_COUNT = 2;
            Variable[] X = {bn.getVariable("RAIN")};
            Variable[] Y = {};
            Variable[] E = {bn.getVariable("WETGRASS")};
            int[] e = {1};
            
            // weighted sampling P(RAIN | WETGRASS = TRUE)
            System.out.println("Weighted sampling...");
            timeStart = System.currentTimeMillis();
            WeightedSampleProducer weightedSampleProducer = new WeightedSampleProducer(bn, X, Y, E, e);
            QuerySampler weightedQuerySampler = new QuerySampler(weightedSampleProducer);
            SamplingController weightedSamplingController = new SamplingController(SAMPLES_COUNT);
            weightedQuerySampler.sample(weightedSamplingController);
            Factor weightedSamples = weightedQuerySampler.getSamplesCounterNormalized();
            System.out.println("sampleCounter:");
            System.out.println(weightedSamples.toString());
            timeEnd = System.currentTimeMillis();
            System.out.println(String.format("(action took %.2f seconds)\n", (timeEnd - timeStart) / 1000.0));
            
            // MCMC sampling P(RAIN | WETGRASS = TRUE)
            System.out.println("MCMC sampling...");
            timeStart = System.currentTimeMillis();
            MCMCSampleProducer mcmcSampleProducer = new MCMCSampleProducer(bn, X, Y, E, e);
            QuerySampler mcmcQuerySampler = new QuerySampler(mcmcSampleProducer);
            SamplingController mcmcSamplingController = new SamplingController(SAMPLES_COUNT);
            mcmcQuerySampler.sample(mcmcSamplingController);
            Factor mcmcSamples = mcmcQuerySampler.getSamplesCounterNormalized();
            // write out
            System.out.println("sampleCounter:");
            System.out.println(mcmcSamples.toString());
            timeEnd = System.currentTimeMillis();
            System.out.println(String.format("(action took %.2f seconds)\n", (timeEnd - timeStart) / 1000.0));
            
            // multithreaded weighted sampling P(RAIN | WETGRASS = TRUE)
            // somehow lousy results (inaccurate, far better is single-threaded result)
            //      - [apears to be the cause] maybe Random is not thread safe
            // TODO slow
            //      - maybe the shared java.util.Random is the bottleneck
            System.out.println("Multithreaded weighted sampling...");
            timeStart = System.currentTimeMillis();
            WeightedSampleProducer sharedWeightedSampleProducer = new WeightedSampleProducer(bn, X, Y, E, e);
            QuerySamplerMultithreaded weightedQuerySamplerMultithreaded = new QuerySamplerMultithreaded(sharedWeightedSampleProducer, THREAD_COUNT);
            SamplingController weightedSamplingMultithreadedController = new SamplingController(SAMPLES_COUNT);
            weightedQuerySamplerMultithreaded.sample(weightedSamplingMultithreadedController);
            Factor weightedSamplesMultithreaded = weightedQuerySamplerMultithreaded.getSamplesCounterNormalized();
            System.out.println("sampleCounter:");
            System.out.println(weightedSamplesMultithreaded.toString());
            timeEnd = System.currentTimeMillis();
            System.out.println(String.format("(action took %.2f seconds)\n", (timeEnd - timeStart) / 1000.0));
            
            /*
            // generating a dataset for the loaded bn
            System.out.println("dataset sampling...");
            timeStart = System.currentTimeMillis();
            BayesianNetworkDatasetCreationSampler datasetSampler = new BayesianNetworkDatasetCreationSampler(bn, "samples.csv");
            SamplingController datasetSamplingController = new SamplingController(SAMPLES_COUNT);
            datasetSampler.sample(datasetSamplingController);
            timeEnd = System.currentTimeMillis();
            System.out.println(String.format("(action took %.2f seconds)\n", (timeEnd - timeStart) / 1000.0));*/
        }
        catch(BayesianNetworkException bnex) {
            bnex.printStackTrace();
        }
    }
}
