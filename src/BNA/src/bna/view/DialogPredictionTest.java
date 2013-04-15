// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/14

package bna.view;

import bna.bnlib.BayesianNetwork;
import bna.bnlib.Factor;
import bna.bnlib.Variable;
import bna.bnlib.learning.Dataset;
import bna.bnlib.misc.Toolkit;
import bna.bnlib.sampling.QuerySampler;
import bna.bnlib.sampling.SampleProducer;
import bna.bnlib.sampling.SamplingController;
import bna.bnlib.sampling.WeightedSampleProducer;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


/**
 * Dialog takes current network, dataset and attempts to predict a single selected attribute.
 */
public class DialogPredictionTest extends javax.swing.JDialog {
    /** How many samples are generated used in stochastic inference to make a prediction. */
    public static final long SAMPLES_PER_PREDICTION = 1000;
    
    private BayesianNetwork bn;
    private Dataset dataset;

    
    public DialogPredictionTest(java.awt.Frame parent, boolean modal,
                                BayesianNetwork bn, Dataset dataset) {
        super(parent, modal);
        this.bn = bn;
        this.dataset = dataset;
        initComponents();
        this.loadConfiguration();
        this.setLocationRelativeTo(this.getParent());
    }
    
    private void loadConfiguration() {
        MainWindow mw = MainWindow.getInstance();
        try {
            String targetAttrIndexStr = mw.getConfiguration("Prediction", "target_attr_index");
            int targetAttrIndex = Integer.valueOf(targetAttrIndexStr);
            this.comboBoxTargetAttribute.setSelectedIndex(targetAttrIndex);
        }
        catch(NumberFormatException ex) {}
        catch(IllegalArgumentException ex) {}
    }
    
    private void saveConfiguration() {
        MainWindow mw = MainWindow.getInstance();
        int targetAttrIndex = this.comboBoxTargetAttribute.getSelectedIndex();
        mw.setConfiguration("Prediction", "target_attr_index", String.valueOf(targetAttrIndex));
    }

    private boolean verifyInputs() {
        int targetAttrIndex = this.comboBoxTargetAttribute.getSelectedIndex();
        if(targetAttrIndex < 0 || targetAttrIndex >= this.bn.getVariablesCount()) {
            String msg = "A target attribute has to be selected.";
            JOptionPane.showMessageDialog(this, msg, "Incomplete inputs", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void notifyTestingStarted() {
        this.comboBoxTargetAttribute.setEnabled(false);
        this.buttonTest.setEnabled(false);
        this.progressBar.setValue(0);
        this.setConfusionMatrix(null, null);
    }
    
    private void notifyTestingProgress(double progress) {
        this.progressBar.setValue((int)(100 * progress));
    }
    
    private void notifyTestingFinished(Variable predictedVar, int[][] confusionMatrix) {
        this.comboBoxTargetAttribute.setEnabled(true);
        this.buttonTest.setEnabled(true);
        this.progressBar.setValue(100);
        this.setConfusionMatrix(predictedVar, confusionMatrix);
    }
    
    private void setConfusionMatrix(Variable predictedVar, int[][] matrix) {
        if(predictedVar == null || matrix == null) {
            this.tableConfusionMatrix.setModel(new DefaultTableModel());
            return;
        }
        String[] assignments = predictedVar.getValues();
        // generate header
        String[] header = new String[1 + assignments.length + 1];
        header[0] = "predicted / real";
        Object[][] data = new Object[assignments.length + 1][1 + assignments.length + 1];
        for(int i = 0 ; i < assignments.length ; i++)
            header[i + 1] = assignments[i];
        header[1 + assignments.length] = "class precision";
        // fill in confusion counts
        for(int i = 0 ; i < assignments.length ; i++) {
            data[i][0] = assignments[i];
            for(int j = 0 ; j < assignments.length ; j++)
                data[i][j + 1] = new Integer(matrix[i][j]);
        }
        // fill in precision
        for(int i = 0 ; i < assignments.length ; i++) {
            int truePositives = 0,
                falsePositives = 0;
            for(int j = 0 ; j < assignments.length ; j++) {
                if(i == j)
                    truePositives += matrix[i][j];
                else
                    falsePositives += matrix[i][j];
            }
            double precision = ((double)truePositives) / (truePositives + falsePositives);
            data[i][1 + assignments.length] = String.format("%.3f", precision);
        }
        // fill in recall
        data[assignments.length][0] = "class recall";
        for(int i = 0 ; i < assignments.length ; i++) {
            int truePositives = 0,
                falseNegatives = 0;
            for(int j = 0 ; j < assignments.length ; j++) {
                if(i == j)
                    truePositives += matrix[j][i];
                else
                    falseNegatives += matrix[j][i];
            }
            double recall = ((double)truePositives) / (truePositives + falseNegatives);
            data[assignments.length][i + 1] = String.format("%.3f", recall);
        }
        // fill in accuracy
        int predictOK = 0,
            predictFail = 0;
        for(int i = 0 ; i < assignments.length ; i++) {
            for(int j = 0 ; j < assignments.length ; j++) {
                if(i == j)
                    predictOK += matrix[i][j];
                else
                    predictFail += matrix[i][j];
            }
        }
        double accuracy = ((double)predictOK) / (predictOK + predictFail);
        data[assignments.length][1 + assignments.length] = String.format("accuracy = %.3f", accuracy);
        
        this.tableConfusionMatrix.setModel(new DefaultTableModel(data, header));
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
        buttonTest = new javax.swing.JButton();
        comboBoxTargetAttribute = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableConfusionMatrix = new javax.swing.JTable();
        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Prediction accuracy test");

        labelPath.setText("Target attribute");

        buttonTest.setText("Test");
        buttonTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTestActionPerformed(evt);
            }
        });

        Variable[] variables = this.bn.getVariables();
        String[] variableNames = new String[variables.length];
        for(int i = 0 ; i < variables.length ; i++)
        variableNames[i] = variables[i].getName();
        comboBoxTargetAttribute.setModel(new javax.swing.DefaultComboBoxModel(variableNames));

        jLabel1.setText("Confusion matrix");

        tableConfusionMatrix.setModel(new javax.swing.table.DefaultTableModel());
        jScrollPane1.setViewportView(tableConfusionMatrix);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelPath)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxTargetAttribute, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(buttonTest, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPath)
                    .addComponent(comboBoxTargetAttribute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addGap(8, 8, 8)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonTest)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTestActionPerformed
        if(!this.verifyInputs())
            return;
        
