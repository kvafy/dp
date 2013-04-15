
package bna;

import bna.bnlib.*;
import bna.bnlib.learning.*;
import bna.bnlib.misc.TextualTable;
import bna.bnlib.misc.Toolkit;
import bna.bnlib.sampling.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Class for on-the-fly testing and toying with the bnlib package.
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
        benchmark_parameter_learningMLEVSBayes();
        //benchmark_parameter_learningMLEVSBayesINSPECTION();
        //playing_structure_learning();
        //playing_structure_learning_evaluation();
        //playing_network_layout();
        //tabu_pars_optimization();
        
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
        finally {}
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
        finally {}
    }
    
    private static void benchmarkSampling_sequentialVSMultithreaded() {
        /* test settings */
        // which network to sample and what sampling method to use
        NetworkVariant networkVariant = NetworkVariant.SprinklerNet;
        SamplingVariant samplingVariant = SamplingVariant.WeightedSampling;
        // how many samples to generate in a single run
        final long SAMPLES_COUNT = 16 * 3 * 208400*2; // cca 20.000.000 but dividible by any used threadcount
        // how many runs to compute an average from
        final int RUNS_COUNT = 35,
                  TRIMM_COUNT = 5;
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
                //double timePerRun = computeMedian(runningTimes);
                double timePerRun = computeTrimmedMean(runningTimes, TRIMM_COUNT);
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
    
    private static double computeTrimmedMean(LinkedList<Double> values, int trimmCount) {
        values = new LinkedList<Double>(values); // make a local copy
        Collections.sort(values);
        for(int i = 0 ; i < trimmCount ; i++) {
            values.removeFirst();
            values.removeLast();
        }
        double sum = 0;
        for(double v : values)
            sum += v;
        return sum / values.size();
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
        finally {}
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
        catch(OutOfMemoryError oome) {
            oome.printStackTrace();
        }
    }
    
    private static void benchmark_parameter_learningMLEVSBayes() {
        // take network with independent variables (no connections) and check the mutual information
        final long SAMPLES_MIN = 10,
                   SAMPLES_MAX = 5000;
                   //STEP_COUNT = 100;
        final String NETWORK = "../../networks/hepar2.net";
        //final double SAMPLES_EXP_STEP = Math.pow((double)SAMPLES_MAX / SAMPLES_MIN, 1.0 / (STEP_COUNT - 1));
        
        String[] headers = {"#no_of_samples", "mle_error", "bayes_1_error", "bayes_5_error", "bayes_10_error", "bayes_20_error", "bayes_30_error", "bayes_40_error"};
        TextualTable table = new TextualTable(headers, 5, false);
        
        BayesianNetwork bnOriginal = BayesianNetwork.loadFromFile(NETWORK);
        // sample the original network
        DatasetCreationSampler datasetSampler = new DatasetCreationSampler(bnOriginal);
        SamplingController samplingController = new SamplingController(SAMPLES_MAX);
        datasetSampler.sample(samplingController);
        Dataset completeDataset = datasetSampler.getDataset();

        // infer probability distributions over Parents(X) for all X variables just once
        System.err.printf("precomputing probability distributions over Parents(X) for all X...\n");
        HashMap<Variable, Factor> distributionsOverParents = new HashMap<Variable, Factor>();
        for(Node node : bnOriginal.getNodes()) {
            if(node.getParentCount() > 0) {
                Factor distribution = Toolkit.inferJointDistribution(bnOriginal, node.getParentVariables());
                distributionsOverParents.put(node.getVariable(), distribution);
            }
        }
        
        /*double samplesDbl = SAMPLES_MIN;
        for(int i = 0 ; i < STEP_COUNT ; i++, samplesDbl *= SAMPLES_EXP_STEP) {
            // take only first "samples" samples from the completeDataset
            long samples = Math.round(samplesDbl);*/
        for(long samples = SAMPLES_MIN ; samples <= SAMPLES_MAX ; samples++) {
            // take only first "samples" samples from the completeDataset
            Dataset partialDataset = new Dataset(completeDataset.getVariables());
            for(int[] sample : completeDataset.getDataReadOnly().subList(0, (int)samples))
                partialDataset.addRecord(sample);
            // learn the parameters using MLE and Bayesian estimation with various equivalent sample sizes
            LinkedList<BayesianNetwork> networks = new LinkedList<BayesianNetwork>();
            networks.addLast(ParameterLearner.learnMLE(bnOriginal, partialDataset));
            networks.addLast(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, partialDataset, 1.0));
            networks.addLast(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, partialDataset, 5.0));
            networks.addLast(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, partialDataset, 10.0));
            networks.addLast(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, partialDataset, 20.0));
            networks.addLast(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, partialDataset, 30.0));
            networks.addLast(ParameterLearner.learnBayesianEstimationUniform(bnOriginal, partialDataset, 40.0));
            // produce one line of output
            System.err.printf("computing case for %d samples...\n", samples);
            LinkedList<Object> dataRowList = new LinkedList<Object>();
            dataRowList.add(samples);
            for(BayesianNetwork bnLearned : networks) {
                Double entropy = Toolkit.networkDistanceRelativeEntropy2(bnOriginal, distributionsOverParents, bnLearned);
                dataRowList.addLast(entropy);
            }
            Object[] entropyRow = dataRowList.toArray();
            table.addRow(entropyRow);
        }
        System.out.println("# Benchmark of parameter learning quality (" + NETWORK + ", MLE & Bayesian estimation, relative entropy)");
        System.out.println(table.toString());
    }
    
    private static void benchmark_parameter_learningMLEVSBayesINSPECTION() {
        // reason: why are such rapid decreases in KL-divergence for small networks?
        // => a samples manages to erase a zero from some CPD => lesser penalization
        
        // take network with independent variables (no connections) and check the mutual information
        final long SAMPLES_MIN = 10,
                   SAMPLES_MAX = 1000;
        final String NETWORK = "../../networks/asia.net";

        BayesianNetwork bnOriginal = BayesianNetwork.loadFromFile(NETWORK);
        // sample the original network
        DatasetCreationSampler datasetSampler = new DatasetCreationSampler(bnOriginal);
        SamplingController samplingController = new SamplingController(SAMPLES_MAX);
        datasetSampler.sample(samplingController);
        Dataset completeDataset = datasetSampler.getDataset();

        // infer probability distributions over Parents(X) for all X variables just once
        System.err.printf("precomputing probability distributions over Parents(X) for all X...\n");
        HashMap<Variable, Factor> distributionsOverParents = new HashMap<Variable, Factor>();
        for(Node node : bnOriginal.getNodes()) {
            if(node.getParentCount() > 0) {
                Factor distribution = Toolkit.inferJointDistribution(bnOriginal, node.getParentVariables());
                distributionsOverParents.put(node.getVariable(), distribution);
            }
        }
        
        BayesianNetwork bnPreviousMLE = null;
        Double klPreviousMLE = null;
        
        for(long samples = SAMPLES_MIN ; samples <= SAMPLES_MAX ; samples++) {
            // take only first "samples" samples from the completeDataset
            Dataset partialDataset = new Dataset(completeDataset.getVariables());
            for(int[] sample : completeDataset.getDataReadOnly().subList(0, (int)samples))
                partialDataset.addRecord(sample);
            // learn the parameters using MLE and Bayesian estimation with various equivalent sample sizes
            BayesianNetwork bnCurrentMLE = ParameterLearner.learnMLE(bnOriginal, partialDataset);
            Double klCurrentMLE = Toolkit.networkDistanceRelativeEntropy2(bnOriginal, distributionsOverParents, bnCurrentMLE);
            if(klPreviousMLE != null && klPreviousMLE - klCurrentMLE >= 0.15) {
                System.out.printf("jump of KL-divergence by %.2f at %d samples\n", klPreviousMLE - klCurrentMLE, samples);
                System.out.println("previous bn:");
                System.out.println(bnPreviousMLE.dumpCPTs());
                System.out.println("");
                System.out.println("new bn:");
                System.out.println(bnCurrentMLE.dumpCPTs());
                System.out.println("");
                System.out.println("");
                System.out.println("");
            }
            bnPreviousMLE = bnCurrentMLE;
            klPreviousMLE = klCurrentMLE;
        }        
    }
    
    private static void playing_structure_learning() {
        final long SAMPLES = 1000;
        final long MAX_ITERATIONS = 1000;
        final double TABU_LIST_RELATIVE_SIZE = 0.15;
        final int RANDOM_RESTART_STEPS = 20;
        final String NETWORK_FILE = "../../networks/child.net";
        //final String NETWORK_FILE = "../../networks/cancer.net";
        //final String NETWORK_FILE = "../../networks/asia.net";
        final int MAX_PARENT_COUNT = 3;
        try {
            long timeStart, timeEnd;
            System.out.println("Structure learning");
            // sample some original network
            System.out.println("Loading original network...");
            BayesianNetwork bnOriginal = BayesianNetwork.loadFromFile(NETWORK_FILE);
            int N = bnOriginal.getNodeCount();
            int maxAlterations = 2 * (N * (N - 1) / 2);
            int TABU_LIST_SIZE = (int)(maxAlterations * TABU_LIST_RELATIVE_SIZE);
            // sample the network
            System.out.println(String.format("Producing dataset with %d samples (to memory)...", SAMPLES));
            DatasetCreationSampler datasetSampler = new DatasetCreationSampler(bnOriginal);
            SamplingController samplingController = new SamplingController(SAMPLES);
            datasetSampler.sample(samplingController);
            Dataset dataset = datasetSampler.getDataset();
            
            LearningController controller = new LearningController(MAX_ITERATIONS);
            StructuralConstraints constraints = new StructuralConstraints(bnOriginal.getVariables());
            constraints.setMaxParentCount(MAX_PARENT_COUNT);
            BayesianNetwork bnInitial = bnOriginal.copyEmptyStructure();
            
            // put the dataset cache in place
            int LRU_CACHE_SIZE = N + 3 * (N * (N - 1) / 2); // TODO seems too small
            LRU_CACHE_SIZE = (int)(LRU_CACHE_SIZE * 2.0); // edge
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
            DecomposableScoringMethod bicScoringMethod = new BICScoringMethod(cachedDataset);
            StructureLearningAlgorithm learningAlgorithm = new TabuSearchLearningAlgorithm(bicScoringMethod, TABU_LIST_SIZE, RANDOM_RESTART_STEPS);
            BayesianNetwork bnResultBIC = learningAlgorithm.learn(bnInitial, controller, constraints);
            timeEnd = System.currentTimeMillis();
            System.out.printf("Learning done! (took %.2f s)\n", (timeEnd - timeStart) / 1000.0);
            System.out.println("best structure:");
            System.out.println(bnResultBIC.dumpStructure());
            System.out.println("best total score: " + bicScoringMethod.absoluteScore(bnResultBIC));
        }
        finally {}
    }
    
    private static void playing_structure_learning_evaluation() {
        //final String NETWORK_FILE = "../../networks/sprinkler.net";
        // - 1k samples, 1k iterations, 0.1 tabulist, 15 random restarts (!!!)
        //final String NETWORK_FILE = "../../networks/john_marry.net";
        // - [TODO] 1000 samples, 50k iterations, 0.2 tabulist, 15 random restarts
        //final String NETWORK_FILE = "../../networks/cancer.net";
        // - [TODO] 1000 samples, 50k iterations, 0.2 tabulist, 15 random restarts
        final String NETWORK_FILE = "../../networks/child.net";
        // - [TODO] 10k samples, 1k iterations, 0.2 tabulist, 50 random restarts
        final int RUN_COUNT = 100; // how many networks learn to make a statistics from
        
        // dataset parameters
        final long SAMPLES = 10 * 1000;
        // learning parameters - single iteration
        final long MAX_ITERATIONS = 1000;
        final double TABU_LIST_RELATIVE_SIZE = 0.15;
        final int RANDOM_RESTART_STEPS = 20;
        final int MAX_PARENT_COUNT = 3; // one more than the maximum expected parent count
        
        // sample some original network
        BayesianNetwork bnOriginal = BayesianNetwork.loadFromFile(NETWORK_FILE);
        int N = bnOriginal.getNodeCount();
        int maxAlterations = 2 * (N * (N - 1) / 2);
        int TABU_LIST_SIZE = (int)(maxAlterations * TABU_LIST_RELATIVE_SIZE);
        // sample the network
        System.out.println(String.format("Producing dataset with %d samples (to memory)...", SAMPLES));
        DatasetCreationSampler datasetSampler = new DatasetCreationSampler(bnOriginal);
        SamplingController samplingController = new SamplingController(SAMPLES);
        datasetSampler.sample(samplingController);
        Dataset dataset = datasetSampler.getDataset();
        
        StructureLearningStatistics statistics = new StructureLearningStatistics(bnOriginal.getVariables());
        for(int run = 0 ; run < RUN_COUNT ; run++) {
            System.out.printf("run no %d\n", run + 1);
            // put the dataset cache in place
            int LRU_CACHE_SIZE = N + 3 * (N * (N - 1) / 2); // TODO seems too small
            LRU_CACHE_SIZE = (int)(LRU_CACHE_SIZE * 2.0); // edge
            CachedDataset cachedDataset = new CachedDataset(dataset, LRU_CACHE_SIZE);
            // learn using likelihood score
            LearningController controller = new LearningController(MAX_ITERATIONS);
            StructuralConstraints constraints = new StructuralConstraints(bnOriginal.getVariables());
            constraints.setMaxParentCount(MAX_PARENT_COUNT);
            BayesianNetwork bnInitial = bnOriginal.copyEmptyStructure();
            DecomposableScoringMethod bicScoringMethod = new BICScoringMethod(cachedDataset);
            StructureLearningAlgorithm learningAlgorithm = new TabuSearchLearningAlgorithm(bicScoringMethod, TABU_LIST_SIZE, RANDOM_RESTART_STEPS);
            BayesianNetwork resultBN = learningAlgorithm.learn(bnInitial, controller, constraints);
            double resultBIC =  bicScoringMethod.absoluteScore(resultBN);
            statistics.registerLearntNetwork(resultBN, resultBIC);
        }
        statistics.report();
        
        System.out.println("\nMost probable network:");
        int LRU_CACHE_SIZE = N + 3 * (N * (N - 1) / 2); // TODO seems too small
            LRU_CACHE_SIZE = (int)(LRU_CACHE_SIZE * 2.0); // edge
        CachedDataset cachedDataset = new CachedDataset(dataset, LRU_CACHE_SIZE);
        ScoringMethod bicScoringMethod = new BICScoringMethod(cachedDataset);
        System.out.println(statistics.getMostProbableNetwork(bicScoringMethod).dumpStructure());
    }
    
    /*private static void tabu_pars_optimization() {
        //final String NETWORK_FILE = "../../networks/sprinkler.net";
        //final String NETWORK_FILE = "../../networks/john_marry.net";
        final String NETWORK_FILE = "../../networks/earthquake.net"; // 5 nodes
        //final String NETWORK_FILE = "../../networks/cancer.net"; // 5 nodes
        //final String NETWORK_FILE = "../../networks/asia.net";   // 8 nodes
        //final String NETWORK_FILE = "../../networks/child.net";  // 20 nodes
        //final String NETWORK_FILE = "../../networks/alarm.net";  // 37 nodes
        final int MAX_PARENT_COUNT = 3; // one more than the maximum expected parent count
        
        final int RUN_COUNT = 1000; // how many networks learn to make a statistics from
        // dataset parameters
        final long SAMPLES = 1000;//2000;
        // learning parameters - single iteration
        final long MAX_ITERATIONS = 1000;
        
        final double[] TABU_LIST_RELATIVE_SIZES = {0.025, 0.05, 0.075, 0.1, 0.15, 0.2, 0.25, 0.3};
        final double[] RANDOM_RESTART_RELATIVE_STEPS = {0.01, 0.02, 0.04, 0.08, 0.015, 0.03, 0.06, 0.1, 0.2, 0.3};
                
        // sample some original network
        BayesianNetwork bnOriginal = BayesianNetwork.loadFromFile(NETWORK_FILE);
        DatasetCreationSampler datasetSampler = new DatasetCreationSampler(bnOriginal);
        SamplingController samplingController = new SamplingController(SAMPLES);
        datasetSampler.sample(samplingController);
        Dataset dataset = datasetSampler.getDataset();
        
        int N = bnOriginal.getNodeCount();
        int MAX_ALTERATIONS = 2 * (N * (N - 1) / 2);
        System.out.printf("# %s, %d samples, %d runs, %d iterations/run, max parent count %d, alterations upper bound %d\n", NETWORK_FILE, SAMPLES, RUN_COUNT, MAX_ITERATIONS, MAX_PARENT_COUNT, MAX_ALTERATIONS);
        TextualTable successTable = new TextualTable(new String[]{"# rel_tabulist_size", "rel_rnd_steps", "success_freq"}, 6, false);
        System.err.printf("# %s, %d samples, %d runs, %d iterations/run, alterations upper bound %d\n", NETWORK_FILE, SAMPLES, RUN_COUNT, MAX_ITERATIONS, MAX_ALTERATIONS);
        TextualTable meanscoreTable = new TextualTable(new String[]{"# rel_tabulist_size", "rel_rnd_steps", "mean_score"}, 6, false);
        
        for(double randomRestartRelativeSteps : RANDOM_RESTART_RELATIVE_STEPS) {
            int randomRestartSteps = (int)(randomRestartRelativeSteps * MAX_ALTERATIONS);
            for(double tabulistRelativeSize : TABU_LIST_RELATIVE_SIZES) {
                StructureLearningStatistics statistics = new StructureLearningStatistics(bnOriginal.getVariables());
                for(int run = 0 ; run < RUN_COUNT ; run++) {
                    // put the dataset cache in place
                    int LRU_CACHE_SIZE = N + 3 * (N * (N - 1) / 2); // TODO seems too small
                    LRU_CACHE_SIZE = (int)(LRU_CACHE_SIZE * 2.0); // edge
                    CachedDataset cachedDataset = new CachedDataset(dataset, LRU_CACHE_SIZE);
                    // learn using likelihood score
                    LearningController controller = new LearningController(MAX_ITERATIONS);
                    StructuralConstraints constraints = new StructuralConstraints(bnOriginal.getVariables());
                    constraints.setMaxParentCount(MAX_PARENT_COUNT);
                    BayesianNetwork bnInitial = bnOriginal.copyEmptyStructure();
                    ScoringMethod bicScoringMethod = new BICScoringMethod(cachedDataset);
                    int TABU_LIST_SIZE = (int)(MAX_ALTERATIONS * tabulistRelativeSize);
                    StructureLearningAlgorithm learningAlgorithm = new TabuSearchLearningAlgorithm(bicScoringMethod, TABU_LIST_SIZE, randomRestartSteps);
                    BayesianNetwork resultBN = learningAlgorithm.learn(bnInitial, controller, constraints);
                    double resultBIC =  bicScoringMethod.absoluteScore(resultBN);
                    statistics.registerLearntNetwork(resultBN, resultBIC);
                }
                successTable.addRow(new Object[] {
                    new Double(tabulistRelativeSize),
                    new Double(randomRestartRelativeSteps),
                    new Double(statistics.getSuccessFrequency())
                });
                meanscoreTable.addRow(new Object[] {
                    new Double(tabulistRelativeSize),
                    new Double(randomRestartRelativeSteps),
                    new Double(statistics.getMeanScore())
                });
            }
            successTable.addRow(new Object[] {"", "", ""}); // blank line needed for gnuplot pm3d plotting
            meanscoreTable.addRow(new Object[] {"", "", ""}); // blank line needed for gnuplot pm3d plotting
        }
        System.out.println(successTable.toString());
        System.err.println(meanscoreTable.toString());
    }*/
}
