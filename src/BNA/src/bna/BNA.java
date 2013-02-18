
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
        try {
            // load BN from file
            System.out.println("Loading a network from file...");
            BayesianNetwork bn = BayesianNetwork.loadFromFile("../../networks/sprinkler.net");
            System.out.println("");
            
            
            // sampling and its variants
            final int SAMPLES_COUNT = 1000000;
            Variable[] X = {bn.getVariable("RAIN")};
            Variable[] Y = {};
            Variable[] E = {bn.getVariable("WETGRASS")};
            int[] e = {1};
            
            // weighted sampling P(RAIN | WETGRASS = TRUE)
            // need 1) BayesianNetworkWeightedSampler
            //      2) SamplingController
            System.out.println("Weighted sampling...");
            BayesianNetworkSampler weightedSampler = new BayesianNetworkWeightedSampler(bn, X, Y, E, e);
            SamplingController weightedSamplingController = new SamplingController(SAMPLES_COUNT);
            weightedSampler.sample(weightedSamplingController);
            Factor weightedSamples = weightedSampler.getSamplesCounterNormalized();
            System.out.println("sampleCounter:");
            System.out.println(weightedSamples.toString());
            System.out.println("");
            
            // MCMC sampling P(RAIN | WETGRASS = TRUE)
            // need 1) BayesianNetworkWeightedSampler
            //      2) SamplingController
            System.out.println("MCMC sampling...");
            BayesianNetworkSampler mcmcSampler = new BayesianNetworkMCMCSampler(bn, X, Y, E, e);
            SamplingController mcmcSamplingController = new SamplingController(SAMPLES_COUNT);
            mcmcSampler.sample(mcmcSamplingController);
            Factor mcmcSamples = mcmcSampler.getSamplesCounterNormalized();
            // write out
            System.out.println("sampleCounter:");
            System.out.println(mcmcSamples.toString());
            System.out.println("");
        }
        catch(BayesianNetworkException bnex) {
            bnex.printStackTrace();
        }
    }
}
