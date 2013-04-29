// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/21

package bna.bnlib.io;

import bna.bnlib.*;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Implementation of Bayesian network file writer for .net files.
 */
public class BayesianNetworkNetFileWriter extends BayesianNetworkFileWriter {
    public BayesianNetworkNetFileWriter(String filename) {
        super(filename);
    }
    
    @Override
    public void save(BayesianNetwork bn) throws BNLibIOException {
        final String NL = System.getProperty("line.separator");
        FileWriter writer = null;
        this.validateNetwork(bn);
        
        try {
            writer = new FileWriter(this.filename);
            // "net" section
            writer.write("net" + NL + "{" + NL + "}" + NL);
            
            // variables specification
            for(Variable var : bn.getVariables()) {
                writer.write("node " + var.getName() + NL + "{" + NL);
                writer.write("  states = ( ");
                for(String value : var.getValues()) {
                    writer.write("\"" + value + "\"");
                    writer.write(" ");
                }
                writer.write(");" + NL);
                writer.write("}" + NL);
            }
            
            // structure and CPDs
            for(Node node : bn.getNodes()) {
                Factor factor = node.getFactor();
                Variable[] factorScope = factor.getScope();
                // the "potential" line
                writer.write("potential ( ");
                writer.write(factorScope[0].getName());
                writer.write(" ");
                if(factorScope.length > 1) {
                    writer.write("| ");
                    for(int i = 1 ; i < factorScope.length ; i++)
                        writer.write(factorScope[i].getName() + " ");
                }
                 writer.write(")" + NL);
                // the CPD representation
                writer.write("{" + NL);
                writer.write("  data = ");
                boolean justBegun = true;
                for(int[] assignment : factor) {
                    int leadingZeros = this.countLeadingZeros(assignment);
                    if(!justBegun)
                        this.writeNTimes(")", leadingZeros, writer); // close what overflowed
                    this.writeNTimes("(", leadingZeros, writer); // openers for new values
                    // probability value itself
                    if(leadingZeros == 0)
                        writer.write(" ");
                    writer.write(String.format("%f", factor.getProbability(assignment)).replace(',', '.'));
                    justBegun = false;
                }
                this.writeNTimes(")", factorScope.length, writer); // final closing
                writer.write(" ;" + NL);
                writer.write("}" + NL);
            }
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
        if(!bn.hasValidCPDs())
            throw new BNLibIOException("The network doesn't have valid CPDs.");
    }
    
    private int countLeadingZeros(int[] values) {
        int zeros = 0;
        for(int v : values) {
            if(v != 0)
                break;
            zeros++;
        }
        return zeros;
    }
    
    private void writeNTimes(String what, int n, FileWriter writer) throws IOException {
        for(int i = 0 ; i < n ; i++)
            writer.write(what);
    }
}
