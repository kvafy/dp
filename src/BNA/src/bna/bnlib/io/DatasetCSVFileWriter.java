// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/10

package bna.bnlib.io;

import bna.bnlib.BNLibIOException;
import bna.bnlib.Variable;
import bna.bnlib.learning.Dataset;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Implementation of dataset file writer for CSV files.
 */
public class DatasetCSVFileWriter extends DatasetFileWriter {
    private String separator;
    
    public DatasetCSVFileWriter(String filename, String separator) {
        super(filename);
        this.separator = separator;
    }

    @Override
    public void save(Dataset dataset) throws BNLibIOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(this.filename);
            boolean firstRecordOfLine;
            Variable[] vars = dataset.getVariables();
            // header
            firstRecordOfLine = true;
            for(Variable var : vars) {
                if(!firstRecordOfLine)
                    writer.write(this.separator);
                firstRecordOfLine = false;
                writer.write(var.getName());
                
                boolean firstValueOfVariable = true;
                writer.write('(');
                for(String value : var.getValues()) {
                    if(!firstValueOfVariable)
                        writer.write('|');
                    firstValueOfVariable = false;
                    writer.write(value);
                }
                writer.write(')');
                
            }
            writer.write(System.getProperty("line.separator"));
            // data rows
            for(int[] rowData : dataset.getDataReadOnly()) {
                firstRecordOfLine = true;
                for(int i = 0 ; i < vars.length ; i++) {
                    if(!firstRecordOfLine)
                        writer.write(this.separator);
                    firstRecordOfLine = false;
                    writer.write(vars[i].getValues()[rowData[i]]);
                }
                writer.write(System.getProperty("line.separator"));
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
}
