// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/04

package bna.view;

import bna.bnlib.BNLibException;
import bna.bnlib.BayesianNetwork;
import bna.bnlib.Variable;
import bna.bnlib.learning.*;
import bna.bnlib.misc.Toolkit;
import java.util.Arrays;
import java.util.Enumeration;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


/**
 * Dialog that takes a dataset and learns structure of a BN that fits the dataset the best.
 */
public class DialogStructureLearning extends javax.swing.JDialog {
    private BayesianNetwork bnOriginal; // null if there was no original network
    private Dataset dataset;
    LearningController learningController = null;
    private BayesianNetwork[] listBestScoringStructuresContent = null;
    

    /**
     * Creates new form DialogSampling
     */
    public DialogStructureLearning(java.awt.Frame parent, boolean modal, Dataset dataset, BayesianNetwork bnOriginal) {
        super(parent, modal);
        this.dataset = dataset; // tables need to access the dataset variables
        this.bnOriginal = bnOriginal;
        initComponents();
        this.setLocationRelativeTo(parent);
        this.initializeEdgeFrequenciesTable();
        this.initializeNetworksSelectionCombobox();
    }
    
    /** Called only from constructor to install the table headers. */
    private void initializeEdgeFrequenciesTable() {
        Variable[] variables = this.dataset.getVariables();
        int variableCount = variables.length;
        
        String[] columnHeaders = new String[1 + variableCount];
        columnHeaders[0] = "";
        for(int i = 0 ; i < variableCount ; i++)
            columnHeaders[i + 1] = variables[i].getName();
        Object[][] rows = new Object [variableCount][1 + variableCount];
        for(int row = 0 ; row < variableCount ; row++) {
            rows[row][0] = variables[row].getName();
            Arrays.fill(rows[row], 1, variableCount + 1, "");
        }
        this.tableEdgeFrequency.setModel(new DefaultTableModel(rows, columnHeaders));
        
        // vertical column labels (thanks to darrylbu)
        TableCellRenderer headerRenderer = new bna.view.darrylbu.VerticalTableHeaderCellRenderer();
        Enumeration<TableColumn> frequencyColumns = this.tableEdgeFrequency.getColumnModel().getColumns();
        boolean firstFrequencyColumn = true;
        while(frequencyColumns.hasMoreElements()) {
            TableColumn column = frequencyColumns.nextElement();
            if(!firstFrequencyColumn) {
                column.setPreferredWidth(50);
                column.sizeWidthToFit();
            }
            column.setHeaderRenderer(headerRenderer);
            firstFrequencyColumn = false;
        }
    }
    
