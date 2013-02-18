
package bna;

import bna.bnlib.*;

/**
 *
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
            final int SAMPLES_COUNT = 1000000;
            final int THREAD_COUNT = 4;
            Variable[] X = {bn.getVariable("RAIN")};
            Variable[] Y = {};
            Variable[] E = {bn.getVariable("WETGRASS")};
            int[] e = {1};
            
            // weighted sampling P(RAIN | WETGRASS = TRUE)
            System.out.println("Weighted sampling...");
            timeStart = System.currentTimeMillis();
            BayesianNetworkWeightedSampleProducer weightedSampleProducer = new BayesianNetworkWeightedSampleProducer(bn, X, Y, E, e);
            BayesianNetworkQuerySampler weightedQuerySampler = new BayesianNetworkQuerySampler(weightedSampleProducer);
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
            BayesianNetworkMCMCSampleProducer mcmcSampleProducer = new BayesianNetworkMCMCSampleProducer(bn, X, Y, E, e);
            BayesianNetworkQuerySampler mcmcQuerySampler = new BayesianNetworkQuerySampler(mcmcSampleProducer);
            SamplingController mcmcSamplingController = new SamplingController(SAMPLES_COUNT);
            mcmcQuerySampler.sample(mcmcSamplingController);
            Factor mcmcSamples = mcmcQuerySampler.getSamplesCounterNormalized();
            // write out
            System.out.println("sampleCounter:");
            System.out.println(mcmcSamples.toString());
            timeEnd = System.currentTimeMillis();
            System.out.println(String.format("(action took %.2f seconds)\n", (timeEnd - timeStart) / 1000.0));
            
            // multithreaded weighted sampling P(RAIN | WETGRASS = TRUE)
            System.out.println("Multithreaded weighted sampling...");
            timeStart = System.currentTimeMillis();
            BayesianNetworkWeightedSampleProducer sharedWeightedSampleProducer = new BayesianNetworkWeightedSampleProducer(bn, X, Y, E, e);
            BayesianNetworkQuerySamplerMultithreaded weightedQuerySamplerMultithreaded = new BayesianNetworkQuerySamplerMultithreaded(sharedWeightedSampleProducer, THREAD_COUNT);
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
