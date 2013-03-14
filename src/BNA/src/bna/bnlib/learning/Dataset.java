// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/28

package bna.bnlib.learning;

import bna.bnlib.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;


/**
 *
 */
public class Dataset implements DatasetInterface {
    private Variable[] variables;
    List<int[]> records; // package-private for the DatasetIterator
    
    
    /**
     * Create new dataset by reading data from given CSV file.
     * @throws BayesianNetworkException When an IO error occurs or the datafile
     *                                  has invalid format.
     */
    public Dataset(String csvFileName) throws BayesianNetworkException {
        try {
            this.loadFile(csvFileName);
        }
        catch(IOException ioex) {
            String msg = String.format("Dataset loading error: %s.", ioex.getMessage());
            throw new BayesianNetworkException(msg);
        }
    }
    
    /**
     * Create a dataset containing
     * @param variables
     * @param records 
     */
    public Dataset(Variable[] variables) {
        this.variables = Arrays.copyOf(variables, variables.length);
        this.records = new LinkedList<int[]>();
    }
    
    @Override
    public Variable[] getVariables() {
        return Arrays.copyOf(this.variables, this.variables.length);
    }
    
    @Override
    public int getSize() {
        return this.records.size();
    }
    
    private void loadFile(String csvFileName) throws IOException {
        LinkedList<int[]> data = new LinkedList<int[]>();
        // TODO read variables (name and list of possible assignments)
        // TODO read records
        
        // make the data read-only
        //this.records = Collections.unmodifiableList(data);
        this.records = data;
        throw new UnsupportedOperationException();
    }
    
    /** Add new record to the dataset. */
    @Override
    public void addRecord(int[] record) {
        if(!Toolkit.validateAssignment(this.variables, record))
            throw new BayesianNetworkRuntimeException("Record of invalid lenght.");
        this.records.add(Arrays.copyOf(record, record.length));
    }
    
    /** Count occurences of all assignments to given variables and return as a factor. */
    @Override
    public Factor computeFactor(Variable[] scope) {
        Counter counter = new Counter(scope);
        VariableSubsetMapper recordToScopeMapper = new VariableSubsetMapper(this.variables, scope);
        int[] scopeAssignment = new int[scope.length];
        for(int[] record : this.records) {
            recordToScopeMapper.map(record, scopeAssignment);
            counter.add(scopeAssignment, 1);
        }
        return counter.toFactor();
    }
    
    /** Compute mutual information between two sets of variables. */
    @Override
    public double mutualInformation(Variable[] set1, Variable[] set2) {
        if(!Toolkit.areDisjoint(set1, set2))
            throw new BayesianNetworkRuntimeException("Sets to compute mutual information for are not disjoint.");
        Variable[] union = Toolkit.union(set1, set2);
        if(!Toolkit.isSubset(this.variables, union))
            throw new BayesianNetworkRuntimeException("Sets contain variables not present in the dataset.");
        
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
