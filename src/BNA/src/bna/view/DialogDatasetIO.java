// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/31

package bna.view;

import bna.bnlib.BNLibIOException;
import bna.bnlib.learning.Dataset;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 * Dialog that collects specification of a csv file with dataset and saves/loads a dataset.
 * If a dataset is loaded, it is returned in the "dataset" attribute.
 * If a dataset is to be saved, the caller needs to fill the "dataset" attribute
 * before invoking dialog.setVisible(true).
 */
public class DialogDatasetIO extends javax.swing.JDialog {
    public static final int ACTION_SAVE = 0,
                            ACTION_LOAD = 1;
    private int action;
    private String datasetDirectory = null;
    
    Dataset dataset = null;
    boolean confirmed = false;

    
    /**
     * Creates new form DialogLoadDataset
     */
    public DialogDatasetIO(java.awt.Frame parent, boolean modal, int action) {
        super(parent, modal);
        initComponents();
        this.loadConfiguration();
        this.setLocationRelativeTo(this.getParent());
        this.action = action;
        this.setCustomizeByAction();
    }
    
    private void loadConfiguration() {
        MainWindow mw = MainWindow.getInstance();
        // directory from which to load dataset
        this.datasetDirectory = mw.getConfiguration("Dataset", "directory");
        if(this.datasetDirectory == null || this.datasetDirectory.isEmpty())
            this.datasetDirectory = ".";
        // csv separator
        this.textFieldSeparator.setText(mw.getConfiguration("Dataset", "separator"));
    }
    
    private void saveConfiguration() {
        MainWindow mw = MainWindow.getInstance();
        mw.setConfiguration("Dataset", "directory", this.datasetDirectory);
        mw.setConfiguration("Dataset", "separator", this.textFieldSeparator.getText());
    }
    
    private void setCustomizeByAction() {
        switch(this.action) {
            case DialogDatasetIO.ACTION_LOAD:
                this.buttonConfirm.setText("Load");
                this.labelPath.setText("Input CSV file");
                this.setTitle("Import dataset from a CSV file");
                break;
            case DialogDatasetIO.ACTION_SAVE:
                this.buttonConfirm.setText("Save");
                this.labelPath.setText("Output CSV file");
                this.setTitle("Export dataset to a CSV file");
                break;
        }
    }

    private boolean verifyInputs() {
        String filenameStr = this.textFieldFilename.getText(),
               separatorStr = this.textFieldSeparator.getText();
        if(filenameStr.isEmpty()) {
            String msg = "Filename field cannot be empty (click to select a file).";
            JOptionPane.showMessageDialog(this, msg, "Incomplete inputs", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(separatorStr.isEmpty()) {
            String msg = "Separator field cannot be empty.";
            JOptionPane.showMessageDialog(this, msg, "Incomplete inputs", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void performLoad() {
        String datafile = this.textFieldFilename.getText(),
               separator = this.textFieldSeparator.getText();
        try {
            this.dataset = Dataset.loadCSVFile(datafile, separator);
            if(dataset.getVariables().length == 1) {
                String msg = "The loaded dataset contains only one column. You probably entered\n"
                           + "wrong column separator. Accept the loaded dataset anyway?",
                    title = "Suspicious dataset";
                int choice = JOptionPane.showConfirmDialog(this, msg, title,
                                                           JOptionPane.OK_CANCEL_OPTION,
                                                           JOptionPane.QUESTION_MESSAGE);
                if(choice != JOptionPane.OK_OPTION)
                    return;
            }
            this.confirmed = true;
            this.saveConfiguration();
            this.dispose();
        }
        catch(BNLibIOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "IO Errror", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performSave() {
        String datafile = this.textFieldFilename.getText(),
               separator = this.textFieldSeparator.getText();
        try {
            this.dataset.saveCSVFile(datafile, separator);
            this.confirmed = true;
            this.saveConfiguration();
            this.dispose();
        }
        catch(BNLibIOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "IO Errror", JOptionPane.ERROR_MESSAGE);
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

        labelPath = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        textFieldSeparator = new javax.swing.JTextField();
        textFieldFilename = new javax.swing.JTextField();
        buttonConfirm = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Import dataset from a CSV file");

        labelPath.setText("CSV file");

        jLabel2.setText("Record separator");

        textFieldFilename.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                textFieldFilenameMouseClicked(evt);
            }
        });

        buttonConfirm.setText("Load");
        buttonConfirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConfirmActionPerformed(evt);
            }
        });

        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(labelPath))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textFieldFilename)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(textFieldSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 165, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPath)
                    .addComponent(textFieldFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(textFieldSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonConfirm)
                    .addComponent(buttonCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConfirmActionPerformed
        if(!this.verifyInputs())
            return;
        if(this.action == DialogDatasetIO.ACTION_LOAD)
            this.performLoad();
        else
            this.performSave();
    }//GEN-LAST:event_buttonConfirmActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        this.confirmed = false;
        this.dispose();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void textFieldFilenameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_textFieldFilenameMouseClicked
        JFileChooser chooser = new JFileChooser(this.datasetDirectory);
        if(this.action == DialogDatasetIO.ACTION_LOAD) {
            chooser.setDialogTitle("Pick a csv file to load");
            chooser.showOpenDialog(this);
        }
        else {
             chooser.setDialogTitle("Pick a destination csv file");
             chooser.showSaveDialog(this);
        }
        
        if(chooser.getSelectedFile() != null) {
            this.textFieldFilename.setText(chooser.getSelectedFile().toString());
            this.datasetDirectory = chooser.getSelectedFile().getParent();
        }
    }//GEN-LAST:event_textFieldFilenameMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonConfirm;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel labelPath;
    private javax.swing.JTextField textFieldFilename;
    private javax.swing.JTextField textFieldSeparator;
    // End of variables declaration//GEN-END:variables
}
