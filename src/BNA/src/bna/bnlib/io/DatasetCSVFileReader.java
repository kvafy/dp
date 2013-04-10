// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/30

package bna.bnlib.io;

import bna.bnlib.BNLibIOException;
import bna.bnlib.Variable;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
    
    private void parse() throws BNLibIOException {
        ArrayList<ArrayList<String>> variablesValues = new ArrayList<ArrayList<String>>();
        ArrayList<int[]> dataRows = new ArrayList<int[]>();

        BufferedReader in = null;
        try {
            String line;
            in = new BufferedReader(new FileReader(this.filename));
            // parse header
            line = in.readLine();
            if(line == null || line.trim().length() == 0)
                throw new BNLibIOException("The file \"" + this.filename + "\" appears to be empty.");
            String[] variableNames = line.split(this.separator);
            int variableCount = variableNames.length;
            for(int i = 0 ; i < variableCount ; i++)
                variablesValues.add(new ArrayList<String>());
            
            // parse data rows
            int lineNumber = 1;
            while(true) {
                line = in.readLine();
                lineNumber++;
                if(line == null)
                    break;
                if(line.trim().isEmpty())
                    continue;
                String[] lineFields = line.split(this.separator);
                if(lineFields.length != variableCount) {
                    String msg = String.format("Unexpected number of records on line %d (expected %d, found %d).",
                                               lineNumber, variableCount, lineFields.length);
                    throw new BNLibIOException(msg);
                }
                int[] dataRow = new int[variableCount];
                for(int i = 0 ; i < variableCount ; i++) {
                    String iVarValue = lineFields[i];
                    ArrayList<String> iVarKnownValues = variablesValues.get(i);
                    int indexOfValue = iVarKnownValues.indexOf(iVarValue);
                    if(indexOfValue == -1) {
                        iVarKnownValues.add(iVarValue);
                        indexOfValue = iVarKnownValues.size() - 1;
                    }
                    dataRow[i] = indexOfValue;
                }
                dataRows.add(dataRow);
            }
            
            this.variables = new Variable[variableCount];
            for(int i = 0 ; i < variableCount ; i++)
                this.variables[i] = new Variable(variableNames[i], variablesValues.get(i));
            this.data = dataRows;
            this.parsed = true;
        }
        catch(FileNotFoundException ex) {
            throw new BNLibIOException("File \"" + this.filename + "\" was not found.");
        }
        catch(IOException ex) {
            throw new BNLibIOException("The following IOException occured: " + ex.getMessage());
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
