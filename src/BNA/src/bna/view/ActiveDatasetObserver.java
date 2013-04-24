// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/24

package bna.view;

import bna.bnlib.learning.Dataset;


/**
 * Recieves notifications regarding the change of the current dataset.
 */
public interface ActiveDatasetObserver {
    public void notifyNewActiveDataset(Dataset d);
}