    private void initializeNetworksSelectionCombobox() {
        boolean hasOriginalNetwork = this.bnOriginal != null;
        // show list of the networks with top score
        final String[] bnStrings;
        if(hasOriginalNetwork) {
            bnStrings = new String[] {"Original network"};
            this.listBestScoringStructuresContent = new BayesianNetwork[] {this.bnOriginal};
        }
        else {
            bnStrings = new String[0];
            this.listBestScoringStructuresContent = new BayesianNetwork[0];
        }
        this.comboBoxNetworksSelection.setModel(new javax.swing.DefaultComboBoxModel(bnStrings));
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
            int maxParents = Integer.valueOf(this.textFieldMaxParents.getText());
            String errorMsg = null;
            if(runCount <= 0)
                errorMsg = "Run count has to be a positive integer.";
            else if(iterationCount <= 0)
                errorMsg = "Iteration count has to be a integer.";
            else if(randomRestartStepcount < 0)
                errorMsg = "Random restart step count has to be a non-negative integer.";
            else if(!(tabulistRelsize >= 0 && tabulistRelsize <= 1.0))
                errorMsg = "Tabulist relative size has to be a real number from the interval [0,1].";
            else if(maxParents < 0) // TODO will work for 0? (ie. will not crash?)
                errorMsg = "Maximum number of parents has to be a non-negative integer.";
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
    
    private void notifyLearningStarted() {
        this.comboBoxMethod.setEnabled(false);
        this.textFieldRunCount.setEnabled(false);
        this.textFieldIterationCount.setEnabled(false);
        this.textFieldRandomRestartStepcount.setEnabled(false);
        this.textFieldTabulistRelsize.setEnabled(false);
        this.buttonLearn.setEnabled(false);
        this.buttonStop.setEnabled(true);
        this.updateFrequencyMatrix(null);
        this.setBestScoringNetworks(null, null);
    }
    
    private void notifyLearningProgess(int currentRun, StructureLearningStatistics statistics) {
        this.labelStatus.setText(String.format("computing run no %d...", currentRun + 1));
        this.updateFrequencyMatrix(statistics.getEdgeCountMatrix());
        this.setBestScoringNetworks(statistics.getBestScoringNetworks(), statistics.getBestScoreSoFar());
    }
    
    private void notifyLearningFinished(boolean success) {
        this.comboBoxMethod.setEnabled(true);
        this.textFieldRunCount.setEnabled(true);
        this.textFieldIterationCount.setEnabled(true);
        this.textFieldRandomRestartStepcount.setEnabled(true);
        this.textFieldTabulistRelsize.setEnabled(true);
        this.buttonLearn.setEnabled(true);
        this.buttonStop.setEnabled(false);
        if(success)
            this.labelStatus.setText("finished succesfully");
        else
            this.labelStatus.setText("interrupted");
    }
    
    private void setBestScoringNetworks(BayesianNetwork[] bns, Double score) {
        // show the score
        String scoreText;
        if(score != null)
            scoreText = String.format("%.3f", score);
        else
            scoreText = "<undefined>";
        this.labelBestScoreSoFar.setText(scoreText);
        
        // show list of the networks with top score
        if(bns == null)
            bns = new BayesianNetwork[0];
        String[] bnStrings = new String[bns.length];
        for(int i = 0 ; i < bnStrings.length ; i++)
            bnStrings[i] = String.format("Network no %d", i + 1);
        this.listBestScoringStructuresContent = Arrays.copyOf(bns, bns.length);
        if(this.bnOriginal != null) {
            bnStrings = Toolkit.union(new String[]{"Original network"}, bnStrings);
            this.listBestScoringStructuresContent = Toolkit.union(new BayesianNetwork[]{this.bnOriginal}, this.listBestScoringStructuresContent);
        }
        
        this.comboBoxNetworksSelection.setModel(new javax.swing.DefaultComboBoxModel(bnStrings));
    }
    
    /** Update content of the table tableEdgeFrequency. */
    private void updateFrequencyMatrix(int[][] edgeCounts) {
        Variable[] variables = this.dataset.getVariables();
        int variableCount = variables.length;
        // number in each cell will be normalized by the occurence count of the most frequence edge
        int maxEdgeCount = 0;
        if(edgeCounts != null) {
            for(int i = 0 ; i < variableCount ; i++)
                for(int j = 0 ; j < variableCount ; j++)
                    maxEdgeCount = Math.max(maxEdgeCount, edgeCounts[i][j]);
        }
        double edgeCountNormalizer = (maxEdgeCount > 0) ? maxEdgeCount : 1.0; // to avoid division by zero
        
        for(int row = 0 ; row < variableCount ; row++) {
            for(int column = 0 ; column < variableCount ; column++) {
                String cellContent;
                if(edgeCounts == null)
                    cellContent = "";
                else
                    cellContent = String.format("%.2f", edgeCounts[row][column] / edgeCountNormalizer);
                this.tableEdgeFrequency.setValueAt(cellContent, row, column + 1);
            }
        }
    }
    
    /**
     * Set node-to-node structural constraints according to content
     * of the tableAllowedConnections table.
     */
    private void fillStructuralConstraints(StructuralConstraints constraints) {
        Variable[] variables = this.dataset.getVariables();
        int variableCount = variables.length;
        for(int i = 0 ; i < variableCount ; i++) {
            Variable iVar = variables[i];
            for(int j = 0 ; j < variableCount ; j++) {
                Variable jVar = variables[j];
                boolean ijAllowed = ((AllowedConnectionsTable)this.tableAllowedConnections).isConnectionAllowed(i, j);
                constraints.setConnectionAllowed(iVar, jVar, ijAllowed);
            }
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
        jTabbedPane1 = new javax.swing.JTabbedPane();
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
        buttonStop = new javax.swing.JButton();
        buttonLearn = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableAllowedConnections = new AllowedConnectionsTable();
        textFieldMaxParents = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        labelBestScoreSoFar = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableEdgeFrequency = new javax.swing.JTable();
        comboBoxNetworksSelection = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        labelStatus = new javax.swing.JLabel();

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
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel2.setBorder(null);

        jLabel3.setText("Scoring method");

        jLabel2.setText("Number of runs");

        textFieldRunCount.setText("100");

        comboBoxMethod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BIC score", "Bayesian score" }));

        jLabel4.setText("Number of iterations");

        textFieldIterationCount.setText("100");

        jLabel1.setText("per run");

        jLabel5.setText("Random restart steps");

        textFieldRandomRestartStepcount.setText("5");

        textFieldTabulistRelsize.setText("0.2");

        jLabel6.setText("Relative tabulist size");

        buttonStop.setText("Stop");
        buttonStop.setEnabled(false);
        buttonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStopActionPerformed(evt);
            }
        });

