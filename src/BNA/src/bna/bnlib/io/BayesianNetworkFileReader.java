// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib.io;

import bna.bnlib.*;
import java.util.Map;


/**
 * Template method object for reading a Bayesian network specification from file.
 */
public abstract class BayesianNetworkFileReader {
    protected String filename;
    
    public BayesianNetworkFileReader(String filename) {
        this.filename = filename;
    }
    
    public final BayesianNetwork load() throws BNLibIOException {
        try {
            // 1) obtain names of all variables
            Variable[] variables = this.readVariables();
            // 2) create a BN with nodes with no dependencies
            BayesianNetwork bn = new BayesianNetwork(variables);
            // 3) add dependencies among nodes
            Map<String, String[]> dependencies = this.readDependencies();
            for(String var : dependencies.keySet()) {
                String[] varParents = dependencies.get(var);
                for(String parent : varParents)
                    bn.addDependency(parent, var);
            }
            // 4) attach factors to nodes
            Map<String, double[]> probabilities = this.readProbabilities();
            for(String var : probabilities.keySet()) {
                double[] probVector = probabilities.get(var);
                bn.setCPT(var, probVector);
            }

            return bn;
        }
        catch(BNLibInvalidStructuralModificationException ex) {
            throw new BNLibIOException("The network structure in given file is invalid.");
        }
        catch(BNLibIllegalCPDException ex) {
            throw new BNLibIOException("Some of the CPDs is invalid.");
        }
    }
    
    
    // template method pattern: methods used within the load() method
    
    public abstract Variable[] readVariables() throws BNLibIOException;
    public abstract Map<String, String[]> readDependencies() throws BNLibIOException;
    public abstract Map<String, double[]> readProbabilities() throws BNLibIOException;
}
