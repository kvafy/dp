// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/16

package bna.view;

import bna.bnlib.BNLibIOException;
import bna.bnlib.BayesianNetwork;
import bna.bnlib.io.BayesianNetworkFileWriter;
import bna.bnlib.io.BayesianNetworkGraphvizFileWriter;
import bna.bnlib.io.BayesianNetworkNetFileWriter;
import bna.bnlib.learning.Dataset;
import bna.bnlib.misc.Toolkit;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.ini4j.Ini;


/**
 * The main window of the whole application (singleton).
 */
public class MainWindow extends javax.swing.JFrame implements ActiveDatasetObserver, ActiveNetworkObserver {
    // singleton
    private static MainWindow instance = new MainWindow();
    // application configuration
    private static final String CONFIG_FILENAME = "config.ini";
    private Ini configuration = null;
    
    
    private MainWindow() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.enableComponentsByState();
        this.configureTooltips();
        this.loadConfiguration();
        
        // the main windows is observer of the current network and of the current dataset
        this.panelNetworkView.addObserver(this);
        ((DatasetViewTable)this.datasetTable).addObserver(this);
        // network viewer observes current dataset (for edge weights)
        ((DatasetViewTable)this.datasetTable).addObserver(this.panelNetworkView);
        
        //javax.swing.JColorChooser.showDialog(this, "HSB colors", null);
    }
    
    public static MainWindow getInstance() {
        return MainWindow.instance;
    }
    
    private void enableComponentsByState() {
        boolean hasNetwork = this.panelNetworkView.hasNetwork();
        boolean hasDataset = ((DatasetViewTable)this.datasetTable).hasDataset();
        // menu "Network" and its items
        this.menuItemSaveNetwork.setEnabled(hasNetwork);
        this.menuItemQuery.setEnabled(hasNetwork);
        this.menuItemNetworkStatistics.setEnabled(hasNetwork);
        this.menuItemShowEdgeWeights.setEnabled(hasNetwork);
        this.menuItemTestPredictionAccuracy.setEnabled(hasNetwork && hasDataset);
        // menu "Dataset" and its items
        this.menuItemExportDataset.setEnabled(hasDataset);
        this.menuItemSampleNewDataset.setEnabled(hasNetwork);
        // menu "Learning" and its items
        this.menuLearning.setEnabled(hasDataset);
        this.menuItemLearnParameters.setEnabled(hasNetwork && hasDataset);
        this.menuItemLearnStructure.setEnabled(hasDataset);
    }
    
    private void configureTooltips() {
        // tooltips are used to show CPDs, so configure the showing/hiding
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    }
    
    /** Loads configuration from a persistent source. */
    private void loadConfiguration() {
        // fill this.configuration variable (either from file or by a blank configuration)
        try {
            this.configuration = new Ini(new FileReader(MainWindow.CONFIG_FILENAME));
        }
        catch(org.ini4j.InvalidFileFormatException ex) {
            this.configuration = new Ini();
        }
        catch(IOException ex) {
            this.configuration = new Ini();
        }
        
        // configure the main window
        this.loadWindowBounds(this, "MainWindow");
        String optionShowEdgeWeightsStr = this.getConfiguration("MainWindow", "show_edge_weights");
        try {
            int optionEdgeWeights = Integer.parseInt(optionShowEdgeWeightsStr);
            this.menuItemShowEdgeWeights.setSelected(optionEdgeWeights != 0);
        }
        catch(NumberFormatException ex) {}
        this.panelNetworkView.setShowEdgeWeights(this.menuItemShowEdgeWeights.isSelected());
    }
    
    /** Saves configurations in persistent form. */
    private void saveConfiguration() {
        // save main window configuration
        String optionShowEdgeWeightsStr = this.menuItemShowEdgeWeights.isSelected() ? "1" : "0";
        this.setConfiguration("MainWindow", "show_edge_weights", optionShowEdgeWeightsStr);
        
        // save options of the whole application to a file
        try {
            if(this.configuration != null)
                this.configuration.store(new File(MainWindow.CONFIG_FILENAME));
        }
        catch(IOException ex) {
            System.err.println("Error saving the configuration to file: " + ex.getMessage());
        }
    }
    
    /**
     * Get value associated with given section and option.
     * @return The string value of null if no such section-option location exists.
     */
    public String getConfiguration(String section, String option) {
        return this.configuration.get(section, option);
    }
    
    /** Set value associated with given section and option. */
    public void setConfiguration(String section, String option, String value) {
        this.configuration.remove(section, option);
        if(value == null)
            return;
        this.configuration.add(section, option, value);
    }
    
    void setActiveNetwork(BayesianNetwork bn) {
        ((NetworkViewPanel)this.panelNetworkView).setNetwork(bn);
    }
    
    @Override
    public void notifyNewActiveDataset(Dataset d) {
        this.enableComponentsByState();
    }
    
    @Override
    public void notifyNewActiveNetwork(GBayesianNetwork gbn) {
        this.enableComponentsByState();
        this.tabbedPane.repaint(); // to repaint the scrollers
    }
    
    void loadWindowBounds(java.awt.Window window, String id) {
        String boundsStr = this.getConfiguration("WindowBounds", id);
        if(boundsStr == null)
            return;
        Pattern pattern = Pattern.compile("^(\\d+)x(\\d+),(\\d+)x(\\d+)$");
        Matcher matcher = pattern.matcher(boundsStr);
        if(!matcher.matches())
            return;
        int x = Integer.valueOf(matcher.group(1)),
            y = Integer.valueOf(matcher.group(2)), 
            width = Integer.valueOf(matcher.group(3)),
            height = Integer.valueOf(matcher.group(4));
        if(window != this) {
            // compensate for insents (titlebar and borders) of the main window
            x -= this.getInsets().left;
            y -= this.getInsets().top;
        }
        window.setBounds(x, y, width, height);
    }
    
    void saveWindowBounds(java.awt.Window window, String id) {
        Rectangle bounds = window.getBounds();
        String boundsStr = String.format("%dx%d,%dx%d", bounds.x, bounds.y, bounds.width, bounds.height);
        this.setConfiguration("WindowBounds", id, boundsStr);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        paneNetworkView = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        datasetTable = new DatasetViewTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuItemExit = new javax.swing.JMenuItem();
        menuNetwork = new javax.swing.JMenu();
        menuItemLoadNetwork = new javax.swing.JMenuItem();
        menuItemSaveNetwork = new javax.swing.JMenuItem();
        menuItemQuery = new javax.swing.JMenuItem();
        menuItemNetworkStatistics = new javax.swing.JMenuItem();
        menuItemShowEdgeWeights = new javax.swing.JCheckBoxMenuItem();
        menuItemTestPredictionAccuracy = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        menuDataset = new javax.swing.JMenu();
        menuItemImportDataset = new javax.swing.JMenuItem();
        menuItemExportDataset = new javax.swing.JMenuItem();
        menuItemSampleNewDataset = new javax.swing.JMenuItem();
        menuLearning = new javax.swing.JMenu();
        menuItemLearnParameters = new javax.swing.JMenuItem();
        menuItemLearnStructure = new javax.swing.JMenuItem();
        menuAbout = new javax.swing.JMenu();
        menuItemCrimeImpact = new javax.swing.JMenuItem();
        menuItemStructuresIntersection = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Bayesian networks applications (Master's thesis)");
        setName("frameMainWindow");
        setPreferredSize(new java.awt.Dimension(800, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        tabbedPane.addTab("Network view", paneNetworkView);
        paneNetworkView.setViewportView(panelNetworkView);
        panelNetworkView.setLayout(null);
        panelNetworkView.setPreferredSize(null);

        datasetTable.setModel(DatasetViewTable.EMPTY_TABLE_MODEL);
        datasetTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        datasetTable.setEnabled(false);
        jScrollPane1.setViewportView(datasetTable);

        tabbedPane.addTab("Dataset view", jScrollPane1);

        menuFile.setText("File");

        menuItemExit.setText("Exit");
        menuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExitActionPerformed(evt);
            }
        });
        menuFile.add(menuItemExit);

        jMenuBar1.add(menuFile);

        menuNetwork.setText("Network");

        menuItemLoadNetwork.setText("Load from file");
        menuItemLoadNetwork.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemLoadNetworkActionPerformed(evt);
            }
        });
        menuNetwork.add(menuItemLoadNetwork);

        menuItemSaveNetwork.setText("Save to file");
        menuItemSaveNetwork.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveNetworkActionPerformed(evt);
            }
        });
        menuNetwork.add(menuItemSaveNetwork);

        menuItemQuery.setText("Probabilistic query");
        menuItemQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemQueryActionPerformed(evt);
            }
        });
        menuNetwork.add(menuItemQuery);

        menuItemNetworkStatistics.setText("Show statistics");
        menuItemNetworkStatistics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemNetworkStatisticsActionPerformed(evt);
            }
        });
        menuNetwork.add(menuItemNetworkStatistics);

        menuItemShowEdgeWeights.setSelected(true);
        menuItemShowEdgeWeights.setText("Display edge weights (needs a dataset)");
        menuItemShowEdgeWeights.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemShowEdgeWeightsActionPerformed(evt);
            }
        });
        menuNetwork.add(menuItemShowEdgeWeights);

        menuItemTestPredictionAccuracy.setText("Test prediction accuracy");
        menuItemTestPredictionAccuracy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemTestPredictionAccuracyActionPerformed(evt);
            }
        });
        menuNetwork.add(menuItemTestPredictionAccuracy);

        jMenuItem1.setText("Compare structure");
        menuNetwork.add(jMenuItem1);

        jMenuItem2.setText("Structural intersection");
        menuNetwork.add(jMenuItem2);

        jMenuBar1.add(menuNetwork);

        menuDataset.setText("Dataset");

        menuItemImportDataset.setText("Import CSV");
        menuItemImportDataset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemImportDatasetActionPerformed(evt);
            }
        });
        menuDataset.add(menuItemImportDataset);

        menuItemExportDataset.setText("Export CSV");
        menuItemExportDataset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExportDatasetActionPerformed(evt);
            }
        });
        menuDataset.add(menuItemExportDataset);

        menuItemSampleNewDataset.setText("Produce by sampling");
        menuItemSampleNewDataset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSampleNewDatasetActionPerformed(evt);
            }
        });
        menuDataset.add(menuItemSampleNewDataset);

        jMenuBar1.add(menuDataset);

        menuLearning.setText("Learning");

        menuItemLearnParameters.setText("Parameter learning");
        menuItemLearnParameters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemLearnParametersActionPerformed(evt);
            }
        });
        menuLearning.add(menuItemLearnParameters);

        menuItemLearnStructure.setText("Structure learning");
        menuItemLearnStructure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemLearnStructureActionPerformed(evt);
            }
        });
        menuLearning.add(menuItemLearnStructure);

        jMenuBar1.add(menuLearning);

        menuAbout.setText("About");

        menuItemCrimeImpact.setText("Magic (crime impact factor)");
        menuItemCrimeImpact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemCrimeImpactActionPerformed(evt);
            }
        });
        menuAbout.add(menuItemCrimeImpact);

        menuItemStructuresIntersection.setText("Structures intersection");
        menuItemStructuresIntersection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemStructuresIntersectionActionPerformed(evt);
            }
        });
        menuAbout.add(menuItemStructuresIntersection);

        jMenuBar1.add(menuAbout);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 607, Short.MAX_VALUE)
                .addGap(14, 14, 14))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExitActionPerformed
        this.dispose();
    }//GEN-LAST:event_menuItemExitActionPerformed

    private void menuItemLoadNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemLoadNetworkActionPerformed
        String lastNetworkDirectory = this.getConfiguration("Network", "directory");
        if(lastNetworkDirectory == null)
            lastNetworkDirectory = ".";
        JFileChooser networkFileChooser = new JFileChooser(lastNetworkDirectory);
        networkFileChooser.setFileFilter(new FileNameExtensionFilter("Net file", "net"));
        networkFileChooser.setDialogTitle("Load a Bayesian network from file");
        networkFileChooser.showOpenDialog(this);
        if(networkFileChooser.getSelectedFile() == null)
            return;
        this.setConfiguration("Network", "directory", networkFileChooser.getSelectedFile().getParent());
        try {
            this.panelNetworkView.setNetwork((GBayesianNetwork)null);
            BayesianNetwork bn = BayesianNetwork.loadFromFile(networkFileChooser.getSelectedFile().getAbsolutePath());
            this.panelNetworkView.setNetwork(bn);
        }
        catch(BNLibIOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "I/O error while loading network", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_menuItemLoadNetworkActionPerformed

    private void menuItemQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemQueryActionPerformed
        DialogQuerySampling dialog = new DialogQuerySampling(this, false, this.panelNetworkView);
        dialog.setVisible(true);
    }//GEN-LAST:event_menuItemQueryActionPerformed

    private void menuItemImportDatasetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemImportDatasetActionPerformed
        DialogDatasetIO dialog = new DialogDatasetIO(this, true, DialogDatasetIO.ACTION_LOAD);
        dialog.setVisible(true);
        if(dialog.confirmed)
            ((DatasetViewTable)this.datasetTable).setDataset(dialog.dataset);
    }//GEN-LAST:event_menuItemImportDatasetActionPerformed

    private void menuItemExportDatasetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExportDatasetActionPerformed
        DialogDatasetIO dialog = new DialogDatasetIO(this, true, DialogDatasetIO.ACTION_SAVE);
        // install the dataset to be saved
        dialog.dataset = ((DatasetViewTable)this.datasetTable).getDataset();
        dialog.setVisible(true);
    }//GEN-LAST:event_menuItemExportDatasetActionPerformed

    private void menuItemSampleNewDatasetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSampleNewDatasetActionPerformed
        BayesianNetwork bn = ((NetworkViewPanel)this.panelNetworkView).getNetwork();
        if(bn == null || !bn.hasValidCPDs()) {
            String msg = "Current network has invalid CPDs and therefore cannot be sampled.\n"
                       + "You probably learnt just network structure but not the parameters.";
            JOptionPane.showMessageDialog(this,msg, "Cannot sample network", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DialogSampleDataset dialog = new DialogSampleDataset(this, true, bn);
        dialog.setVisible(true);
        if(dialog.confirmed)
            ((DatasetViewTable)this.datasetTable).setDataset(dialog.dataset);
    }//GEN-LAST:event_menuItemSampleNewDatasetActionPerformed

    private void menuItemLearnParametersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemLearnParametersActionPerformed
        BayesianNetwork bn = ((NetworkViewPanel)this.panelNetworkView).getNetwork();
        Dataset dataset = ((DatasetViewTable)this.datasetTable).getDataset();
        if(!dataset.containsVariables(bn.getVariables())) {
            String msg = "Current network may not contain any variable that\n"
                       + "isn't present in the current dataset.";
            JOptionPane.showMessageDialog(this, msg, "Incompatible network and dataset", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DialogParametersLearning dialog = new DialogParametersLearning(this, true, bn, dataset);
        dialog.setVisible(true);
        if(dialog.confirmed)
            ((NetworkViewPanel)this.panelNetworkView).setNetwork(dialog.bnLearnt);
    }//GEN-LAST:event_menuItemLearnParametersActionPerformed

    private void menuItemLearnStructureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemLearnStructureActionPerformed
        BayesianNetwork bnCurrent = ((NetworkViewPanel)this.panelNetworkView).getNetwork();
        DatasetViewTable datasetViewer = (DatasetViewTable)this.datasetTable;
        DialogStructureLearning dialog = new DialogStructureLearning(this, false, bnCurrent, datasetViewer);
        dialog.setVisible(true);
    }//GEN-LAST:event_menuItemLearnStructureActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        this.saveWindowBounds(this, "MainWindow");
        this.saveConfiguration();
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.saveWindowBounds(this, "MainWindow");
        this.saveConfiguration();
    }//GEN-LAST:event_formWindowClosing

    private void menuItemNetworkStatisticsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemNetworkStatisticsActionPerformed
        BayesianNetwork bnCurrent = ((NetworkViewPanel)this.panelNetworkView).getNetwork();
        DialogNetworkStatistics dialog = new DialogNetworkStatistics(this, false, bnCurrent);
        dialog.setVisible(true);
    }//GEN-LAST:event_menuItemNetworkStatisticsActionPerformed

    private void menuItemTestPredictionAccuracyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemTestPredictionAccuracyActionPerformed
        BayesianNetwork bnCurrent = ((NetworkViewPanel)this.panelNetworkView).getNetwork();
        Dataset dataset = ((DatasetViewTable)this.datasetTable).getDataset();
        if(!bnCurrent.hasValidCPDs()) {
            String msg = "Current network may not contain any variable that\n"
                       + "isn't present in the current dataset.";
            JOptionPane.showMessageDialog(this,msg, "Cannot use network for classification", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(!dataset.containsVariables(bnCurrent.getVariables())) {
            String msg = "Current network and current dataset must contain exactly the same variables\n"
                       + "with the same sets of possible assignments.";
            JOptionPane.showMessageDialog(this, msg, "Incompatible network and dataset", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DialogPredictionTest dialog = new DialogPredictionTest(this, true, bnCurrent, dataset);
        dialog.setVisible(true);
    }//GEN-LAST:event_menuItemTestPredictionAccuracyActionPerformed

    private void menuItemSaveNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveNetworkActionPerformed
        String lastNetworkDirectory = this.getConfiguration("Network", "directory");
        if(lastNetworkDirectory == null)
            lastNetworkDirectory = ".";
        JFileChooser networkFileChooser = new JFileChooser(lastNetworkDirectory);
        networkFileChooser.setFileFilter(new FileNameExtensionFilter("Net file", "net"));
        networkFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Graphviz file", "gv"));
        networkFileChooser.setDialogTitle("Save the current network to file");
        networkFileChooser.showSaveDialog(this);
        File selectedFile = networkFileChooser.getSelectedFile();
        if(selectedFile == null)
            return;
        this.setConfiguration("Network", "directory", selectedFile.getParent());
        try {
            
            BayesianNetwork bnCurrent = this.panelNetworkView.getNetwork();
            BayesianNetworkFileWriter writer;
            if(selectedFile.getName().endsWith(".gv"))
                writer = new BayesianNetworkGraphvizFileWriter(selectedFile.getPath());
            else
                writer = new BayesianNetworkNetFileWriter(selectedFile.getPath());
            writer.save(bnCurrent);
        }
        catch(BNLibIOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error saving the network", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_menuItemSaveNetworkActionPerformed

    private void menuItemShowEdgeWeightsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemShowEdgeWeightsActionPerformed
        boolean showEdgeWeights = this.menuItemShowEdgeWeights.isSelected();
        this.panelNetworkView.setShowEdgeWeights(showEdgeWeights);
    }//GEN-LAST:event_menuItemShowEdgeWeightsActionPerformed

    private void menuItemCrimeImpactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemCrimeImpactActionPerformed
        class VariableDeviation implements Comparable {
            String variable;
            double deviation;
            public VariableDeviation(String variable, double deviation) {
                this.variable = variable;
                this.deviation = deviation;
            }
            @Override
            public int compareTo(Object o) {
                return (int)Math.signum(this.deviation - ((VariableDeviation)o).deviation);
            }
        }
        
        final long SAMPLE_COUNT = 1 * 1000 * 1000;
        final int THREAD_COUNT = 6;
        final String TARGET_VAR = "ViolentCrimesPerPop";
        BayesianNetwork bn = (this.panelNetworkView).getNetwork();
        bna.bnlib.sampling.SamplingController controller = new bna.bnlib.sampling.SamplingController(SAMPLE_COUNT);
        java.util.List<VariableDeviation> deviations = new java.util.ArrayList<VariableDeviation>();
        System.out.print(String.format("sensitivity analysis %2d/%2d", 0, bn.getVariablesCount()));
        int i = 0;
        for(bna.bnlib.Variable var : bn.getVariables()) {
            if(var.getName().equals(TARGET_VAR))
                continue;
            String queryStr = String.format("P(%s | %s)", TARGET_VAR, var.getName());
            bna.bnlib.sampling.SampleProducer producer = new bna.bnlib.sampling.WeightedSampleProducer(bn, queryStr);
            bna.bnlib.sampling.QuerySamplerMultithreaded sampler = new bna.bnlib.sampling.QuerySamplerMultithreaded(producer, THREAD_COUNT);
            sampler.sample(controller);
            // determine the maximal deviation
            bna.bnlib.Factor cpd = sampler.getSamplesCounterNormalized();
            double maxDeviation = 0;
            for(int[] assignment : cpd) {
                maxDeviation = Math.max(maxDeviation, Math.abs(1.0 / 3.0 - cpd.getProbability(assignment)));
            }
            deviations.add(new VariableDeviation(var.getName(), maxDeviation));
            System.out.print(String.format("\rsensitivity analysis %2d/%2d", ++i, bn.getVariablesCount()));
        }
        System.out.println("");
        java.util.Collections.sort(deviations);
        System.out.println("Variable deviations from " + TARGET_VAR);
        i = 0;
        for(VariableDeviation varDev : deviations) {
            System.out.printf("%2d) %s - %.3f\n", ++i, varDev.variable, varDev.deviation);
        }
    }//GEN-LAST:event_menuItemCrimeImpactActionPerformed

    private void menuItemStructuresIntersectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemStructuresIntersectionActionPerformed
        File directory = new File("/home/kvafy/skola/dp/2-crime/round-4");
        java.util.LinkedList<File> networkFiles = new java.util.LinkedList<File>();
        for(File file : directory.listFiles()) {
            if(file.getName().endsWith(".net")) {
                networkFiles.addLast(file);
                System.out.println("considering network " + file.getName());
            }
        }
        
        /*BayesianNetwork bnIntersection = BayesianNetwork.loadFromFile(networkFiles.removeFirst().getPath());
        for(File file : networkFiles) {
            BayesianNetwork bnOther = BayesianNetwork.loadFromFile(file.getPath());
            for(bna.bnlib.Node node : bnIntersection.getNodes()) {
                bna.bnlib.Node otherNode = bnOther.getNode(node.getVariable());
                for(bna.bnlib.Node child : node.getChildNodes()) {
                    if(!Toolkit.arrayContains(otherNode.getChildVariables(), child.getVariable()))
                        bnIntersection.removeDependency(node, child);
                }
            }
        }*/
        
        final int ABSENCE_TOLERANCE = 1;
        
        bna.bnlib.Variable[] varsOrder = BayesianNetwork.loadFromFile(networkFiles.peekFirst().getPath()).getVariables();
        int edgeCounter[][] = new int[varsOrder.length][varsOrder.length];
        for(File file : networkFiles) {
            BayesianNetwork bn = BayesianNetwork.loadFromFile(file.getPath());
            for(bna.bnlib.Node node : bn.getNodes()) {
                bna.bnlib.Variable parent = node.getVariable();
                int parentIndex = Toolkit.indexOf(varsOrder, parent);
                for(bna.bnlib.Variable child : node.getChildVariables()) {
                    int childIndex = Toolkit.indexOf(varsOrder, child);
                    edgeCounter[parentIndex][childIndex]++;
                }
            }
        }
        BayesianNetwork bnIntersection = new BayesianNetwork(varsOrder);
        for(int i = 0 ; i < varsOrder.length ; i++) {
            for(int j = 0 ; j < varsOrder.length ; j++) {
                if(edgeCounter[i][j] >= networkFiles.size() - ABSENCE_TOLERANCE) // tolerate if the edge is missing ABSENCE_TOLERANCE-times
                    bnIntersection.addDependency(varsOrder[i], varsOrder[j]);
            }
        }
        
        this.panelNetworkView.setNetwork(bnIntersection);
    }//GEN-LAST:event_menuItemStructuresIntersectionActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        // Create and display the form
        final MainWindow mainWindow = MainWindow.getInstance();
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainWindow.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable datasetTable;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenu menuAbout;
    private javax.swing.JMenu menuDataset;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuItemCrimeImpact;
    private javax.swing.JMenuItem menuItemExit;
    private javax.swing.JMenuItem menuItemExportDataset;
    private javax.swing.JMenuItem menuItemImportDataset;
    private javax.swing.JMenuItem menuItemLearnParameters;
    private javax.swing.JMenuItem menuItemLearnStructure;
    private javax.swing.JMenuItem menuItemLoadNetwork;
    private javax.swing.JMenuItem menuItemNetworkStatistics;
    private javax.swing.JMenuItem menuItemQuery;
    private javax.swing.JMenuItem menuItemSampleNewDataset;
    private javax.swing.JMenuItem menuItemSaveNetwork;
    private javax.swing.JCheckBoxMenuItem menuItemShowEdgeWeights;
    private javax.swing.JMenuItem menuItemStructuresIntersection;
    private javax.swing.JMenuItem menuItemTestPredictionAccuracy;
    private javax.swing.JMenu menuLearning;
    private javax.swing.JMenu menuNetwork;
    private javax.swing.JScrollPane paneNetworkView;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
    private NetworkViewPanel panelNetworkView = new NetworkViewPanel();
}
