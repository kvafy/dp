// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/16

package bna.view;

import bna.bnlib.Factor;
import bna.bnlib.Variable;
import java.util.Arrays;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


/**
 * Views a factor in form of a JTable.
 * By default the table is empty.
 */
public class FactorViewTable extends javax.swing.JTable {
    public static final TableModel EMPTY_TABLE_MODEL = new DefaultTableModel();
    
    private Factor factor;
    
    
    public FactorViewTable() {
        this.factor = null;
        this.setNewTableModel();
    }
    
    /** From now on view the given factor. If the newFactor is null, the table is empty. */
    public void setFactor(Factor newFactor) {
        Factor currentFactor = this.factor;
        if(newFactor == currentFactor) // factor are immutable => nothing to be done
            return;
        
        this.factor = newFactor;
        if(currentFactor != null && newFactor != null && Arrays.equals(currentFactor.getScope(), newFactor.getScope()))
            this.updateProbabilityColumn();
        else
            this.setNewTableModel();
    }
    
    private void setNewTableModel() {
        TableModel model;
        if(this.factor == null)
            model = EMPTY_TABLE_MODEL;
        else
            model = this.generateTableModel(this.factor);
        this.setModel(model);
    }
    
    private TableModel generateTableModel(Factor f) {
        Variable[] scope = f.getScope();
        // generate header
        String[] header = new String[scope.length + 1];
        for(int i = 0 ; i < scope.length ; i++)
            header[i] = scope[i].getName();
        header[scope.length] = "probability";
        // generate data matrix
        Object[][] matrix = new Object[f.getCardinality()][scope.length + 1];
        int row = 0;
        for(int[] assignment : f) {
            for(int i = 0 ; i < assignment.length ; i++) {
                matrix[row][i] = scope[i].getValues()[assignment[i]];
            }
            matrix[row][scope.length] = this.probabilityToString(f.getProbability(assignment));
            row++;
        }
        return new DefaultTableModel(matrix, header);
    }
    
    private void updateProbabilityColumn() {
        int probabilityColumn = this.factor.getScope().length;
        int row = 0;
        for(int[] assignment : this.factor) {
            double prob = this.factor.getProbability(assignment);
            String probStr = this.probabilityToString(prob);
            this.setValueAt(probStr, row, probabilityColumn);
            row++;
        }
    }
    
    private String probabilityToString(double prob) {
        return String.format("%.6f", prob);
    }
}
