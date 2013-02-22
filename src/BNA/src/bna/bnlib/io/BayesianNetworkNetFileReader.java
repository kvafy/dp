// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib.io;

import bna.bnlib.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public class BayesianNetworkNetFileReader extends BayesianNetworkFileReader {
    private boolean parsed = false;
    private ArrayList<Variable> variables;
    private HashMap<String, String[]> parents;
    private HashMap<String, Double[]> probabilities;
    
    public BayesianNetworkNetFileReader(String filename) {
        super(filename);
        this.parsed = false;
        this.variables = new ArrayList<Variable>();
        this.parents = new HashMap<String, String[]>();
        this.probabilities = new HashMap<String, Double[]>();
    }

    @Override
    public Variable[] readVariables() throws BayesianNetworkException {
        if(!this.parsed)
            parse();
        Variable[] variablesArray = new Variable[this.variables.size()];
        return this.variables.toArray(variablesArray);
    }

    @Override
    public Map<String, String[]> readDependencies() throws BayesianNetworkException {
        if(!this.parsed)
            parse();
        return this.parents;
    }

    @Override
    public Map<String, Double[]> readProbabilities() throws BayesianNetworkException {
        if(!this.parsed)
            parse();
        return this.probabilities;
    }
    
    private void parse() throws BayesianNetworkException {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(this.filename));
            scanner.useDelimiter("[\\s()]+");
            // net 
            // { 
            // }
            scanner.next("net");
            scanner.next("\\{");
            scanner.next("\\}");
            
            // node HISTORY 
            // {
            // states = ( "TRUE" "FALSE" );
            // }
            while(scanner.hasNext("node")) {
                String nodeName;
                ArrayList<String> nodeValues = new ArrayList<String>();
                
                scanner.next("node");
                nodeName = scanner.next();
                scanner.next("\\{");
                scanner.next("states");
                scanner.next("=");
                //scanner.next("\\(");
                while(scanner.hasNext("\"\\w+\"")) {
                    String quotedValue = scanner.next();
                    nodeValues.add(unquote(quotedValue));
                }
                scanner.next(";");
                scanner.next("\\}");
                
                Variable var = new Variable(nodeName, nodeValues);
                this.variables.add(var);
            }
            
            // potential ( CVP | LVEDVOLUME ) 
            // {
            // data = ((0.95 0.04 0.01)(0.04 0.95 0.01)(0.01 0.29 0.70)) ;
            // }
            while(scanner.hasNext("potential")) {
                scanner.next("potential");
                //scanner.next("\\(");
                String var = scanner.next();
                ArrayList<String> varParentsList = new ArrayList<String>();
                if(scanner.hasNext("\\|")) {
                    scanner.next("\\|");
                    String token;
                    while(!scanner.hasNext("\\{")) {
                        token = scanner.next();
                        varParentsList.add(token);
                    }
                }
                
                scanner.next("\\{");
                scanner.next("data");
                scanner.next("=");
                ArrayList<Double> probabilityList = new ArrayList<Double>();
                while(true) {
                    String token = scanner.next();
                    if(token.equals(";"))
                        break;
                    Double value = Double.parseDouble(token);
                    probabilityList.add(value);
                }
                scanner.next("\\}");
                // save the dependencies
                String[] varParentsArray = new String[varParentsList.size()];
                varParentsList.toArray(varParentsArray);
                this.parents.put(var, varParentsArray);
                // save the probability vector
                Double[] probabilityArray = new Double[probabilityList.size()];
                probabilityList.toArray(probabilityArray);
                this.probabilities.put(var, probabilityArray);
            }
            
            if(scanner.hasNext())
                throw new BayesianNetworkException("Something is left at the footer of input file.");
            
            this.parsed = true; // everything ok
        }
        catch(IOException ioex) {
            throw new BayesianNetworkException("IO Error while reading BN from a file: " + ioex.getMessage());
        }
        catch(NoSuchElementException nseex) { // when scanner has other token than it expects
            throw new BayesianNetworkException("Invalid format of BN specification file.");
        }
        catch(NumberFormatException nfe) {
            throw new BayesianNetworkException("Invalid number format in BN specification file.");
        }
    }
    
    private String unquote(String quoted) {
        return quoted.substring(1, quoted.length() - 1);
    }
}

