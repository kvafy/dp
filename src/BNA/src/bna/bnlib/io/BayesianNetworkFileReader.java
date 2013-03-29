// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib.io;

import bna.bnlib.*;
import bna.bnlib.misc.LRUCache;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Template method object for reading a Bayesian network specification from file.
 */
public abstract class BayesianNetworkFileReader {
    protected String filename;
    private LRUCache<String, Pattern> patternCache;
    
    public BayesianNetworkFileReader(String filename) {
        this.filename = filename;
        this.patternCache = new LRUCache<String, Pattern>(20);
    }
    
    /** Template method to load a BN from network. */
    public final BayesianNetwork load() throws BNLibIOException {
        this.patternCache.clear();
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

            bn.validate();
            return bn;
        }
        catch(BNLibIllegalStructuralModificationException ex) {
            throw new BNLibIOException("The network structure in given file is invalid (variables cannot be connected as specified).");
        }
        catch(BNLibIllegalNetworkSpecificationException ex) {
            throw new BNLibIOException(ex.getMessage());
        }
        catch(BNLibIllegalCPDException ex) {
            throw new BNLibIOException("CPDs specification is invalid.");
        }
        finally {
            this.patternCache.clear();
        }
    }
    
    boolean expectInput(ParsingContext context, String regexp, boolean canFail) throws IOException, BNLibIOException {
        final int MAX_TOKEN_LENGTH = 80;
        StringBuilder dataRead = new StringBuilder();
        String regexpWithWhitespaces = "[\\s\n\r]*" + regexp + "[\\s\n\r]*";
        Pattern pattern = this.compileRegexp(regexpWithWhitespaces);
        boolean unsuccesfull = true;
        
        context.input.mark(Integer.MAX_VALUE);
        while(dataRead.toString().trim().length() < MAX_TOKEN_LENGTH) {
            if(pattern.matcher(dataRead.toString()).matches()) {
                // read as much data as possible until the regexp matches data read
                while(true) {
                    context.input.mark(Integer.MAX_VALUE);
                    int nextchar = context.input.read();
                    if(nextchar != -1 && pattern.matcher(dataRead.toString() + String.valueOf((char)nextchar)).matches())
                        dataRead.append((char)nextchar);
                    else {
                        context.input.reset();
                        break;
                    }
                }
                unsuccesfull = false;
                break; // we have dataRead mathing our regexp
            }
            int nextchar = context.input.read();
            if(nextchar == -1)
               break;
            else
                dataRead.append((char)nextchar);
        }
        
        if(!unsuccesfull) {
            context.line += this.countOccurences("\n", dataRead.toString());
            context.token = dataRead.toString().trim();
            return true;
        }
        else {
            if(canFail) {
                context.input.reset();
                return false;
            }
            else {
                String msg = String.format("Unparsable input - expected regexp \"%s\" but got \"%s\".",
                                           regexp, dataRead.toString().substring(0, 10));
                throw new BNLibIOException(msg);
            }
        }
    }
    
    private Pattern compileRegexp(String regexp) {
        Pattern pattern = this.patternCache.get(regexp);
        if(pattern == null) {
            pattern = Pattern.compile(regexp, Pattern.DOTALL);
            this.patternCache.put(regexp, pattern);
        }
        return pattern;
    }
    
    private int countOccurences(String needle, String haystack) {
        int count = 0;
        int pos = haystack.indexOf(needle);
        while(pos != -1) {
            count++;
            pos = haystack.indexOf(needle, pos + needle.length());
        }
        return count;
    }
    
    
    // template method pattern: methods used within the load() method
    
    public abstract Variable[] readVariables() throws BNLibIOException;
    public abstract Map<String, String[]> readDependencies() throws BNLibIOException;
    public abstract Map<String, double[]> readProbabilities() throws BNLibIOException;
}

class ParsingContext {
    InputStream input;
    String token; // last token read
    int line;
    
    public ParsingContext(InputStream input) {
        this.input = input;
        this.line = 1;
    }
}