// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/16

package bna.view;

import bna.bnlib.BNLibIOException;
import bna.bnlib.BNLibIllegalNetworkSpecificationException;
import bna.bnlib.BayesianNetwork;
import bna.bnlib.BayesianNetworkException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 * The main window of the whole application.
 */
public class MainWindow extends javax.swing.JFrame {
    // singleton
    private static MainWindow instance = new MainWindow();
    // application configuration
    

    
    private MainWindow() {
        initComponents();
        this.enableComponentsByState();
    }
    
    public static MainWindow getInstance() {
        return MainWindow.instance;
    }
    
    public void enableComponentsByState() {
        boolean hasNetwork = this.panelNetworkView.hasNetwork();
        boolean hasDataset = false;
        // menu "Network" and its items
        this.menuItemSaveNetwork.setEnabled(hasNetwork);
        this.menuItemQuery.setEnabled(hasNetwork);
        // menu "Dataset" and its items
        this.menuItemSaveDataset.setEnabled(hasDataset);
        this.menuItemSampleNewDataset.setEnabled(hasNetwork);
        // menu "Learning" and its items
        this.menuLearning.setEnabled(hasDataset);
        this.menuItemLearnParameters.setEnabled(hasNetwork && hasDataset);
        this.menuItemLearnStructure.setEnabled(hasDataset);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        paneNetworkView = new javax.swing.JScrollPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuItemExit = new javax.swing.JMenuItem();
        menuNetwork = new javax.swing.JMenu();
        menuItemLoadNetwork = new javax.swing.JMenuItem();
        menuItemSaveNetwork = new javax.swing.JMenuItem();
        menuItemQuery = new javax.swing.JMenuItem();
        menuDataset = new javax.swing.JMenu();
        menuItemLoadDataset = new javax.swing.JMenuItem();
        menuItemSaveDataset = new javax.swing.JMenuItem();
        menuItemSampleNewDataset = new javax.swing.JMenuItem();
        menuLearning = new javax.swing.JMenu();
        menuItemLearnParameters = new javax.swing.JMenuItem();
        menuItemLearnStructure = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Bayesian networks applications");
        setName("frameMainWindow");
        setPreferredSize(new java.awt.Dimension(800, 600));

        paneNetworkView.setViewport(null);

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
        menuNetwork.add(menuItemSaveNetwork);

        menuItemQuery.setText("Query");
        menuNetwork.add(menuItemQuery);

        jMenuBar1.add(menuNetwork);

        menuDataset.setText("Dataset");

        menuItemLoadDataset.setText("Load from file");
        menuDataset.add(menuItemLoadDataset);

        menuItemSaveDataset.setText("Save to file");
        menuDataset.add(menuItemSaveDataset);

        menuItemSampleNewDataset.setText("Produce by sampling");
        menuDataset.add(menuItemSampleNewDataset);

        jMenuBar1.add(menuDataset);

        menuLearning.setText("Learning");

        menuItemLearnParameters.setText("Parameter learning");
        menuLearning.add(menuItemLearnParameters);

        menuItemLearnStructure.setText("Structure learning (tabu search)");
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
                .addComponent(paneNetworkView, javax.swing.GroupLayout.DEFAULT_SIZE, 609, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(paneNetworkView, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addContainerGap())
        );

        paneNetworkView.setViewportView(panelNetworkView);
        panelNetworkView.setLayout(null);
        panelNetworkView.setPreferredSize(null);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExitActionPerformed
        this.dispose();
    }//GEN-LAST:event_menuItemExitActionPerformed

    private void menuItemLoadNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemLoadNetworkActionPerformed
        String startDirector = ".";
        JFileChooser networkFileChooser = new JFileChooser(startDirector);
        networkFileChooser.setDialogTitle("Load a Bayesian network from file");
        networkFileChooser.showOpenDialog(this);
        if(networkFileChooser.getSelectedFile() == null)
            return;
        try {
            BayesianNetwork bn = BayesianNetwork.loadFromFile(networkFileChooser.getSelectedFile().getAbsolutePath());
            this.panelNetworkView.setNetwork(bn);
        }
        catch(BNLibIOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "I/O error while loading network", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_menuItemLoadNetworkActionPerformed

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
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu menuDataset;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenuItem menuItemExit;
    private javax.swing.JMenuItem menuItemLearnParameters;
    private javax.swing.JMenuItem menuItemLearnStructure;
    private javax.swing.JMenuItem menuItemLoadDataset;
    private javax.swing.JMenuItem menuItemLoadNetwork;
    private javax.swing.JMenuItem menuItemQuery;
    private javax.swing.JMenuItem menuItemSampleNewDataset;
    private javax.swing.JMenuItem menuItemSaveDataset;
    private javax.swing.JMenuItem menuItemSaveNetwork;
    private javax.swing.JMenu menuLearning;
    private javax.swing.JMenu menuNetwork;
    private javax.swing.JScrollPane paneNetworkView;
    // End of variables declaration//GEN-END:variables
    private NetworkViewPanel panelNetworkView = new NetworkViewPanel();
}
