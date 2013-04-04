// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/04

package bna.view;

import bna.bnlib.BayesianNetworkException;
import bna.bnlib.Variable;
import bna.bnlib.learning.Dataset;
import java.util.Enumeration;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


/**
 * Dialog that takes a dataset and learns structure of a BN that fits the dataset the best.
 */
public class DialogStructureLearning extends javax.swing.JDialog {
    private Dataset dataset;
    

    /**
     * Creates new form DialogSampling
     */
    public DialogStructureLearning(java.awt.Frame parent, boolean modal, Dataset dataset) {
        super(parent, modal);
        initComponents();
        this.setLocationRelativeTo(parent);
        this.dataset = dataset;
        try {
            Variable[] vars = new Variable[] {
                new Variable("wet", new String[] {"false", "true"}),
                new Variable("cloudy", new String[] {"false", "true"}),
                new Variable("rain", new String[] {"false", "true"})
            };
            int[][] edgeOccurences = new int[][]{
                {0, 2, 3},
                {0, 0, 2},
                {15, 0, 0}
            };
            this.setFrequencyMatrix(vars, edgeOccurences);
        }
        catch(BayesianNetworkException ex) {
            ex.printStackTrace();
        }
    }

    private boolean verifyInputs() {
        int selectedScoringMethod = this.comboBoxMethod.getSelectedIndex();
        if(selectedScoringMethod != 0 && selectedScoringMethod != 1) {
            String msg = "No scoring method is selected.";
            JOptionPane.showMessageDialog(this, msg, "Incomplete parameters", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int runCount = Integer.valueOf(this.textFieldRunCount.getText());
            long iterationCount = Long.valueOf(this.textFieldIterationCount.getText());
            int randomRestartStepcount = Integer.valueOf(this.textFieldRandomRestartStepcount.getText());
            double tabulistRelsize = Double.valueOf(this.textFieldTabulistRelsize.getText());
            String errorMsg = null;
            if(runCount <= 0)
                errorMsg = "Run count has to be a positive integer.";
            else if(iterationCount <= 0)
                errorMsg = "Iteration count has to be a integer.";
            else if(randomRestartStepcount < 0)
                errorMsg = "Random restart step count has to be a non-negative integer.";
            else if(tabulistRelsize < 0)
                errorMsg = "Tabulist relative size has to be a non-negative real number.";
            if(errorMsg != null) {
                JOptionPane.showMessageDialog(this, errorMsg, "Invalid parameters", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch(NumberFormatException ex) {
            String msg = "Numeric parameters are invalid.";
            JOptionPane.showMessageDialog(this, msg, "Invalid parameters", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void setFrequencyMatrix(Variable[] variables, int[][] edgeCounts) {
        int variableCount = variables.length;
        int maxEdgeCount = 0;
        for(int i = 0 ; i < variableCount ; i++)
            for(int j = 0 ; j < variableCount ; j++)
                maxEdgeCount = Math.max(maxEdgeCount, edgeCounts[i][j]);
        double edgeCountNormalizer = (maxEdgeCount > 0) ? maxEdgeCount : 1.0; // to avoid division by zero
        
        String[] columnHeaders = new String[1 + variableCount];
        columnHeaders[0] = "";
        for(int i = 0 ; i < variableCount ; i++)
            columnHeaders[i + 1] = variables[i].getName();
        Object[][] rows = new Object [variableCount][1 + variableCount];
        for(int row = 0 ; row < variableCount ; row++) {
            rows[row][0] = variables[row].getName();
            for(int column = 0 ; column < variableCount ; column++) {
                rows[row][column + 1] = String.format("%.2f", edgeCounts[row][column] / edgeCountNormalizer);
            }
        }
        this.tableEdgeFrequency.setModel(new DefaultTableModel(rows, columnHeaders));
        
        // vertical column labels (thanks to darrylbu)
        TableCellRenderer headerRenderer = new bna.view.darrylbu.VerticalTableHeaderCellRenderer();
        Enumeration<TableColumn> columns = tableEdgeFrequency.getColumnModel().getColumns();
        boolean firstColumn = true;
        while(columns.hasMoreElements()) {
            TableColumn column = columns.nextElement();
            if(!firstColumn) {
                column.setPreferredWidth(50);
                column.sizeWidthToFit();
            }
            column.setHeaderRenderer(headerRenderer);
            firstColumn = false;
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        buttonLearn = new javax.swing.JButton();
        buttonStop = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        textFieldRunCount = new javax.swing.JTextField();
        comboBoxMethod = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        textFieldIterationCount = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        textFieldRandomRestartStepcount = new javax.swing.JTextField();
        textFieldTabulistRelsize = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        labelBestScoreSoFar = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listBestScoringStructures = new javax.swing.JList();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableEdgeFrequency = new javax.swing.JTable();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Answer a probabilistic query");

        buttonLearn.setText("Learn");
        buttonLearn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLearnActionPerformed(evt);
            }
        });

        buttonStop.setText("Stop");
        buttonStop.setEnabled(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Learning parameters"));

        jLabel3.setText("Scoring method");

        jLabel2.setText("Number of runs");

        comboBoxMethod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BIC score", "Bayesian score" }));

        jLabel4.setText("Number of iterations");

        jLabel1.setText("per run");

        jLabel5.setText("Random restart steps");

        jLabel6.setText("Relative tabulist size");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addGap(56, 56, 56)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textFieldRunCount, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(textFieldIterationCount, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(textFieldTabulistRelsize, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(textFieldRandomRestartStepcount, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(comboBoxMethod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(textFieldRunCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(textFieldIterationCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(textFieldRandomRestartStepcount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(textFieldTabulistRelsize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Learning results"));

        jLabel7.setText("Best score so far:");

        labelBestScoreSoFar.setText("<undefined>");

        jLabel8.setText("Best scoring networks");

        listBestScoringStructures.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(listBestScoringStructures);

        jLabel9.setText("Edge frequency matrix (relative to the number of occurences of the most frequent edge)");

        tableEdgeFrequency.setModel(new DefaultTableModel());
        tableEdgeFrequency.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tableEdgeFrequency.setEnabled(false);
        jScrollPane2.setViewportView(tableEdgeFrequency);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelBestScoreSoFar))
                            .addComponent(jLabel8)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(labelBestScoreSoFar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(buttonLearn, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonStop, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonLearn)
                    .addComponent(buttonStop))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonLearnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLearnActionPerformed
        if(!this.verifyInputs())
            return;
        
        int selectedScoringMethodIndex = this.comboBoxMethod.getSelectedIndex();
        int runCount = Integer.valueOf(this.textFieldRunCount.getText());
        long iterationCount = Long.valueOf(this.textFieldIterationCount.getText());
        int randomRestartStepcount = Integer.valueOf(this.textFieldRandomRestartStepcount.getText());
        double tabulistRelsize = Double.valueOf(this.textFieldTabulistRelsize.getText());
        
        // TOOD
    }//GEN-LAST:event_buttonLearnActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonLearn;
    private javax.swing.JButton buttonStop;
    private javax.swing.JComboBox comboBoxMethod;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelBestScoreSoFar;
    private javax.swing.JList listBestScoringStructures;
    private javax.swing.JTable tableEdgeFrequency;
    private javax.swing.JTextField textFieldIterationCount;
    private javax.swing.JTextField textFieldRandomRestartStepcount;
    private javax.swing.JTextField textFieldRunCount;
    private javax.swing.JTextField textFieldTabulistRelsize;
    // End of variables declaration//GEN-END:variables
}
