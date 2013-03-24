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
public class DatafileCreationSampler extends Sampler {
    private static final String RECORD_SEPARATOR = ";";
    private static final String VARIABLE_VALUE_SEPARATOR = "|";
    
    private String outputFilename;
    private FileWriter fileWriter;

    
    public DatafileCreationSampler(BayesianNetwork bn, String outputFilename) {
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
        this.outputFilename = outputFilename;
        this.fileWriter = null;
    }
    
    @Override
    protected void registerSample(int[] XYVarsValues, double sampleWeight) {
        if(sampleWeight != 1.0)
            throw new BNLibInternalException("Internal error: Weight of a sample for datafile is not 1.0.");
        try {
            this.printFileEntry(XYVarsValues);
        }
        catch(IOException ioex) {
            this.fileWriter = null;
            throw new BNLibInternalException("Error manipulating dataset output file: " + ioex.getMessage());
        }
    }

    @Override
    protected void presamplingActions() {
        try {
            this.fileWriter = new FileWriter(this.outputFilename);
            this.printFileHeader();
        }
        catch(IOException ioex) {
            this.fileWriter = null;
            throw new BNLibInternalException("Error manipulating dataset output file: " + ioex.getMessage());
        }
    }

    @Override
    protected void postsamplingActions() {
        try {
            this.fileWriter.close();
        }
        catch(IOException ioex) {
            throw new BNLibInternalException("Error manipulating dataset output file: " + ioex.getMessage());
        }
        finally {
            this.fileWriter = null;
        }
    }
    
    private void printFileHeader() throws IOException {
        boolean firstRecord = true;
        // print variable names
        for(Variable x : this.sampleProducer.sampledVars) {
            if(!firstRecord)
                this.fileWriter.write(RECORD_SEPARATOR);
            firstRecord = false;
            this.fileWriter.write(x.getName());
        }
        this.fileWriter.write(System.lineSeparator());
        // print variable values
        firstRecord = true;
        for(Variable x : this.sampleProducer.sampledVars) {
            if(!firstRecord)
                this.fileWriter.write(RECORD_SEPARATOR);
            firstRecord = false;
            boolean firstVariableValue = true;
            for(String value : x.getValues()) {
                if(!firstVariableValue)
                    this.fileWriter.write(VARIABLE_VALUE_SEPARATOR);
                firstVariableValue = false;
                this.fileWriter.write(value);
            }
        }
    }
    
    private void printFileEntry(int[] sampleAssignment) throws IOException {
        this.fileWriter.write(System.lineSeparator());
        for(int i = 0 ; i < sampleAssignment.length ; i++) {
            if(i != 0)
                this.fileWriter.write(RECORD_SEPARATOR);
            String[] ithVariableValues = this.XYVars[i].getValues();
            this.fileWriter.write(ithVariableValues[sampleAssignment[i]]);
        }
    }
}
