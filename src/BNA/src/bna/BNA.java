
package bna;

import bna.bnlib.*;
import bna.bnlib.learning.*;
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
        //playing_sampling_mixed();
        //benchmarkSampling_sequentialVSMultithreaded();
        //playing_sampling_string_query();
        //playing_dataset_testing();
        //playing_parameter_learning();
        //benchmark_parameter_learningMLEVSBayes();
        playing_structure_learning();
    }
    
    
    private static void playing_sampling_mixed() {
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
            SampleProducer weightedSampleProducer = new WeightedSampleProducer(bn, X, Y, E, e);
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
            SampleProducer mcmcSampleProducer = new MCMCSampleProducer(bn, X, Y, E, e);
            QuerySampler mcmcQuerySampler = new QuerySampler(mcmcSampleProducer);
            SamplingController mcmcSamplingController = new SamplingController(SAMPLES_COUNT);
            mcmcQuerySampler.sample(mcmcSamplingController);
            Factor mcmcSamples = mcmcQuerySampler.getSamplesCounterNormalized();
            // write out
            System.out.println("sampleCounter:");
            System.out.println(mcmcSamples.toString());
            timeEnd = System.currentTimeMillis();
            System.out.println(String.format("(action took %.2f seconds)\n", (timeEnd - timeStart) / 1000.0));
            
            
            // optional interruption of the main thread when performing multithreaded sampling
            final Thread mainThread = Thread.currentThread();
            Thread interruptThread = new Thread() {
                @Override
                public void run() {
                    try {
                        System.out.println("Waiting to interrupt the main thread...");
                        sleep(2000);
                        System.out.println("Interrupting the main thread now.");
                        mainThread.interrupt();
                    }
                    catch(InterruptedException iex) {}
                }
            };
            interruptThread.start();
            // multithreaded weighted sampling P(RAIN | WETGRASS = TRUE)
            // somehow lousy results (inaccurate, far better is single-threaded result)
            //   [solved] Random is not thread safe => each thread has its own instane
            // slow
            //   [in progress] Random.nextDouble() takes about 90 % of time
            //                 of sampling a non-evidence variable in weighted sampling
            System.out.println("Multithreaded weighted sampling...");
            timeStart = System.currentTimeMillis();
            SampleProducer sharedWeightedSampleProducer = new WeightedSampleProducer(bn, X, Y, E, e);
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
            DatafileCreationSampler datasetSampler = new DatafileCreationSampler(bn, "samples.csv");
            SamplingController datasetSamplingController = new SamplingController(SAMPLES_COUNT);
            datasetSampler.sample(datasetSamplingController);
            timeEnd = System.currentTimeMillis();
            System.out.println(String.format("(action took %.2f seconds)\n", (timeEnd - timeStart) / 1000.0));*/
        }
        catch(BayesianNetworkException bnex) {
            bnex.printStackTrace();
        }
    }
    
    private static void playing_sampling_string_query() {
        final long SAMPLES_COUNT = 1 * 1000 * 1000;
        final int THREAD_COUNT = 3;
        final String NETWORK_FILE = "../../networks/sprinkler.net";
        final String QUERY_STR = "P(RAIN | SPRINKLER = TRUE, WETGRASS = TRUE)";
        //final String NETWORK_FILE = "../../networks/alarm.net";
        //final String QUERY_STR = "P(PVSAT | EXPCO2, SAO2 = HIGH)";
        
        long timeStart, timeEnd;
        try {
            // load BN from file
            timeStart = System.currentTimeMillis();
            System.out.println("Loading a network from file...");
            BayesianNetwork bn = BayesianNetwork.loadFromFile(NETWORK_FILE);
            timeEnd = System.currentTimeMillis();
            System.out.println(String.format("(action took %.2f seconds)\n", (timeEnd - timeStart) / 1000.0));
            
            // multithreaded weighted sampling for QUERY_STR
            // somehow lousy results (inaccurate, far better is single-threaded result)
            //   [solved] the SampleProducer had some cached arrays (to avoid frequent allocation)
            //            whose content got rewritten by concurrent threads
            // slow
            //   [solved] Random.nextDouble() takes about 90 % of time
            //            of sampling a non-evidence variable in weighted sampling
            //            => each thread has its own Random instance
            System.out.println("Multithreaded weighted sampling of string query " + QUERY_STR);
            timeStart = System.currentTimeMillis();
            SampleProducer sharedWeightedSampleProducer = new WeightedSampleProducer(bn, QUERY_STR);
            QuerySamplerMultithreaded weightedQuerySamplerMultithreaded = new QuerySamplerMultithreaded(sharedWeightedSampleProducer, THREAD_COUNT);
            SamplingController weightedSamplingMultithreadedController = new SamplingController(SAMPLES_COUNT / THREAD_COUNT);
            weightedQuerySamplerMultithreaded.sample(weightedSamplingMultithreadedController);
            Factor weightedSamplesMultithreaded = weightedQuerySamplerMultithreaded.getSamplesCounterNormalized();
            System.out.println("sampleCounter:");
            System.out.println(weightedSamplesMultithreaded.toString());
            timeEnd = System.currentTimeMillis();
            System.out.println(String.format("(action took %.2f seconds)\n", (timeEnd - timeStart) / 1000.0));
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
                    // P(PVSAT | ECO2, SAO2 = HIGH)
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
                    SamplerInterface sampler;
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
    
    
    private static void playing_dataset_testing() {
        // take network with independent variables (no connections) and check the mutual information
        final long SAMPLES = 1000 * 1000;
        try {
            System.out.println("Mutual information computation test");
            // sample some empty network
            System.out.println("Loading unconnected sprinkler network...");
            BayesianNetwork bn = BayesianNetwork.loadFromFile("../../networks/sprinkler-empty.net");
            // sample the network
            System.out.println(String.format("Producing dataset with %d samples (to memory)...", SAMPLES));
            DatasetCreationSampler datasetSampler = new DatasetCreationSampler(bn);
            SamplingController samplingController = new SamplingController(SAMPLES);
            datasetSampler.sample(samplingController);
            Dataset dataset = datasetSampler.getDataset();
            // compute mutual information between some two variables
            Variable XVars[] = {bn.getVariable("CLOUDY"), bn.getVariable("WETGRASS")},
                     YVars[] = {bn.getVariable("SPRINKLER"), bn.getVariable("RAIN")};
            double mutualInf = dataset.mutualInformation(XVars, YVars);
            System.out.println(String.format("Mutual information of independent variables: %.2f (should be close to zero)",mutualInf));
        }
        catch(BayesianNetworkException bnex) {
            bnex.printStackTrace();
        }
    }
    
    private static void playing_parameter_learning() {
        final long SAMPLES = 1000 * 1000;
        try {
            System.out.println("Parameter estimation using maximum likelihood");
            BayesianNetwork bn = BayesianNetwork.loadFromFile("../../networks/sprinkler.net");
            System.out.println("Original factors (~CPDs) in all nodes:");
            System.out.println(bn.dumpCPTs());
            System.out.print("\n\n");
            // sample the network
            System.out.println(String.format("Producing dataset with %d samples (to memory)...", SAMPLES));
            DatasetCreationSampler datasetSampler = new DatasetCreationSampler(bn);
            SamplingController samplingController = new SamplingController(SAMPLES);
            datasetSampler.sample(samplingController);
            Dataset dataset = datasetSampler.getDataset();
            // attempt to compute CPDs for all nodes based on the dataset
            System.out.println("Computing CPDs using MLE...");
            BayesianNetwork bnLearnt = ParameterLearner.learnMLE(bn, dataset);
            // write the resulting CPDs
            System.out.println("Resulting factors (~CPDs) in all nodes:");
            System.out.println(bnLearnt.dumpCPTs());
            
            // TODO other methods than MLE
        }
        catch(BayesianNetworkException bnex) {
            bnex.printStackTrace();
        }
        catch(OutOfMemoryError oome) {
            oome.printStackTrace();
        }
    }
    
    private static void benchmark_parameter_learningMLEVSBayes() {
        // take network with independent variables (no connections) and check the mutual information
        final long SAMPLES_MIN = 50,
                   SAMPLES_MAX = 5000,
                   STEP_COUNT = 30;
        final double SAMPLES_EXP_STEP = Math.pow((double)SAMPLES_MAX / SAMPLES_MIN, 1.0 / (STEP_COUNT - 1));
        String[] headers = {"#no_of_samples", "mle_error", "bayes_1_error", "bayes_2.5_error", "bayes_5_error", "bayes_10_error", "bayes_20_error"};
        CmdlineTable table = new CmdlineTable(headers, 5, false);
        try {
            BayesianNetwork bnOriginal = BayesianNetwork.loadFromFile("../../networks/alarm.net");
            double samplesDbl = SAMPLES_MIN;
            for(int i = 0 ; i < STEP_COUNT ; i++, samplesDbl *= SAMPLES_EXP_STEP) {
                long samples = Math.round(samplesDbl);
                // sample the original network
                DatasetCreationSampler datasetSampler = new DatasetCreationSampler(bnOriginal);
                SamplingController samplingController = new SamplingController(samples);
                datasetSampler.sample(samplingController);
                Dataset dataset = datasetSampler.getDataset();
                // learn the parameters using MLE and Bayesian estimation with various equivalent sample sizes
                LinkedList<BayesianNetwork> networks = new LinkedList<BayesianNetwork>();
                networks.addLast(ParameterLearner.learnMLE(bnOriginal, dataset));
                networks.addLast(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, dataset, 1.0));
                networks.addLast(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, dataset, 2.5));
                networks.addLast(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, dataset, 5.0));
                networks.addLast(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, dataset, 10.0));
                networks.addLast(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, dataset, 20.0));
                /*System.out.println("Dump of original network:");
                System.out.println(bnOriginal.dumpCPTs());
                System.out.println("=======================================");
                System.out.println("Dump of MLE network:");
                System.out.println(ParameterLearner.learnMLE(bnOriginal, dataset).dumpCPTs());
                System.out.println("=======================================");
                System.out.println("Dump of Bayes(1) network:");
                System.out.println(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, dataset, 1.0).dumpCPTs());
                System.out.println("=======================================");
                System.out.println("Dump of Bayes(5) network:");
                System.out.println(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, dataset, 5.0).dumpCPTs());
                if(1==1)break;*/
                // produce one line of output
                System.err.printf("computing case for %d samples...\n", samples);
                LinkedList<Object> dataRowList = new LinkedList<Object>();
                dataRowList.add(samples);
                for(BayesianNetwork bnLearned : networks) {
                    Double entropy = Toolkit.networkDistanceRelativeEntropy2(bnOriginal, bnLearned);
                    dataRowList.addLast(entropy);
                }
                Object[] entropyRow = dataRowList.toArray();
                table.addRow(entropyRow);
            }
            System.out.println("# Benchmark of parameter learning quality (ICU network, MLE & Bayesian estimation, relative entropy)");
            System.out.println(table.toString());
        }
        catch(BayesianNetworkException bnex) {
            bnex.printStackTrace();
        }
    }
    
    private static void playing_structure_learning() {
        final long SAMPLES = 100;
        final long MAX_ITERATIONS = 300;
        final double TABU_LIST_RELATIVE_SIZE = 0.3;
        final double RANDOM_RESTART_STEPS_RELATIVE = 0.5;
        try {
            long timeStart, timeEnd;
            System.out.println("Structure learning");
            // sample some original network
            System.out.println("Loading original network...");
            BayesianNetwork bnOriginal = BayesianNetwork.loadFromFile("../../networks/sprinkler.net");
            int N = bnOriginal.getNodeCount();
            int maxAlterations = 2 * (N * (N - 1) / 2);
            int TABU_LIST_SIZE = (int)(maxAlterations * TABU_LIST_RELATIVE_SIZE),
                RANDOM_RESTART_STEPS = (int)(maxAlterations * RANDOM_RESTART_STEPS_RELATIVE);
            // sample the network
            System.out.println(String.format("Producing dataset with %d samples (to memory)...", SAMPLES));
            DatasetCreationSampler datasetSampler = new DatasetCreationSampler(bnOriginal);
            SamplingController samplingController = new SamplingController(SAMPLES);
            datasetSampler.sample(samplingController);
            Dataset dataset = datasetSampler.getDataset();
            
            LearningController controller = new LearningController(MAX_ITERATIONS);
            BayesianNetwork bnInitial = BayesianNetwork.loadFromFile("../../networks/sprinkler-empty.net");
            
            // put the dataset cache in place
            int LRU_CACHE_SIZE = N + 3 * (N * (N - 1) / 2);
            LRU_CACHE_SIZE = (int)(LRU_CACHE_SIZE * 1.2); // 20 % edge
            CachedDataset cachedDataset = new CachedDataset(dataset, LRU_CACHE_SIZE);
            
            // learn using likelihood score
            /*System.out.println("Learning structure by likelihood score...");
            //BayesianNetwork bnInitial = new BayesianNetwork(dataset.getVariables());
            ScoringMethod likelihoodScoringMethod = new LikelihoodScoringMethod(dataset);
            StructureLearningAlgorithm likelihoodLearningAlgorithm = new HillClimbLearningAlgorithm(likelihoodScoringMethod);
            BayesianNetwork bnResultLikelihood = likelihoodLearningAlgorithm.learn(bnInitial, controller);
            System.out.println("Learning done!");
            System.out.println("best structure:");
            System.out.println(bnResultLikelihood.dumpStructure());
            System.out.println("best total score: " + likelihoodScoringMethod.absoluteScore(bnResultLikelihood));
            System.out.println("");*/
            
            // learn using likelihood score
            timeStart = System.currentTimeMillis();
            System.out.println("Learning structure by BIC score...");
            ScoringMethod bicScoringMethod = new BICScoringMethod(cachedDataset);
            StructureLearningAlgorithm learningAlgorithm = new TabuSearchLearningAlgorithm(bicScoringMethod, TABU_LIST_SIZE, RANDOM_RESTART_STEPS);
            BayesianNetwork bnResultBIC = learningAlgorithm.learn(bnInitial, controller);
            timeEnd = System.currentTimeMillis();
            System.out.printf("Learning done! (took %.2f s)\n", (timeEnd - timeStart) / 1000.0);
            System.out.println("best structure:");
            System.out.println(bnResultBIC.dumpStructure());
            System.out.println("best total score: " + bicScoringMethod.absoluteScore(bnResultBIC));
        }
        catch(BayesianNetworkException bnex) {
            bnex.printStackTrace();
        }
    }
}
