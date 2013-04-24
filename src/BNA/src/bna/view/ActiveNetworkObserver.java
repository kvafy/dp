// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/24

package bna.view;


/**
 * Recieves notifications regarding the change of the current dataset.
 */
public interface ActiveNetworkObserver {
    public void notifyNewActiveNetwork(GBayesianNetwork gbn);
}
