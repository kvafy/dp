// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/16

package bna.view;

import bna.bnlib.BNLibIOException;
import bna.bnlib.BayesianNetwork;
import bna.bnlib.Variable;
import bna.bnlib.io.BayesianNetworkFileWriter;
import bna.bnlib.io.BayesianNetworkNetFileWriter;
import bna.bnlib.learning.Dataset;
import bna.bnlib.misc.Toolkit;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import org.ini4j.Ini;


/**
 * The main window of the whole application.
 */
public class MainWindow extends javax.swing.JFrame {
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
    }
    
    /** Saves configurations in persistent form. */
    private void saveConfiguration() {
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
    
    void notifyActiveNetworkChange() {
        this.enableComponentsByState();
    }
    
    void notifyActiveDatasetChange() {
        this.enableComponentsByState();
    }
    
    private boolean networkAndDatasetAreCompatible() {
        BayesianNetwork currentNetwork = ((NetworkViewPanel)this.panelNetworkView).getNetwork();
        Variable[] networkVariables = currentNetwork.getVariables();
        Dataset currentDataset = ((DatasetViewTable)this.datasetTable).getDataset();
        Variable[] datasetVariables = currentDataset.getVariables();
        // test whether variables have the same name
        boolean sameVariableNames = Toolkit.areEqual(networkVariables, datasetVariables);
        if(!sameVariableNames)
            return false;
        // test whether variables have the same set of values
        for(Variable bnVar : networkVariables) {
            int index = Toolkit.indexOf(datasetVariables, bnVar);
            Variable datasetVar = datasetVariables[index];
            if(!Toolkit.areEqual(bnVar.getValues(), datasetVar.getValues()))
                return false;
        }
        return true;
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
        menuItemTestPredictionAccuracy = new javax.swing.JMenuItem();
        menuDataset = new javax.swing.JMenu();
        menuItemImportDataset = new javax.swing.JMenuItem();
        menuItemExportDataset = new javax.swing.JMenuItem();
        menuItemSampleNewDataset = new javax.swing.JMenuItem();
        menuLearning = new javax.swing.JMenu();
        menuItemLearnParameters = new javax.swing.JMenuItem();
        menuItemLearnStructure = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Bayesian networks applications");
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

        menuItemQuery.setText("Query");
        menuItemQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemQueryActionPerformed(evt);
            }
        });
        menuNetwork.add(menuItemQuery);

        menuItemNetworkStatistics.setText("Statistics");
        menuItemNetworkStatistics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemNetworkStatisticsActionPerformed(evt);
            }
        });
        menuNetwork.add(menuItemNetworkStatistics);

        menuItemTestPredictionAccuracy.setText("Test prediction accuracy");
        menuItemTestPredictionAccuracy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemTestPredictionAccuracyActionPerformed(evt);
            }
        });
        menuNetwork.add(menuItemTestPredictionAccuracy);

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

        menuItemLearnStructure.setText("Structure learning (tabu search)");
        menuItemLearnStructure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemLearnStructureActionPerformed(evt);
            }
        });
        menuLearning.add(menuItemLearnStructure);

        jMenuBar1.add(menuLearning);

        menuHelp.setText("Help");
        jMenuBar1.add(menuHelp);

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
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 412, Short.MAX_VALUE)
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
        BayesianNetwork bn = this.panelNetworkView.getNetwork();
        if(bn == null || !bn.hasValidCPDs()) {
            String msg = "Current network has invalid CPDs and therefore cannot be sampled.\n"
                       + "You probably learnt just network structure but not parameters.";
            JOptionPane.showMessageDialog(this,msg, "Cannot sample network", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DialogQuerySampling dialog = new DialogQuerySampling(this, false, bn);
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
        if(!this.networkAndDatasetAreCompatible()) {
            String msg = "Current network and current dataset must contain exactly the same variables\n"
                       + "with the same sets of possible assignments.";
            JOptionPane.showMessageDialog(this, msg, "Incompatible network and dataset", JOptionPane.ERROR_MESSAGE);
            return;
        }
        BayesianNetwork bn = ((NetworkViewPanel)this.panelNetworkView).getNetwork();
        Dataset dataset = ((DatasetViewTable)this.datasetTable).getDataset();
        DialogParametersLearning dialog = new DialogParametersLearning(this, true, bn, dataset);
        dialog.setVisible(true);
        if(dialog.confirmed)
            ((NetworkViewPanel)this.panelNetworkView).setNetwork(dialog.bnLearnt);
    }//GEN-LAST:event_menuItemLearnParametersActionPerformed

    private void menuItemLearnStructureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemLearnStructureActionPerformed
        Dataset dataset = ((DatasetViewTable)this.datasetTable).getDataset();
        BayesianNetwork bnCurrent = ((NetworkViewPanel)this.panelNetworkView).getNetwork();
        DialogStructureLearning dialog = new DialogStructureLearning(this, false, dataset, bnCurrent);
        dialog.setVisible(true);
    }//GEN-LAST:event_menuItemLearnStructureActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        this.saveConfiguration();
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
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
            String msg = "Current network has invalid CPDs and therefore cannot be used for classification.\n"
                       + "You probably learnt just network structure but not the parameters.";
            JOptionPane.showMessageDialog(this,msg, "Cannot use network for classification", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(!this.networkAndDatasetAreCompatible()) {
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
        networkFileChooser.setDialogTitle("Save the current network to file");
        networkFileChooser.showSaveDialog(this);
        if(networkFileChooser.getSelectedFile() == null)
            return;
        this.setConfiguration("Network", "directory", networkFileChooser.getSelectedFile().getParent());
        try {
            BayesianNetwork bnCurrent = this.panelNetworkView.getNetwork();
            BayesianNetworkFileWriter writer = new BayesianNetworkNetFileWriter(networkFileChooser.getSelectedFile().getPath());
            writer.save(bnCurrent);
        }
        catch(BNLibIOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error saving the network", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_menuItemSaveNetworkActionPerformed

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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenu menuDataset;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuHelp;
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
    private javax.swing.JMenuItem menuItemTestPredictionAccuracy;
    private javax.swing.JMenu menuLearning;
    private javax.swing.JMenu menuNetwork;
    private javax.swing.JScrollPane paneNetworkView;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
    private NetworkViewPanel panelNetworkView = new NetworkViewPanel();
}
