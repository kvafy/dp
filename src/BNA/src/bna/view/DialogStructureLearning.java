// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/04

package bna.view;

import bna.bnlib.BNLibException;
import bna.bnlib.BayesianNetwork;
import bna.bnlib.Node;
import bna.bnlib.Variable;
import bna.bnlib.learning.*;
import bna.bnlib.misc.Toolkit;
import java.awt.Component;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Enumeration;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


/**
 * Dialog that takes a dataset and learns structure of a BN that fits the dataset the best.
 */
public class DialogStructureLearning extends javax.swing.JDialog implements ActiveDatasetObserver {
    private BayesianNetwork bnOriginal; // null if there was no original network
    private Dataset dataset;
    LearningController learningController = null;
    private BayesianNetwork[] comboBoxNetworksSelectionContent = null;
    

    /**
     * Creates new form DialogSampling
     */
    public DialogStructureLearning(java.awt.Frame parent, boolean modal, BayesianNetwork bnOriginal, final DatasetViewTable datasetViewer) {
        super(parent, modal);
        this.dataset = null; // tables need to access the dataset variables
        this.bnOriginal = null;//bnOriginal;
        initComponents();
        this.setLocationRelativeTo(parent);
        this.loadConfiguration();
        this.prepareEdgeFrequenciesTable();
        this.initializeNetworksSelectionCombobox();
        
        // ensure proper observing (registration and deregistration)
        final DialogStructureLearning thisDialog = this;
        datasetViewer.addObserver(thisDialog);
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                datasetViewer.removeObserver(thisDialog);
            }
            @Override
            public void windowClosed(WindowEvent e) {
                datasetViewer.removeObserver(thisDialog);
            }
        });
    }
    
    private void prepareAllowedConnectionsTable() {
    }
    
    /** Installs the table headers. */
    private void prepareEdgeFrequenciesTable() {
        if(this.dataset == null) {
            this.tableEdgeFrequency.setModel(new DefaultTableModel());
            return;
        }
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
        
        // resize the first column to accomodate for variable names
        int firstColumnWidth = 0;
        for(int row = 0 ; row < this.tableEdgeFrequency.getRowCount() ; row++) {
            TableCellRenderer renderer = this.tableEdgeFrequency.getCellRenderer(row, 0);
            Component comp = this.tableEdgeFrequency.prepareRenderer(renderer, row, 0);
            firstColumnWidth = Math.max (comp.getPreferredSize().width, firstColumnWidth);
        }
        this.tableEdgeFrequency.getColumnModel().getColumn(0).setPreferredWidth(firstColumnWidth);
    }
    
    private void initializeNetworksSelectionCombobox() {
        boolean hasOriginalNetwork = this.bnOriginal != null;
        // show list of the networks with top score
        final String[] bnStrings;
        if(hasOriginalNetwork) {
            bnStrings = new String[] {"Original network"};
            this.comboBoxNetworksSelectionContent = new BayesianNetwork[] {this.bnOriginal};
        }
        else {
            bnStrings = new String[0];
            this.comboBoxNetworksSelectionContent = new BayesianNetwork[0];
        }
        this.comboBoxNetworksSelection.setModel(new javax.swing.DefaultComboBoxModel(bnStrings));
    }
    
    private void loadConfiguration() {
        MainWindow mw = MainWindow.getInstance();
        // text fields
        this.textFieldAlpha.setText(mw.getConfiguration("LearningStructure", "alpha"));
        this.textFieldRunCount.setText(mw.getConfiguration("LearningStructure", "run_count"));
        this.textFieldIterationCount.setText(mw.getConfiguration("LearningStructure", "iteration_count"));
        this.textFieldRandomRestartStepcount.setText(mw.getConfiguration("LearningStructure", "rnd_restart_step_count"));
        this.textFieldTabulistRelsize.setText(mw.getConfiguration("LearningStructure", "tabulist_relsize"));
        this.textFieldMaxParents.setText(mw.getConfiguration("LearningStructure", "max_parents"));
        // comboboxes
        String scoringMethodIndexStr = mw.getConfiguration("LearningStructure", "scoring_method_index");
        try {
            int scoringMethodIndex = Integer.parseInt(scoringMethodIndexStr);
            this.comboBoxMethod.setSelectedIndex(scoringMethodIndex);
        }
        catch(NumberFormatException nfe) {}
        catch(IllegalArgumentException ex) {} // invalid index
        
        mw.loadWindowBounds(this, "DialogStructureLearning");
    }
    
    private void saveConfiguration() {
        MainWindow mw = MainWindow.getInstance();
        // text fields
        mw.setConfiguration("LearningStructure", "alpha", this.textFieldAlpha.getText());
        mw.setConfiguration("LearningStructure", "run_count", this.textFieldRunCount.getText());
        mw.setConfiguration("LearningStructure", "iteration_count", this.textFieldIterationCount.getText());
        mw.setConfiguration("LearningStructure", "rnd_restart_step_count", this.textFieldRandomRestartStepcount.getText());
        mw.setConfiguration("LearningStructure", "tabulist_relsize", this.textFieldTabulistRelsize.getText());
        mw.setConfiguration("LearningStructure", "max_parents", this.textFieldMaxParents.getText());
        // comboboxes
        String scoringMethodIndexStr = String.valueOf(this.comboBoxMethod.getSelectedIndex());
        mw.setConfiguration("LearningStructure", "scoring_method_index", scoringMethodIndexStr);
    }

    private boolean verifyInputs() {
        int selectedScoringMethod = this.comboBoxMethod.getSelectedIndex();
        if(selectedScoringMethod != 0 && selectedScoringMethod != 1) {
            String msg = "No scoring method is selected.";
            JOptionPane.showMessageDialog(this, msg, "Incomplete parameters", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            double alpha = (selectedScoringMethod == 1) ? Double.valueOf(this.textFieldAlpha.getText()) : 1;
            int runCount = Integer.valueOf(this.textFieldRunCount.getText());
            long iterationCount = Long.valueOf(this.textFieldIterationCount.getText());
            int randomRestartStepcount = Integer.valueOf(this.textFieldRandomRestartStepcount.getText());
            double tabulistRelsize = Double.valueOf(this.textFieldTabulistRelsize.getText());
            int maxParents = Integer.valueOf(this.textFieldMaxParents.getText());
            String errorMsg = null;
            if(selectedScoringMethod == 1 && alpha <= 0)
                errorMsg = "Equivalent sample size (alpha) needs to be positive.";
            else if(runCount <= 0)
                errorMsg = "Run count has to be a positive integer.";
            else if(iterationCount <= 0)
                errorMsg = "Iteration count has to be a integer.";
            else if(randomRestartStepcount < 0)
                errorMsg = "Random restart step count has to be a non-negative integer.";
            else if(!(tabulistRelsize >= 0 && tabulistRelsize <= 1.0))
                errorMsg = "Tabulist relative size has to be a real number from the interval [0,1].";
            else if(maxParents <= 0)
                errorMsg = "Maximum number of parents has to be a positive integer.";
            if(errorMsg != null) {
                JOptionPane.showMessageDialog(this, errorMsg, "Invalid parameters", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch(NumberFormatException ex) {
            String msg = "Numeric parameters are invalid strings.";
            JOptionPane.showMessageDialog(this, msg, "Invalid parameters", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void notifyLearningStarted() {
        this.comboBoxMethod.setEnabled(false);
        this.textFieldAlpha.setEnabled(false);
        this.textFieldRunCount.setEnabled(false);
        this.textFieldIterationCount.setEnabled(false);
        this.textFieldRandomRestartStepcount.setEnabled(false);
        this.textFieldTabulistRelsize.setEnabled(false);
        this.textFieldMaxParents.setEnabled(false);
        this.tableAllowedConnections.setEnabled(false);
        this.buttonLearn.setEnabled(false);
        this.buttonStop.setEnabled(true);
        this.comboBoxNetworksSelection.setEnabled(false);
        this.updateFrequencyMatrix(null);
        this.setBestScoringNetworks(null, null);
    }
    
    private void notifyLearningProgess(int currentRun, StructureLearningStatistics statistics) {
        this.labelStatus.setText(String.format("computing run no %d...", currentRun + 1));
        this.updateFrequencyMatrix(statistics.getEdgeCountMatrix());
        this.setBestScoringNetworks(statistics.getBestScoringNetworks(), statistics.getBestScoreSoFar());
    }
    
    private void notifyLearningFinished(boolean success) {
        boolean bayesianScoringMethodSelected = this.comboBoxMethod.getSelectedIndex() == 1;
        this.comboBoxMethod.setEnabled(true);
        this.textFieldAlpha.setEnabled(bayesianScoringMethodSelected);
        this.textFieldRunCount.setEnabled(true);
        this.textFieldIterationCount.setEnabled(true);
        this.textFieldRandomRestartStepcount.setEnabled(true);
        this.textFieldTabulistRelsize.setEnabled(true);
        this.textFieldMaxParents.setEnabled(true);
        this.tableAllowedConnections.setEnabled(true);
        this.buttonLearn.setEnabled(true);
        this.buttonStop.setEnabled(false);
        this.comboBoxNetworksSelection.setEnabled(true);
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
        this.comboBoxNetworksSelectionContent = Arrays.copyOf(bns, bns.length);
        if(this.bnOriginal != null) {
            bnStrings = Toolkit.union(new String[]{"Original network"}, bnStrings);
            this.comboBoxNetworksSelectionContent = Toolkit.union(new BayesianNetwork[]{this.bnOriginal}, this.comboBoxNetworksSelectionContent);
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
        tabbedPane = new javax.swing.JTabbedPane();
        panelLearning = new javax.swing.JPanel();
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
        jLabel13 = new javax.swing.JLabel();
        textFieldAlpha = new javax.swing.JTextField();
        panelResults = new javax.swing.JPanel();
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
        setTitle("Structure learning");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });

        panelLearning.setBorder(null);

        jLabel3.setText("Scoring method");

        jLabel2.setText("Number of runs");

        comboBoxMethod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "BIC score", "Bayesian score" }));
        comboBoxMethod.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboBoxMethodItemStateChanged(evt);
            }
        });

        jLabel4.setText("Number of iterations");

        jLabel1.setText("per run");

        jLabel5.setText("Random restart steps");

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
        tableAllowedConnections.setColumnSelectionAllowed(true);
        tableAllowedConnections.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(tableAllowedConnections);

        jLabel13.setText("Alpha");

        textFieldAlpha.setEnabled(false);

        javax.swing.GroupLayout panelLearningLayout = new javax.swing.GroupLayout(panelLearning);
        panelLearning.setLayout(panelLearningLayout);
        panelLearningLayout.setHorizontalGroup(
            panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLearningLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLearningLayout.createSequentialGroup()
                        .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelLearningLayout.createSequentialGroup()
                                .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addGroup(panelLearningLayout.createSequentialGroup()
                                        .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel13))
                                        .addGap(28, 28, 28)
                                        .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(comboBoxMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(textFieldAlpha, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(textFieldRunCount, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)))))
                                .addGap(18, 18, 18)
                                .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel6))
                                .addGap(18, 18, 18)
                                .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(textFieldTabulistRelsize, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textFieldRandomRestartStepcount, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(panelLearningLayout.createSequentialGroup()
                                        .addComponent(textFieldIterationCount, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel1))))
                            .addGroup(panelLearningLayout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldMaxParents, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel11)
                            .addGroup(panelLearningLayout.createSequentialGroup()
                                .addComponent(buttonLearn, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(buttonStop, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 15, Short.MAX_VALUE))
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );

        panelLearningLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonLearn, buttonStop});

        panelLearningLayout.setVerticalGroup(
            panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLearningLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(comboBoxMethod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(textFieldIterationCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(textFieldRandomRestartStepcount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(textFieldAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(textFieldTabulistRelsize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(textFieldRunCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(textFieldMaxParents, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(panelLearningLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonLearn)
                    .addComponent(buttonStop))
                .addContainerGap())
        );

        tabbedPane.addTab("Learning", panelLearning);

        panelResults.setBorder(null);

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

        javax.swing.GroupLayout panelResultsLayout = new javax.swing.GroupLayout(panelResults);
        panelResults.setLayout(panelResultsLayout);
        panelResultsLayout.setHorizontalGroup(
            panelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelResultsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelResultsLayout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addContainerGap())
                    .addGroup(panelResultsLayout.createSequentialGroup()
                        .addGroup(panelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelResultsLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelBestScoreSoFar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(panelResultsLayout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(0, 24, Short.MAX_VALUE))
                            .addGroup(panelResultsLayout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 12, 12))
                    .addGroup(panelResultsLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxNetworksSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        panelResultsLayout.setVerticalGroup(
            panelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelResultsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(labelStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(labelBestScoreSoFar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(comboBoxNetworksSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Results", panelResults);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonLearnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLearnActionPerformed
        if(!this.verifyInputs())
            return;
        StructureLearningThread thread = new StructureLearningThread((java.awt.Frame)this.getParent());
        thread.start();
        this.saveConfiguration();
        this.tabbedPane.setSelectedComponent(this.panelResults);
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
            activeNetwork = this.comboBoxNetworksSelectionContent[bnIndex];
        MainWindow.getInstance().setActiveNetwork(activeNetwork);
    }//GEN-LAST:event_comboBoxNetworksSelectionActionPerformed

    private void comboBoxMethodItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboBoxMethodItemStateChanged
        boolean bayesianScoringMethod = this.comboBoxMethod.getSelectedIndex() == 1;
        this.textFieldAlpha.setEnabled(bayesianScoringMethod);
    }//GEN-LAST:event_comboBoxMethodItemStateChanged

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        MainWindow.getInstance().saveWindowBounds(this, "DialogStructureLearning");
    }//GEN-LAST:event_formComponentMoved

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        MainWindow.getInstance().saveWindowBounds(this, "DialogStructureLearning");
    }//GEN-LAST:event_formComponentResized

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonLearn;
    private javax.swing.JButton buttonStop;
    private javax.swing.JComboBox comboBoxMethod;
    private javax.swing.JComboBox comboBoxNetworksSelection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel labelBestScoreSoFar;
    private javax.swing.JLabel labelStatus;
    private javax.swing.JPanel panelLearning;
    private javax.swing.JPanel panelResults;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTable tableAllowedConnections;
    private javax.swing.JTable tableEdgeFrequency;
    private javax.swing.JTextField textFieldAlpha;
    private javax.swing.JTextField textFieldIterationCount;
    private javax.swing.JTextField textFieldMaxParents;
    private javax.swing.JTextField textFieldRandomRestartStepcount;
    private javax.swing.JTextField textFieldRunCount;
    private javax.swing.JTextField textFieldTabulistRelsize;
    // End of variables declaration//GEN-END:variables

    @Override
    public void notifyNewActiveDataset(Dataset d) {
        this.dataset = d;
        this.prepareEdgeFrequenciesTable();
        ((AllowedConnectionsTable)this.tableAllowedConnections).prepare();
    }


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
                double equivalentSampleSize = (selectedScoringMethodIndex == 1) ? Double.valueOf(textFieldAlpha.getText()) : 1;
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
                DecomposableScoringMethod scoringMethod;
                if(selectedScoringMethodIndex == 0)
                    scoringMethod = new BICScoringMethod(cachedDataset);
                else
                    scoringMethod = new BayesianScoringMethod(cachedDataset, equivalentSampleSize);
                StructureLearningStatistics statistics = new StructureLearningStatistics(VARIABLES);

                for(int run = 0 ; run < runCount && !learningController.getStopFlag(); run++) {
                    notifyLearningProgess(run, statistics);
                    BayesianNetwork bnInitial = bnEmpty.copyEmptyStructure();
                    StructureLearningAlgorithm learningAlgorithm = new TabuSearchLearningAlgorithm(scoringMethod, tabulistAbssize, randomRestartStepcount);
                    BayesianNetwork resultBN = learningAlgorithm.learn(bnInitial, learningController, constraints);
                    if(learningController.getStopFlag() == true)
                        break;
                    double resultScore =  scoringMethod.absoluteScore(resultBN);
                    statistics.registerLearntNetwork(resultBN, resultScore);
                }
                notifyLearningProgess(runCount, statistics);
                notifyLearningFinished(!learningController.getStopFlag());
            }
            catch(BNLibException ex) {
                notifyLearningFinished(false);
                JOptionPane.showMessageDialog(this.parent, ex.getMessage(), "An error occured", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    
    class AllowedConnectionsTable extends JTable {
        /** Establish the table model. */
        void prepare() {
            if(dataset == null) {
                this.setModel(new DefaultTableModel());
                return;
            }
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
            
            // resize the first column to accomodate for variable names
            int firstColumnWidth = 0;
            for(int row = 0 ; row < this.getRowCount() ; row++) {
                TableCellRenderer renderer = this.getCellRenderer(row, 0);
                Component comp = this.prepareRenderer(renderer, row, 0);
                firstColumnWidth = Math.max (comp.getPreferredSize().width, firstColumnWidth);
            }
            this.getColumnModel().getColumn(0).setPreferredWidth(firstColumnWidth);
            
            
            // on right click show a popup menu to tick/untick all items in a row or column
            this.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if(e.getButton() == MouseEvent.BUTTON3)
                        onRightClickCell(e);
                }
            });
        }
        
        private void onRightClickCell(MouseEvent e) {
            int row = this.rowAtPoint(e.getPoint()),
                column = this.columnAtPoint(e.getPoint());
            final int from = row,
                      to = column - 1;
            // create popup menu
            JMenuItem menuDisableRow = new JMenuItem("Disable row");
            JMenuItem menuEnableRow = new JMenuItem("Enable row");
            JMenuItem menuDisableColumn = new JMenuItem("Disable column");
            JMenuItem menuEnableColumn = new JMenuItem("Enable column");
            menuDisableRow.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { setRow(from, false); }
            });
            menuEnableRow.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { setRow(from, true); }
            });
            menuDisableColumn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { setColumn(to, false); }
            });
            menuEnableColumn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { setColumn(to, true); }
            });
            JPopupMenu menu = new JPopupMenu("Batch settings");
            menu.add(menuDisableRow);
            menu.add(menuEnableRow);
            menu.add(menuDisableColumn);
            menu.add(menuEnableColumn);
            // show menu
            menu.show(this, e.getPoint().x, e.getPoint().y);
        }
        
        private void setRow(int row, boolean connectionAllowed) {
            Variable[] variables = dataset.getVariables();
            int variableCount = variables.length;
            for(int to = 0 ; to < variableCount ; to++)
                this.setConnectionAllowed(row, to, connectionAllowed);
        }
        
        private void setColumn(int col, boolean connectionAllowed) {
            Variable[] variables = dataset.getVariables();
            int variableCount = variables.length;
            for(int from = 0 ; from < variableCount ; from++)
                this.setConnectionAllowed(from, col, connectionAllowed);
        }
        
        private void setConnectionAllowed(int from, int to, boolean allowed) {
            if(from == to)
                return;
            this.getModel().setValueAt(new Boolean(allowed), from, to + 1);
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
