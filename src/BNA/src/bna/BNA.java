
package bna;

import bna.bnlib.*;
import bna.bnlib.sampling.*;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Class for on-the-fly testing and playing with the bnlib package.
 */
public class BNA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        playing_sampling();
        //benchmarkSampling_sequentialVSMultithreaded();
    }
    
    
    private static void playing_sampling() {
        long timeStart, timeEnd;
        try {
            // load BN from file
            timeStart = System.currentTimeMillis();
            System.out.println("Loading a network from file...");
            BayesianNetwork bn = BayesianNetwork.loadFromFile("../../networks/sprinkler.net");
            timeEnd = System.currentTimeMillis();
            System.out.println(String.format("(action took %.2f seconds)\n", (timeEnd - timeStart) / 1000.0));
            
            
            // sampling and its variants
            final long SAMPLES_COUNT = 20000000;
            final int THREAD_COUNT = 3;
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
            //   [solved] Random is not thread safe => each thread has its own instane
            // slow
            //   [in progress] Random.nextDouble() takes about 90 % of time
            //                 of sampling a non-evidence variable in weighted sampling
            System.out.println("Multithreaded weighted sampling...");
            timeStart = System.currentTimeMillis();
            WeightedSampleProducer sharedWeightedSampleProducer = new WeightedSampleProducer(bn, X, Y, E, e);
            QuerySamplerMultithreaded weightedQuerySamplerMultithreaded = new QuerySamplerMultithreaded(sharedWeightedSampleProducer, THREAD_COUNT);
            SamplingController weightedSamplingMultithreadedController = new SamplingController(SAMPLES_COUNT / THREAD_COUNT);
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
    
    private static void benchmarkSampling_sequentialVSMultithreaded() {
        /* test settings */
        // which network to sample and what sampling method to use
        NetworkVariant networkVariant = NetworkVariant.ICUNet;
        SamplingVariant samplingVariant = SamplingVariant.WeightedSampling;
        // how many samples to generate in a single run
        final long SAMPLES_COUNT = 16 * 3 * 208400*2; // cca 20.000.000 but dividible by any used threadcount
        // how many runs to compute an average from
        final int RUNS_COUNT = 25;
        // how many threads
        final int[] THREAD_COUNTS = {1, 2, 3, 4, 6, 8};
        
        
        /* testing */
        String networkName, networkFile;
        try {
            BayesianNetwork bn;
            Variable[] X;
            Variable[] Y;
            Variable[] E;
            int[] e;
            
            // load the network according to configuration
            switch(networkVariant) {
                case SprinklerNet:
                    networkFile = "../../networks/sprinkler.net";
                    networkName = "sprinkler_net";
                    bn = BayesianNetwork.loadFromFile(networkFile);
                    // P(Rain | WetGrass = true)
                    X = new Variable[] {bn.getVariable("RAIN")};
                    Y = new Variable[] {};
                    E = new Variable[] {bn.getVariable("WETGRASS")};
                    e = new int[] {1};
                    break;
                    
                case ICUNet:
                    networkFile = "../../networks/alarm.net";
                    networkName = "icu_net";
                    bn = BayesianNetwork.loadFromFile(networkFile);
                    // P(PVSAT | ECO2, SAO2 = high)
                    //  - PVSAT ~ PVS   ( "LOW" "NORMAL" "HIGH" )
                    //  - EXPCO2 ~ ECO2 ( "ZERO" "LOW" "NORMAL" "HIGH" )
                    //  - SAO2          ( "LOW" "NORMAL" "HIGH" )
                    X = new Variable[] {bn.getVariable("PVSAT")};
                    Y = new Variable[] {bn.getVariable("EXPCO2")};
                    E = new Variable[] {bn.getVariable("SAO2")};
                    e = new int[] {2};
                    break;
                default:
                    throw new RuntimeException("unsupported network variant for benchmark");
            }
            
            // sampling type
            SampleProducer sampleProducer;
            String samplingVariantName;
            switch(samplingVariant) {
                case WeightedSampling:
                    sampleProducer = new WeightedSampleProducer(bn, X, Y, E, e);
                    samplingVariantName = "weighted_sampling";
                    break;
                case MCMCSampling:
                    sampleProducer = new MCMCSampleProducer(bn, X, Y, E, e);
                    samplingVariantName = "mcmc_sampling";
                    break;
                default:
                    throw new RuntimeException("unsupported sampling variant for benchmark");
            }
            
            System.out.println(String.format("test for \"%s\" on network \"%s\":", samplingVariantName, networkName));
            int samplesPerSecondSingleThread = 0;
            for(int threadcount : THREAD_COUNTS) {
                LinkedList<Double> runningTimes = new LinkedList<Double>();
                System.out.print(String.format("- run times for %d thread(s):", threadcount));
                while(runningTimes.size() < RUNS_COUNT) {
                    long timeStart, timeEnd;
                    Sampler sampler;
                    sampler = new QuerySamplerMultithreaded(sampleProducer, threadcount); // also for a single thread
                    SamplingController weightedSamplingController = new SamplingController(SAMPLES_COUNT / threadcount);
                    timeStart = System.currentTimeMillis();
                    sampler.sample(weightedSamplingController);
                    timeEnd = System.currentTimeMillis();
                    // statistics
                    runningTimes.add((timeEnd - timeStart) / 1000.0);
                    System.out.print(String.format(" %.3f", runningTimes.getLast()));
                }
                long samplesPerRun = SAMPLES_COUNT;
                double timePerRun = computeMedian(runningTimes);
                int samplesPerSecond = (int)(samplesPerRun / timePerRun);
                if(threadcount == 1) {
                    // new referential value for speedup computation
                    samplesPerSecondSingleThread = samplesPerSecond;
                }
                System.out.println("");
                System.out.println(String.format(
                        "- %d thread(s): %d samples/second (%.2f speedup)",
                        threadcount, samplesPerSecond, ((double)samplesPerSecond) / samplesPerSecondSingleThread));
            }
        }
        catch(BayesianNetworkException bnex) {
            bnex.printStackTrace();
        }
        catch(BayesianNetworkRuntimeException bnex) {
            bnex.printStackTrace();
        }
    }
    
    /*private static double computeAverageWithoutExtremes(LinkedList<Double> values) {
        final double DROP_EXTREMES = 0.1; // drop 10 % of extreme values
        
        values = new LinkedList<>(values); // make a local copy
        Collections.sort(values);
        
        int dropCount = (int)(DROP_EXTREMES * values.size() / 2);
        for(int i = 0 ; i < dropCount ; i++) {
            values.removeFirst();
            values.removeLast();
        }
        // compute the average
        double sum = 0;
        for(double x : values)
            sum += x;
        return sum / values.size();
    }*/
    
    private static double computeMedian(LinkedList<Double> values) {
        values = new LinkedList<Double>(values); // make a local copy
        Collections.sort(values);
        if(values.size() % 2 == 1)
            return values.get(values.size() / 2);
        else
            return (values.get(values.size() / 2 - 1) + values.get(values.size() / 2)) / 2;
    }
    
    enum NetworkVariant {SprinklerNet, ICUNet}
    enum SamplingVariant {WeightedSampling, MCMCSampling}
}
