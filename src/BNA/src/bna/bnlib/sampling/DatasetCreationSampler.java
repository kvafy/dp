// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/18

package bna.bnlib.sampling;

import bna.bnlib.*;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Sampler for creating an artificial dataset from given BN and writing it to a file on the fly.
 */
public class DatasetCreationSampler implements Sampler {
    private String outputFilename;
    private SampleProducer sampleProducer;
    private static final String RECORD_SEPARATOR = ";";
    private static final String VARIABLE_VALUE_SEPARATOR = "|";

    public DatasetCreationSampler(BayesianNetwork bn, String outputFilename) {
        this.outputFilename = outputFilename;
        // generate a weighted sample producer that will sample all variables in the network,
        // ie. for a query P(AllVariables)
        this.sampleProducer = new WeightedSampleProducer(
                bn,
                bn.getVariables(),
                new Variable[0],
                new Variable[0],
                new int[0]);
    }

    /** Perform sampling according to given controller. */
    @Override
    public void sample(SamplingController controller) {
        int[] sampledVarsValues = new int[this.sampleProducer.sampledVars.length]; // to this array variables are sampled
        
        FileWriter writer = null;
        try {
            writer = new FileWriter(this.outputFilename);
            this.printFileHeader(writer);
            
            int sampleNumber = 0;
            this.sampleProducer.initializeSample(sampledVarsValues);
            while(!controller.stopFlag() && sampleNumber < controller.maxSamples()) {
                this.sampleProducer.produceSample(sampledVarsValues);
                this.printFileEntry(writer, sampledVarsValues);
                sampleNumber++;
            }
            writer.close();
        }
        catch(IOException ioex) {
            String msg = String.format("Error manipulating the output dataset file while sampling: %s", ioex.getMessage());
            throw new BayesianNetworkRuntimeException(msg);
        }
    }
    
    private void printFileHeader(FileWriter writer) throws IOException {
        boolean firstRecord = true;
        // print variable names
        for(Variable x : this.sampleProducer.sampledVars) {
            if(!firstRecord)
                writer.write(RECORD_SEPARATOR);
            firstRecord = false;
            writer.write(x.getName());
        }
        writer.write(System.lineSeparator());
        // print variable values
        firstRecord = true;
        for(Variable x : this.sampleProducer.sampledVars) {
            if(!firstRecord)
                writer.write(RECORD_SEPARATOR);
            firstRecord = false;
            boolean firstVariableValue = true;
            for(String value : x.getValues()) {
                if(!firstVariableValue)
                    writer.write(VARIABLE_VALUE_SEPARATOR);
                firstVariableValue = false;
                writer.write(value);
            }
        }
    }
    
    private void printFileEntry(FileWriter writer, int[] sampleAssignment) throws IOException {
        writer.write(System.lineSeparator());
        for(int i = 0 ; i < sampleAssignment.length ; i++) {
            if(i != 0)
                writer.write(RECORD_SEPARATOR);
            writer.write(String.valueOf(sampleAssignment[i]));
        }
    }
}