        PredictionTestingThread worker = new PredictionTestingThread(this.bn, this.dataset);
        worker.setDaemon(true);
        worker.start();
        
        this.saveConfiguration();
    }//GEN-LAST:event_buttonTestActionPerformed

    /** Thread that carries out the prediction testing and notifies GUI. */
    class PredictionTestingThread extends Thread {
        private BayesianNetwork bn;
        private Dataset dataset;
        
        
        public PredictionTestingThread(BayesianNetwork bn, Dataset dataset) {
            this.bn = bn;
            this.dataset = dataset;
        }
        
        @Override
        public void run() {
            Variable[] bnVars = this.bn.getVariables();
            Variable[] datasetVars = this.dataset.getVariables();
            int targetAttrIndex = comboBoxTargetAttribute.getSelectedIndex();
            Variable targetVar = bnVars[targetAttrIndex];
            // need to have instance of the target variable from dataset (values may have other ordering)
            int targetVarIndexInDataset = Toolkit.indexOf(datasetVars, targetVar);
            targetVar = datasetVars[targetVarIndexInDataset];

            int[][] confusionMatrix = new int[targetVar.getCardinality()][targetVar.getCardinality()];

            notifyTestingStarted();
            int i = 0;
            for(int[] sample : this.dataset.getDataReadOnly()) {
                // assemble a query in which everything except targetVariable is evidence, ie. P(Target | E = e)
                String query = this.getQueryString(targetVar, datasetVars, sample);
                // make a prediction via sampling
                Factor predictionFactor = this.getPredictionFactor(query);
                // evaluate prediction result for this sample
                int realValue = sample[targetVarIndexInDataset];
                String predictedValueStr = this.getPredictedValue(predictionFactor);
                int predictedValue = targetVar.getValueIndex(predictedValueStr);
                confusionMatrix[predictedValue][realValue]++;
                notifyTestingProgress(++i / (double)this.dataset.getSize());
            }
            
            notifyTestingFinished(targetVar, confusionMatrix);
        }
        
        private String getQueryString(Variable targetVar, Variable[] datasetVars, int[] datasetSample) {
            StringBuilder query = new StringBuilder();
            query.append("P(")
                    .append(targetVar.getName())
                    .append(" | ");
            boolean firstEvidence = true;
            for(int i = 0 ; i < datasetVars.length ; i++) {
                if(datasetVars[i].equals(targetVar))
                    continue;
                if(!firstEvidence)
                    query.append(", ");
                firstEvidence = false;
                String datasetVarValue = datasetVars[i].getValues()[datasetSample[i]];
                query.append(datasetVars[i].getName())
                        .append(" = ")
                        .append(datasetVarValue);
            }
            query.append(")");
            return query.toString();
        }

        private Factor getPredictionFactor(String query) {
            SampleProducer sampleProducer = new WeightedSampleProducer(this.bn, query);
            QuerySampler sampler = new QuerySampler(sampleProducer);
            SamplingController samplingController = new SamplingController(DialogPredictionTest.SAMPLES_PER_PREDICTION);
            sampler.sample(samplingController);
            return sampler.getSamplesCounter(); // no need to normalize
        }

        /** According to prediction policy and to inferred probabilities pick a final predicted value. */
        private String getPredictedValue(Factor predictionFactor) {
            int maxProbIndex = 0;
            double maxProb = predictionFactor.getProbability(maxProbIndex);
            for(int i = 1 ; i < predictionFactor.getCardinality() ; i++) {
                double iProb = predictionFactor.getProbability(i);
                if(iProb > maxProb) {
                    maxProb = iProb;
                    maxProbIndex = i;
                }
            }
            return predictionFactor.getScope()[0].getValues()[maxProbIndex];
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonTest;
    private javax.swing.JComboBox comboBoxTargetAttribute;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelPath;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTable tableConfusionMatrix;
    // End of variables declaration//GEN-END:variables
}
