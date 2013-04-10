// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/10

package bna.bnlib.learning;

import bna.bnlib.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * Class implementing the BIC score.
 */
public class BICScoringMethod extends ScoringMethod {
    private DatasetInterface dataset;
    private BayesianNetwork bnTheCacheIsFor = null;
    private HashMap<AlterationAction, Double> mutualInformationChangeCache = new HashMap<AlterationAction, Double>();

    
    public BICScoringMethod(DatasetInterface dataset) {
        this.dataset = dataset;
    }
    
    @Override
    public double absoluteScore(BayesianNetwork bn) {
        double N = this.dataset.getSize();
        // the likelihood-score part
        double likelihoodScore = 0;
        for(Node node : bn.getNodes())
            likelihoodScore += this.getMutualInformationFromDataset(node);
        likelihoodScore *= N;
        // the dimension part
        double dim = bn.getNetworkDimension();
        double structurePenalization = Math.log(N) / 2 * dim;
        return likelihoodScore - structurePenalization;
    }
    
    @Override
    public double deltaScore(BayesianNetwork bn, AlterationAction action) throws BNLibIllegalStructuralModificationException {
        double mutualInformationChange = this.getMutualInformationTermChange(bn, action);
        double dimensionTermChange = this.getDimensionTermChange(bn, action);
        return mutualInformationChange - dimensionTermChange;
    }
    
    @Override
    /**
     * Cached values of mutual information changes for some AlterationAction have to be invalidated.
     * If we make add or remove (X,Y) action, then parents of Y change and hence
     * any cached value that was computed using mutual information I(Y, Pa(Y))
     * is no longer valid. If we make a reverse (X,Y) action, then both parents
     * of X and of Y change, hence any cached value that was computed using I(X, Pa(X))
     * or I(Y, Pa(Y)) is no longer valid.
     * Cached value of add/remove (X,Y) is invalid if Parents(Y) changed.
     * Cached value of reverse (X,Y) is invalid if Parents(X) or Parents(Y) changed.
     */
    public void notifyNetworkAlteration(AlterationAction actionTaken) {
        // by taking the given action, whose parents have changed?
        ArrayList<Variable> variablesWithNewParents = new ArrayList<Variable>();
        if(actionTaken instanceof AlterationActionAddEdge || actionTaken instanceof AlterationActionRemoveEdge) {
            variablesWithNewParents.add(actionTaken.getChildVariable());
        }
        else if(actionTaken instanceof AlterationActionReverseEdge) {
            variablesWithNewParents.add(actionTaken.getChildVariable());
            variablesWithNewParents.add(actionTaken.getParentVariable());
        }
        else
            throw new BayesianNetworkRuntimeException("Unknown action type.");
        
        // determine what cached mutual information changes are invalidated by the action
        Set<AlterationAction> cachedActions = this.mutualInformationChangeCache.keySet();
        Iterator<AlterationAction> cachedActionsIterator = cachedActions.iterator();
        while(cachedActionsIterator.hasNext()) {
            // we can use the iterator's "remove" method to remove mapping from the map
            AlterationAction cachedAction = cachedActionsIterator.next();
            if(cachedAction instanceof AlterationActionAddEdge || cachedAction instanceof AlterationActionRemoveEdge) {
                if(variablesWithNewParents.contains(cachedAction.getChildVariable()))
                    cachedActionsIterator.remove();
            }
            else if(cachedAction instanceof AlterationActionReverseEdge) {
                if(variablesWithNewParents.contains(cachedAction.getChildVariable())
                        || variablesWithNewParents.contains(cachedAction.getParentVariable()))
                    cachedActionsIterator.remove();
            }
            else
                throw new BayesianNetworkRuntimeException("Unknown action type.");
        }
    }
    
    /** In the BIC formula, how does the mutual information term change? */
    private double getMutualInformationTermChange(BayesianNetwork bn, AlterationAction action) throws BNLibIllegalStructuralModificationException {
        // try cache lookup
        if(this.bnTheCacheIsFor == bn) {
            Double cachedMutinfChange = this.mutualInformationChangeCache.get(action);
            if(cachedMutinfChange != null)
                return cachedMutinfChange;
        }
        else {
            // throw cache away
            this.bnTheCacheIsFor = bn;
            this.mutualInformationChangeCache.clear();
        }
        
        // value wasn't in cache
        double N = this.dataset.getSize();
        double mutinfOld, mutinfNew;
        if(action instanceof AlterationActionAddEdge || action instanceof AlterationActionRemoveEdge) {
            Variable childVar = action.getChildVariable();
            Node childNode = bn.getNode(childVar);
            mutinfOld = this.getMutualInformationFromDataset(childNode);
            action.apply(bn);
            mutinfNew = this.getMutualInformationFromDataset(childNode);
            action.undo(bn);
        }
        else if(action instanceof AlterationActionReverseEdge) {
            Variable childVar = action.getChildVariable(),
                     parentVar = action.getParentVariable();
            Node childNode = bn.getNode(childVar),
                 parentNode = bn.getNode(parentVar);
            mutinfOld = this.getMutualInformationFromDataset(childNode) + this.getMutualInformationFromDataset(parentNode);
            action.apply(bn);
            mutinfNew = this.getMutualInformationFromDataset(childNode) + this.getMutualInformationFromDataset(parentNode);
            action.undo(bn);
        }
        else
            throw new BayesianNetworkRuntimeException("Unknown action type.");
        
        Double mutinfTermChange = N * (mutinfNew - mutinfOld);
        this.mutualInformationChangeCache.put(action, mutinfTermChange);
        return mutinfTermChange;
    }
    
    /** In the BIC formula, how does the dimension term change? */
    private double getDimensionTermChange(BayesianNetwork bn, AlterationAction action) throws BNLibIllegalStructuralModificationException {
        double N = this.dataset.getSize();
        double dimCurrent = bn.getNetworkDimension();
        action.apply(bn);
        double dimNew = bn.getNetworkDimension();
        action.undo(bn);
        return Math.log(N) / 2 * (dimNew - dimCurrent);
    }
    
    /**
     * Compute the mutual information between node and its parents from the dataset.
     * Cache is not used at all.
     */
    private double getMutualInformationFromDataset(Node n) {
        if(n.getParentCount() == 0) // TODO really works like this?
            return 0.0;
        Variable[] nSet = {n.getVariable()};
        Variable[] parentsSet = n.getParentVariables();
        return dataset.mutualInformation(nSet, parentsSet);
    }
}
