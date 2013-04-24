// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib.learning;

import bna.bnlib.*;
import bna.bnlib.io.DatasetCSVFileReader;
import bna.bnlib.io.DatasetCSVFileWriter;
import bna.bnlib.io.DatasetFileReader;
import bna.bnlib.io.DatasetFileWriter;
import bna.bnlib.misc.Toolkit;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Holds samples for a set of variables.
 * Provides counting of occurences for a subset of dataset variables and also
 * computes mutual information.
 */
public class Dataset implements DatasetInterface {
    private Variable[] variables;
    List<int[]> records; // package-private for the DatasetIterator
    
    
    /**
     * Create a dataset containing
     * @param variables
     * @param records 
     */
    public Dataset(Variable[] variables) {
        this.variables = Arrays.copyOf(variables, variables.length);
        this.records = new LinkedList<int[]>();
    }
    
    /**
     * Create new dataset by reading data from given CSV file.
     * @throws BNLibIOException When an IO error occurs or the datafile content
     *                          is invalid.
     */
    public static Dataset loadCSVFile(String csvFileName, String separator) throws BNLibIOException {
        DatasetFileReader reader = new DatasetCSVFileReader(csvFileName, separator);
        return reader.load();
    }
    
    /**
     * Saves this dataset to the given CSV file.
     * @throws BNLibIOException When an IO error occurs.
     */
    public void saveCSVFile(String csvFileName, String separator) throws BNLibIOException {
        DatasetFileWriter csvWriter = new DatasetCSVFileWriter(csvFileName, separator);
        csvWriter.save(this);
    }
    
    @Override
    public Variable[] getVariables() {
        return Arrays.copyOf(this.variables, this.variables.length);
    }
    
    /** Check whether the dataset contains each of the specified variables. */
    public boolean containsVariables(Variable[] vars) {
        return Toolkit.isSubset(this.variables, vars);
    }
    
    @Override
    public int getSize() {
        return this.records.size();
    }
    
    /** Return all records in a read only list. */
    public List<int[]> getDataReadOnly() {
        return Collections.unmodifiableList(this.records);
    }
    
    /**
     * Add new record to the dataset.
     * @throws BNLibIllegalArgumentException When the given record cannot be an
     *         assignment of the variables in this dataset.
     */
    @Override
    public void addRecord(int[] record) throws BNLibIllegalArgumentException {
        if(!Toolkit.validateAssignment(this.variables, record))
            throw new BNLibIllegalArgumentException("Record of invalid lenght or with invalid values.");
        this.records.add(Arrays.copyOf(record, record.length));
    }
    
    /**
     * Count occurences of all assignments to given variables and return as a factor.
     * @throws BNLibInconsistentVariableSetsException When this dataset doesn't
     *         contain all variables from the scope parameter.
     */
    @Override
    public Factor computeFactor(Variable[] scope) throws BNLibInconsistentVariableSetsException {
        if(!this.containsVariables(scope))
            throw new BNLibInconsistentVariableSetsException("Dataset doesn't contain all requested variables.");
        Counter counter = new Counter(scope);
        VariableSubsetMapper recordToScopeMapper = new VariableSubsetMapper(this.variables, scope);
        int[] scopeAssignment = new int[scope.length];
        for(int[] record : this.records) {
            recordToScopeMapper.map(record, scopeAssignment);
            counter.add(scopeAssignment, 1);
        }
        return counter.toFactor();
    }
    
    /**
     * Compute mutual information between two sets of variables.
     * @throws BNLibIllegalArgumentException When the two sets aren't disjoint
     *         or containt a variable not present in the dataset.
     */
    @Override
    public double mutualInformation(Variable[] set1, Variable[] set2) throws BNLibIllegalArgumentException {
        if(!Toolkit.areDisjoint(set1, set2))
            throw new BNLibIllegalArgumentException("Sets to compute mutual information for are not disjoint.");
        Variable[] union = Toolkit.union(set1, set2);
        if(!Toolkit.isSubset(this.variables, union))
            throw new BNLibIllegalArgumentException("Sets contain variables not present in the dataset.");
        
        if(set1.length == 0 || set2.length == 0)
            return 0.0;
        
        double inf = 0.0;
        Factor unionFactor = this.computeFactor(union).normalize(),
               set1Factor = unionFactor.marginalize(set2).normalize(),
               set2Factor = unionFactor.marginalize(set1).normalize();
        VariableSubsetMapper unionToSet1Mapper = new VariableSubsetMapper(union, set1),
                             unionToSet2Mapper = new VariableSubsetMapper(union, set2);
        int[] set1Assignment = new int[set1.length],
              set2Assignment = new int[set2.length];
        for(int[] unionAssignment : unionFactor) {
            unionToSet1Mapper.map(unionAssignment, set1Assignment);
            unionToSet2Mapper.map(unionAssignment, set2Assignment);
            double pxy = unionFactor.getProbability(unionAssignment),
                   px = set1Factor.getProbability(set1Assignment),
                   py = set2Factor.getProbability(set2Assignment);
            if(pxy != 0) // to avoid problems with floating-point arithmetics
                // if pxy > 0, then surely px > 0 && py > 0
                inf += pxy * Math.log(pxy / (px * py));
        }
        
        return inf;
    }
}
