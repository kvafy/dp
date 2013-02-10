
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
            BayesianNetwork bn = BayesianNetwork.loadFromFile("../../networks/sprinkler.net");
            
            
            // weighted sampling P(RAIN | WETGRASS = TRUE)
            // need 1) BayesianNetworkWeightedSampler
            //      2) SamplingController
            Variable[] X = {bn.getVariable("RAIN")};
            Variable[] Y = {};
            Variable[] E = {bn.getVariable("WETGRASS")};
            int[] e = {1};
            BayesianNetworkSampler sampler = new BayesianNetworkWeightedSampler(bn, X, Y, E, e);
            SamplingController samplingController = new SamplingController(100000);
            
            sampler.sample(samplingController);
            
            Factor samples = sampler.getSamplesCounterNormalized();
            // write out
            System.out.println("sampleCounter:");
            System.out.println(samples.toString());
        }
        catch(BayesianNetworkException bnex) {
            bnex.printStackTrace();
        }
    }
}