        buttonLearn.setText("Learn");
        buttonLearn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLearnActionPerformed(evt);
            }
        });

        jLabel10.setText("Maximum number of parents");

        jLabel11.setText("Allowed connections matrix");

        tableAllowedConnections.setModel(new javax.swing.table.DefaultTableModel());
        tableAllowedConnections.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane3.setViewportView(tableAllowedConnections);
        ((AllowedConnectionsTable)tableAllowedConnections).initModel();

        textFieldMaxParents.setText("3");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(28, 28, 28)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(comboBoxMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textFieldRunCount, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel6))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(textFieldTabulistRelsize, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textFieldRandomRestartStepcount, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(textFieldIterationCount, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel1))))
                            .addComponent(jLabel2)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldMaxParents, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel11)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(buttonLearn, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(buttonStop, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 15, Short.MAX_VALUE))
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonLearn, buttonStop});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(comboBoxMethod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(textFieldIterationCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(textFieldRunCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(textFieldRandomRestartStepcount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(textFieldTabulistRelsize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(textFieldMaxParents, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonLearn)
                    .addComponent(buttonStop))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Learning", jPanel2);

        jPanel3.setBorder(null);

        jLabel7.setText("Best score so far:");

        labelBestScoreSoFar.setText("<undefined>");

        jLabel8.setText("View network");

        jLabel9.setText("Edge frequency matrix (relative to the number of occurences of the most frequent edge)");

        tableEdgeFrequency.setModel(new DefaultTableModel());
        tableEdgeFrequency.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tableEdgeFrequency.setEnabled(false);
        jScrollPane2.setViewportView(tableEdgeFrequency);

        comboBoxNetworksSelection.setModel(new javax.swing.DefaultComboBoxModel());
        comboBoxNetworksSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxNetworksSelectionActionPerformed(evt);
            }
        });

        jLabel12.setText("Status:");

        labelStatus.setText("not started yet");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelBestScoreSoFar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(0, 24, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 12, 12))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxNetworksSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(labelStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(labelBestScoreSoFar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(comboBoxNetworksSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Results", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonLearnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLearnActionPerformed
        if(!this.verifyInputs())
            return;
        StructureLearningThread thread = new StructureLearningThread((java.awt.Frame)this.getParent());
        thread.start();
    }//GEN-LAST:event_buttonLearnActionPerformed

    private void buttonStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStopActionPerformed
        if(this.learningController != null)
            this.learningController.setStopFlag();
    }//GEN-LAST:event_buttonStopActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if(this.learningController != null)
            this.learningController.setStopFlag();
    }//GEN-LAST:event_formWindowClosing

    private void comboBoxNetworksSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxNetworksSelectionActionPerformed
        int bnIndex = this.comboBoxNetworksSelection.getSelectedIndex();
        BayesianNetwork activeNetwork;
        if(bnIndex == -1)
            activeNetwork = null;
        else
            activeNetwork = this.listBestScoringStructuresContent[bnIndex];
        MainWindow.getInstance().setActiveNetwork(activeNetwork);
    }//GEN-LAST:event_comboBoxNetworksSelectionActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonLearn;
    private javax.swing.JButton buttonStop;
    private javax.swing.JComboBox comboBoxMethod;
    private javax.swing.JComboBox comboBoxNetworksSelection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
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
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelBestScoreSoFar;
    private javax.swing.JLabel labelStatus;
    private javax.swing.JTable tableAllowedConnections;
    private javax.swing.JTable tableEdgeFrequency;
    private javax.swing.JTextField textFieldIterationCount;
    private javax.swing.JTextField textFieldMaxParents;
    private javax.swing.JTextField textFieldRandomRestartStepcount;
    private javax.swing.JTextField textFieldRunCount;
    private javax.swing.JTextField textFieldTabulistRelsize;
    // End of variables declaration//GEN-END:variables


    class StructureLearningThread extends Thread {
        private java.awt.Frame parent;
        
        public StructureLearningThread(java.awt.Frame parent) {
            this.parent = parent;
        }
        
        @Override
        public void run() {
            try {
                notifyLearningStarted();
                
                final Variable[] VARIABLES = dataset.getVariables();
                final int VARIABLE_COUNT = VARIABLES.length;
                // learning parameters
                int selectedScoringMethodIndex = comboBoxMethod.getSelectedIndex();
                int runCount = Integer.valueOf(textFieldRunCount.getText());
                long iterationCount = Long.valueOf(textFieldIterationCount.getText());
                int randomRestartStepcount = Integer.valueOf(textFieldRandomRestartStepcount.getText());
                double tabulistRelsize = Double.valueOf(textFieldTabulistRelsize.getText());
                int maxParents = Integer.valueOf(textFieldMaxParents.getText());


                int maxAlterations = 2 * (VARIABLE_COUNT * (VARIABLE_COUNT - 1) / 2);
                int tabulistAbssize = (int)(maxAlterations * tabulistRelsize);
                // put the dataset cache in place (can be shared among all runs due to LRU policy)
                int LRU_CACHE_SIZE = VARIABLE_COUNT + 3 * (VARIABLE_COUNT * (VARIABLE_COUNT - 1) / 2);
                LRU_CACHE_SIZE = (int)(LRU_CACHE_SIZE * 2.0); // reserve
                CachedDataset cachedDataset = new CachedDataset(dataset, LRU_CACHE_SIZE);
                
                
                StructuralConstraints constraints = new StructuralConstraints(VARIABLES);
                constraints.setMaxParentCount(maxParents);
                fillStructuralConstraints(constraints);
                learningController = new LearningController(iterationCount);
                BayesianNetwork bnEmpty = new BayesianNetwork(VARIABLES);
                StructureLearningStatistics statistics = new StructureLearningStatistics(VARIABLES);

                for(int run = 0 ; run < runCount && !learningController.getStopFlag(); run++) {
                    notifyLearningProgess(run, statistics);
                    BayesianNetwork bnInitial = bnEmpty.copyEmptyStructure();
                    // !! BICScoringMethod has to be created for each run separately due to caching policy
                    ScoringMethod bicScoringMethod = new BICScoringMethod(cachedDataset);
                    StructureLearningAlgorithm learningAlgorithm = new TabuSearchLearningAlgorithm(bicScoringMethod, tabulistAbssize, randomRestartStepcount);
                    BayesianNetwork resultBN = learningAlgorithm.learn(bnInitial, learningController, constraints);
                    if(learningController.getStopFlag() == true)
                        break;
                    double resultBIC =  bicScoringMethod.absoluteScore(resultBN);
                    statistics.registerLearntNetwork(resultBN, resultBIC);
                }
                notifyLearningProgess(runCount, statistics);
                notifyLearningFinished(!learningController.getStopFlag());
                //statistics.report();
                /*
                System.out.println("\nMost probable network:");
                ScoringMethod bicScoringMethod = new BICScoringMethod(cachedDataset);
                System.out.println(statistics.getMostProbableNetwork(bicScoringMethod).dumpStructure());*/
            }
            catch(BNLibException ex) {
                notifyLearningFinished(false);
                JOptionPane.showMessageDialog(this.parent, ex.getMessage(), "An error occured", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    class AllowedConnectionsTable extends JTable {
        
        /** Establish the table model. */
        private void initModel() {
            Variable[] variables = dataset.getVariables();
            int variableCount = variables.length;

            String[] columnHeaders = new String[1 + variableCount];
            columnHeaders[0] = "";
            for(int i = 0 ; i < variableCount ; i++)
                columnHeaders[i + 1] = variables[i].getName();
            Object[][] rows = new Object [variableCount][1 + variableCount];
            for(int row = 0 ; row < variableCount ; row++) {
                rows[row][0] = variables[row].getName();
                for(int column = 0 ; column < variableCount ; column++)
                    rows[row][column + 1] = new Boolean(row != column);
            }
            this.setModel(new DefaultTableModel(rows, columnHeaders) {
                @Override
                public Class getColumnClass(int column) {
                    if(column == 0)
                        return String.class;
                    else
                        return Boolean.class; // make some of the cells have checkboxes
                }
            });

            // vertical column labels (thanks to darrylbu)
            TableCellRenderer headerRenderer = new bna.view.darrylbu.VerticalTableHeaderCellRenderer();
            Enumeration<TableColumn> constraintColumns = this.getColumnModel().getColumns();
            boolean firstConstraintColumn = true;
            while(constraintColumns.hasMoreElements()) {
                TableColumn column = constraintColumns.nextElement();
                if(!firstConstraintColumn) {
                    column.setPreferredWidth(30);
                    column.sizeWidthToFit();
                }
                column.setHeaderRenderer(headerRenderer);
                firstConstraintColumn = false;
            }
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int colIndex) {
            if(colIndex == 0) // names of the variables
                return false;
            else if(colIndex == rowIndex + 1) // autoconnection on the variable itself
                return false;
            else
                return true;
        }
        
        public boolean isConnectionAllowed(int from, int to) {
            return (Boolean)this.getModel().getValueAt(from, to + 1);
        }
    }
}
