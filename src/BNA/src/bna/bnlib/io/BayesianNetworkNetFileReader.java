// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib.io;

import bna.bnlib.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class for reading specification of a BN in .net file format.
 */
public class BayesianNetworkNetFileReader extends BayesianNetworkFileReader {
    private boolean parsed = false;
    private ArrayList<Variable> variables;
    private HashMap<String, String[]> parents;
    private HashMap<String, double[]> probabilities;
    
    public BayesianNetworkNetFileReader(String filename) {
        super(filename);
        this.parsed = false;
        this.variables = new ArrayList<Variable>();
        this.parents = new HashMap<String, String[]>();
        this.probabilities = new HashMap<String, double[]>();
    }

    @Override
    public Variable[] readVariables() throws BNLibIOException {
        if(!this.parsed)
            parse();
        Variable[] variablesArray = new Variable[this.variables.size()];
        return this.variables.toArray(variablesArray);
    }

    @Override
    public Map<String, String[]> readDependencies() throws BNLibIOException {
        if(!this.parsed)
            parse();
        return this.parents;
    }

    @Override
    public Map<String, double[]> readProbabilities() throws BNLibIOException {
        if(!this.parsed)
            parse();
        return this.probabilities;
    }
    
    private void parse() throws BNLibIOException {
        final String NUMBER_REGEXP = "[-+0-9\\.e]+"; // must be this way because of the expectInput method
        ParsingContext context = null;
        try {
            context = new ParsingContext(new BufferedInputStream(new FileInputStream(this.filename)));
            // net 
            // { 
            // }
            this.expectInput(context, "net\\s*\\{[^}]*\\}", false);
            
            // node HISTORY 
            // {
            // states = ( "TRUE" "FALSE" );
            // }
            while(this.expectInput(context, "node", true)) {
                String nodeName;
                ArrayList<String> nodeValues = new ArrayList<String>();
                this.expectInput(context, IOConfiguration.VARNAME_REGEX, false);
                nodeName = context.token;
                this.expectInput(context, "\\{", false);
                this.expectInput(context, "states", false);
                this.expectInput(context, "=", false);
                //scanner.next("\\(");
                this.expectInput(context, "[()]*", true);
                while(this.expectInput(context, "\"" + IOConfiguration.VARVALUE_REGEX + "\"", true)) {
                    String quotedValue = context.token;
                    nodeValues.add(unquote(quotedValue));
                    this.expectInput(context, "[() ]*", true);
                }
                this.expectInput(context, ";", false);
                this.expectInput(context, "\\}", false);
                
                Variable var = new Variable(nodeName, nodeValues);
                this.variables.add(var);
            }
            
            // potential ( CVP | LVEDVOLUME ) 
            // {
            // data = ((0.95 0.04 0.01)(0.04 0.95 0.01)(0.01 0.29 0.70)) ;
            // }
            while(this.expectInput(context, "potential", true)) {
                this.expectInput(context, "\\(", false);
                this.expectInput(context, IOConfiguration.VARNAME_REGEX, false);
                String var = context.token;
                ArrayList<String> varParentsList = new ArrayList<String>();
                if(this.expectInput(context, "\\|", true)) {
                    boolean canFail = false;
                    while(this.expectInput(context, IOConfiguration.VARNAME_REGEX, canFail)) {
                        varParentsList.add(context.token);
                        canFail = true;
                    }
                }
                this.expectInput(context, "\\)", false);
                
                this.expectInput(context, "\\{", false);
                this.expectInput(context, "data", false);
                this.expectInput(context, "=", false);
                ArrayList<Double> probabilityList = new ArrayList<Double>();
                while(true) {
                    this.expectInput(context, "[() ]*", true);
                    
                    if(this.expectInput(context, ";", true))
                        break;
                    this.expectInput(context, NUMBER_REGEXP, false);
                    try {
                        Double value = Double.parseDouble(context.token);
                        probabilityList.add(value);
                    }
                    catch(NumberFormatException ex) {
                        throw new BNLibIOException(String.format("Invalid number \"%s\".", context.token));
                    }
                }
                this.expectInput(context, "\\}", false);
                // save the dependencies
                String[] varParentsArray = new String[varParentsList.size()];
                varParentsList.toArray(varParentsArray);
                this.parents.put(var, varParentsArray);
                // save the probability vector
                double[] probabilityArray = this.doubleListToPrimitiveArray(probabilityList);
                this.probabilities.put(var, probabilityArray);
            }
            
            //this.expectInput(context, "", true); // trailing whitespaces
            //if(context.input.ready())
            //    throw new BNLibIOException("Unparsed content left at the end of the input file.");
            
            context.input.close();
            this.parsed = true; // everything ok
        }
        catch(FileNotFoundException ex) {
            throw new BNLibIOException(String.format("File \"%s\" has not been found.", this.filename));
        }
        catch(IOException ex) {
            throw new BNLibIOException(String.format("The following IOException occured: %s", ex.getMessage()));
        }
        catch(BNLibIOException ex) {
            throw new BNLibIOException(String.format("Line %d: %s", context.line, ex.getMessage()));
        }
        catch(BNLibIllegalVariableSpecificicationException ex) {
            throw new BNLibIOException(String.format("Line %d: %s", context.line, ex.getMessage()));
        }
    }
    
    private double[] doubleListToPrimitiveArray(List<Double> list) {
        double[] array = new double[list.size()];
        int i = 0;
        for(double value : list)
            array[i++] = value;
        return array;
    }
    
    private String unquote(String quoted) {
        return quoted.substring(1, quoted.length() - 1);
    }
}

