// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/26

package bna.bnlib.sampling;

import bna.bnlib.*;
import java.util.regex.*;
import java.util.ArrayList;


/**
 * Package-private class to parse a textual query of the form "P(X | Y, E = e)".
 * The parsed values are stored to arrays X, Y, E, e. If an error occurs,
 * BayesianNetworkException is thrown.
 * <p>
 * Syntax description:
 * The general "P(...)" structure must be met. X is a non-empty comma separated
 * list of variable names (case sensitive). Y is a possibly empty comma separated
 * list of variable names. The evidence part consists of comma separated records
 * in the form <variable-name> = <variable-value>. Around commas, "|" and "="
 * symbols may be additional whitespaces.
 * <p>
 * Examples:
 * P(RAIN | WETGRASS = TRUE)
 * P(RAIN | CLOUDY)
 * P(SPRINKLER)
 */
class ParsedQuery {
    BayesianNetwork bn;
    String query;
    // the parsed result
    Variable[] X, Y, E;
    int[] e;
    
    private final String varnameRegex = "[^,|=\\s-]+",
                         varvalueRegex = "[^,|=\\s-]+";
    
    
    public ParsedQuery(BayesianNetwork bn, String query) throws BayesianNetworkException {
        this.bn = bn;
        this.query = query;
        this.parse();
    }
    
    private void parse() throws BayesianNetworkException {
        String varnameListRegex = String.format("%s(?:\\s*,\\s*%s)*", varnameRegex, varnameRegex);
        String varnassignmentListRegex = String.format("%s\\s*=\\s*%s(?:\\s*,\\s*%s\\s*=\\s*%s)*", varnameRegex, varvalueRegex, varnameRegex, varvalueRegex);
        String queryRegex = 
                "P\\("
                + "(" + varnameListRegex + ")"      // \1 ~ X variables
                + "(?:"                             // "given" part of query (optional)
                    + "\\s*\\|\\s*"
                    + "(" + varnameListRegex + ")?" // \2 ~ Y variables (optional)
                    // optional Ei = ei part, possibly preceeded by a comma separating Y from E
                    + "(?:"
                        // handle separating comma between Y and E by negative lookbehind (?<!R)
                        + "(?:" + "(?<!\\|\\s?)\\s*,\\s*" + "|" + "(?<=\\|\\s?)\\s*" + ")"
                        + "(" + varnassignmentListRegex + ")?" // \3 ~ E = e variables (optional)
                    + ")?"
                + ")?"
                // disallow by lookbehind the case P(X | <nothing>)
                + "(?<!\\|\\s?)"
                + "\\)";
        Pattern queryPattern = Pattern.compile(queryRegex);
        Matcher queryMatcher = queryPattern.matcher(query);
        if(!queryMatcher.matches())
            throw new BayesianNetworkException("QueryParser: Query is invalid.");
        this.parseX(queryMatcher.group(1));
        this.parseY(queryMatcher.group(2));
        this.parseEe(queryMatcher.group(3));
    }
    
    private void parseX(String strX) throws BayesianNetworkException {
        if(strX == null)
            throw new BayesianNetworkException("QueryParser: There must be at least one X variable.");
        
        ArrayList<Variable> listX = new ArrayList<Variable>();
        this.parseVariableList(strX, listX);
        this.X = new Variable[listX.size()];
        listX.toArray(this.X);
    }
    
    private void parseY(String strY) throws BayesianNetworkException {
        if(strY == null) {
            this.Y = new Variable[0];
            return;
        }
        
        ArrayList<Variable> listY = new ArrayList<Variable>();
        this.parseVariableList(strY, listY);
        this.Y = new Variable[listY.size()];
        listY.toArray(this.Y);
    }
    
    private void parseEe(String strEe) throws BayesianNetworkException {
        if(strEe == null) {
            this.E = new Variable[0];
            this.e = new int[0];
            return;
        }
        ArrayList<Variable> listE = new ArrayList<Variable>();
        ArrayList<Integer> liste = new ArrayList<Integer>();
        this.parseEvidence(strEe, listE, liste);
        this.E = new Variable[listE.size()];
        this.e = new int[liste.size()];
        for(int i = 0 ; i < listE.size() ; i++) {
            this.E[i] = listE.get(i);
            this.e[i] = liste.get(i);
        }
    }
    
    private void parseVariableList(String varlistStr, ArrayList<Variable> varlist) throws BayesianNetworkException {
        varlistStr = varlistStr.trim();
        for(String varName : varlistStr.split("\\s*,\\s*")) {
            // check if the variable is in the network
            try {
                Variable var = this.bn.getVariable(varName);
                varlist.add(var);
            }
            catch(BayesianNetworkRuntimeException bne) {
                throw new BayesianNetworkException("QueryParser: " + bne.getMessage());
            }
        }
    }
    
    private void parseEvidence(String EeStr, ArrayList<Variable> varlist, ArrayList<Integer> valuelist) throws BayesianNetworkException {
        EeStr = EeStr.trim();
        for(String assignment : EeStr.split("\\s*,\\s*")) {
            // check if the variable is in the network
            try {
                String[] assignmentParsed = assignment.split("\\s*=\\s*");
                String varName = assignmentParsed[0],
                       valueStr = assignmentParsed[1];
                Variable var = this.bn.getVariable(varName);
                int value = var.getValueIndex(valueStr);
                varlist.add(var);
                valuelist.add(value);
            }
            catch(BayesianNetworkRuntimeException bne) {
                throw new BayesianNetworkException("QueryParser: " + bne.getMessage());
            }
        }
    }
}
