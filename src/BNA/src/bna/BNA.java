/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
            
            
            // weighted sampling P(SPRINKLER | WETGRASS = TRUE)
            Variable[] XY = {bn.getVariable("RAIN")};
            Variable[] E = {bn.getVariable("WETGRASS")};
            int[] e = {1};
            BayesianNetworkSampler sampler = new BayesianNetworkWeightedSampler(bn, XY, E, e);
            SamplingController samplingController = new SamplingController(1000000);
            
            sampler.sample(samplingController);
            
            double[] samples = sampler.getSamplesCounter();
            // normalize
            double samplesSum = 0;
            for(double s : samples)
                samplesSum += s;
            System.out.println("sampleCounter:");
            for(double s : samples)
                System.out.println(String.format(" %.4f", s / samplesSum).replace(',', '.'));
            
            //BayesianNetwork bn = BayesianNetwork.loadFromFile("../../icu/cycle.net");
        }
        catch(BayesianNetworkException bnex) {
            bnex.printStackTrace();
        }
    }
}
