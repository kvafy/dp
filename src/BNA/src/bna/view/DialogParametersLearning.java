// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/16

package bna.view;

import bna.bnlib.BNLibInconsistentVariableSetsException;
import bna.bnlib.BayesianNetwork;
import bna.bnlib.learning.Dataset;
import bna.bnlib.learning.ParameterLearner;
import javax.swing.JOptionPane;


/**
 * Dialog that takes a BN and allows to sample it with various options.
 */
public class DialogParametersLearning extends javax.swing.JDialog {
    private BayesianNetwork bnOriginal;
    private Dataset dataset;
    BayesianNetwork bnLearnt = null;
    boolean confirmed = false;

    /**
     * Creates new form DialogSampling
     */
    public DialogParametersLearning(java.awt.Frame parent, boolean modal, BayesianNetwork bn, Dataset dataset) {
        super(parent, modal);
        initComponents();
        this.setLocationRelativeTo(parent);
        this.bnOriginal = bn;
        this.dataset = dataset;
    }
    
    private boolean verifyInputs() {
        int method = this.comboBoxMethod.getSelectedIndex();
        if(method != 0 && method != 1) {
            String msg = "No learning method is selected.";
            JOptionPane.showMessageDialog(this, msg, "Invalid learning specification", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(this.bayesianEstimationSelected()) {
            try {
                double alpha = Double.parseDouble(this.textFieldEquivalentSampleSize.getText());
                if(alpha < 0) {
                    String msg = "The equivalent sample size has to be non-negative.";
                    JOptionPane.showMessageDialog(this, msg, "Invalid learning specification", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            catch(NumberFormatException ex) {
                String msg = "The equivalent sample size has to be a real number.";
                JOptionPane.showMessageDialog(this, msg, "Invalid learning specification", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }
    
    private boolean bayesianEstimationSelected() {
        return this.comboBoxMethod.getSelectedIndex() == 1;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        textFieldEquivalentSampleSize = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        comboBoxMethod = new javax.swing.JComboBox();
        buttonLearn = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Answer a probabilistic query");

        jLabel2.setText("Equivalent sample size");

        textFieldEquivalentSampleSize.setText("1");
        textFieldEquivalentSampleSize.setEnabled(false);

        jLabel3.setText("Method");

        comboBoxMethod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Maximum likelihood", "Bayesian estimation" }));
        comboBoxMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxMethodActionPerformed(evt);
            }
        });

        buttonLearn.setText("Learn");
        buttonLearn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLearnActionPerformed(evt);
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
                        .addComponent(jLabel3)
                        .addGap(127, 127, 127)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(comboBoxMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textFieldEquivalentSampleSize, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonLearn, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(comboBoxMethod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(textFieldEquivalentSampleSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonLearn)
                    .addComponent(buttonCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void comboBoxMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxMethodActionPerformed
        this.textFieldEquivalentSampleSize.setEnabled(this.bayesianEstimationSelected());
    }//GEN-LAST:event_comboBoxMethodActionPerformed

    private void buttonLearnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLearnActionPerformed
        if(!this.verifyInputs())
            return;
        try {
            if(!this.bayesianEstimationSelected())
                this.bnLearnt = ParameterLearner.learnMLE(this.bnOriginal, this.dataset);
            else {
                double alpha = Double.parseDouble(this.textFieldEquivalentSampleSize.getText());
                this.bnLearnt = ParameterLearner.learnBayesianEstimationUniform(this.bnOriginal, this.dataset, alpha);
            }
            this.confirmed = true;
            this.dispose();
        }
        catch(BNLibInconsistentVariableSetsException ex) {
            String msg = "Current network and current dataset must contain exactly the same variables.";
            JOptionPane.showMessageDialog(this, msg, "Incompatible network and dataset", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_buttonLearnActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        this.confirmed = false;
        this.dispose();
    }//GEN-LAST:event_buttonCancelActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonLearn;
    private javax.swing.JComboBox comboBoxMethod;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField textFieldEquivalentSampleSize;
    // End of variables declaration//GEN-END:variables
}