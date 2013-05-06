// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/28

package bna.bnlib.io;

import bna.bnlib.*;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Implementation of Bayesian network file writer for graphviz files.
 */
public class BayesianNetworkGraphvizFileWriter extends BayesianNetworkFileWriter {
    public BayesianNetworkGraphvizFileWriter(String filename) {
        super(filename);
    }
    
    @Override
    public void save(BayesianNetwork bn) throws BNLibIOException {
        final String NL = System.getProperty("line.separator");
        FileWriter writer = null;
        this.validateNetwork(bn);
        
        try {
            writer = new FileWriter(this.filename);
            writer.write("digraph bn {" + NL);
            
            // structure
            for(Node node : bn.getNodes()) {
                Variable parent = node.getVariable();
                for(Variable child : node.getChildVariables()) {
                    writer.write(String.format("    \"%s\" -> \"%s\";" + NL, parent.getName(), child.getName()));
                }
            }
            writer.write("}" + NL);
        }
        catch(IOException ex) {
            throw new BNLibIOException("The following IO exception occured: " + ex.getMessage());
        }
        finally {
            try {
                if(writer != null)
                    writer.close();
            }
            catch(IOException ex) {}
        }
    }
    
    private void validateNetwork(BayesianNetwork bn) throws BNLibIOException {
        if(bn == null)
            throw new BNLibIOException("The network may not be null.");
    }
}
