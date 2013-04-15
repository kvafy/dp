// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/30

package bna.bnlib.io;

import bna.bnlib.BNLibIOException;
import bna.bnlib.BNLibIllegalVariableSpecificicationException;
import bna.bnlib.BNLibNonexistentVariableValueException;
import bna.bnlib.Variable;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * Implementation of dataset file reader for CSV files.
 */
public class DatasetCSVFileReader extends DatasetFileReader {
    private String separator;
    private boolean parsed = false;
    private Variable[] variables = null;
    private List<int[]> data = null;
    
    
    public DatasetCSVFileReader(String csvFile, String separator) {
        super(csvFile);
        this.separator = separator;
    }
    
    private Variable parseVariableSpecification(String spec) throws BNLibIllegalVariableSpecificicationException {
        // each record has to be in the form "<var-name>(<var-value1>|...|<var-value-n>)"
        String patternStr = String.format("(%s)"           // \1 is variable name
                                        + "\\("
                                        + "(%s(:?\\|%s)*)" // \2 is list of possible values
                                        + "\\)",
                                          IOConfiguration.VARNAME_REGEX,
                                          IOConfiguration.VARVALUE_REGEX,
                                          IOConfiguration.VARVALUE_REGEX);
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(spec);
        if(!matcher.matches()) {
            String msg = String.format("\"%s\" is not a valid variable specification (must contain name and list of possible values).", spec);
            throw new BNLibIllegalVariableSpecificicationException(msg);
        }
        String varName = matcher.group(1);
        String[] varValues = matcher.group(2).split("\\|");
        return new Variable(varName, varValues);
    }
    
    private void parse() throws BNLibIOException {
        ArrayList<int[]> dataRows = new ArrayList<int[]>();

        BufferedReader in = null;
        int lineNumber = 1;
        try {
            String line;
            in = new BufferedReader(new FileReader(this.filename));
            // parse header (variable names and set of possible values)
            line = in.readLine();
            if(line == null || line.trim().length() == 0)
                throw new BNLibIOException("The file \"" + this.filename + "\" appears to be empty.");
            String[] variableSpecifications = line.split(this.separator);
            int variableCount = variableSpecifications.length;
            this.variables = new Variable[variableCount];
            for(int i = 0 ; i < variableCount ; i++)
                this.variables[i] = this.parseVariableSpecification(variableSpecifications[i]);
            
            // parse data rows
            while(true) {
                line = in.readLine();
                lineNumber++;
                if(line == null)
                    break;
                if(line.trim().isEmpty())
                    continue;
                String[] lineFields = line.split(this.separator);
                if(lineFields.length != variableCount) {
                    String msg = String.format("Line %d: Unexpected number of records - expected %d, found %d.",
                                               lineNumber, variableCount, lineFields.length);
                    throw new BNLibIOException(msg);
                }
                int[] dataRow = new int[variableCount];
                for(int i = 0 ; i < variableCount ; i++) {
                    String iVarValue = lineFields[i];
                    Variable iVar = this.variables[i];
                    int indexOfValue = iVar.getValueIndex(iVarValue); // throws BNLibNonexistentVariableValueException
                    dataRow[i] = indexOfValue;
                }
                dataRows.add(dataRow);
            }
            
            this.data = dataRows;
            this.parsed = true;
        }
        catch(FileNotFoundException ex) {
            throw new BNLibIOException("File \"" + this.filename + "\" was not found.");
        }
        catch(IOException ex) {
            throw new BNLibIOException("The following IOException occured: " + ex.getMessage());
        }
        catch(BNLibIllegalVariableSpecificicationException ex) {
            String msg = String.format("Line %d: %s", lineNumber, ex.getMessage());
            throw new BNLibIOException(msg);
        }
        catch(BNLibNonexistentVariableValueException ex) {
            String msg = String.format("Line %d: %s", lineNumber, ex.getMessage());
            throw new BNLibIOException(msg);
        }
        finally {
            try {
                if(in != null)
                    in.close();
            }
            catch(IOException ex) {
                throw new BNLibIOException("An IOException occured while closing the input file: " + ex.getMessage());
            }
        }
    }

    @Override
    protected Variable[] readVariables() {
        if(!this.parsed)
            this.parse();
        return this.variables;
    }

    @Override
    protected List<int[]> readDataRows() {
        if(!this.parsed)
            this.parse();
        return this.data;
    }
}
