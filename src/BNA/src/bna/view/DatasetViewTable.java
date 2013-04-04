// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/30

package bna.view;

import bna.bnlib.Variable;
import bna.bnlib.learning.Dataset;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


/**
 * Table to view a dataset.
 */
public class DatasetViewTable extends javax.swing.JTable {
    public static final TableModel EMPTY_TABLE_MODEL = new DefaultTableModel();
    
    private Dataset dataset = null;
    private int displayedRowCount = 1000;
    
    
    public DatasetViewTable() {
        this.dataset = null;
        this.setModel(DatasetViewTable.EMPTY_TABLE_MODEL);
    }
    
    public Dataset getDataset() {
        return this.dataset;
    }
    
    public boolean hasDataset() {
        return this.dataset != null;
    }
    
    public void setDataset(Dataset dataset) {
        if(this.dataset == dataset)
            return;
        this.dataset = dataset;
        this.updateTableModel();
        MainWindow.getInstance().notifyActiveDatasetChange();
    }
    
    private void updateTableModel() {
        if(this.dataset == null) {
            this.setModel(DatasetViewTable.EMPTY_TABLE_MODEL);
            return;
        }
        Variable[] variables = this.dataset.getVariables();
        // table headers
        String[] variableNames = new String[variables.length];
        for(int i = 0 ; i < variables.length ; i++)
            variableNames[i] = variables[i].getName();
        // data rows
        int dataRowCount = Math.min(this.displayedRowCount, this.dataset.getSize());
        Object[][] data = new Object[dataRowCount][variables.length];
        int i = 0;
        for(int[] iSample : this.dataset.getDataReadOnly()) {
            if(!(i < dataRowCount))
                break;
            for(int j = 0 ; j < variables.length ; j++)
                data[i][j] = variables[j].getValues()[iSample[j]];
            i++;
        }
        
        this.setModel(new DefaultTableModel(data, variableNames));
    }
}
