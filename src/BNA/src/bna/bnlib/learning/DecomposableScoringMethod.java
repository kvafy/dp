// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/04/11

package bna.bnlib.learning;

import bna.bnlib.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * A general class for scoring methods decomposable by structure to a sum of family
 * scores plus a penalty term for complexity.
 * <p>
 * The scoring method works with a notion of "current network" in order to use
 * an efficient caching scheme of delta family scores. Therefore the optimization
 * algorithm needs to notify the scoring method about the new network structure
 * for the next step of local search so that right cache entries can be
 * invalidated and the cache doesn't grow too large.
 * <p>
 * This class implements the logic of caching delta family scores and proper
 * invalidation of delta family score based on the currently accepted network
 * alteration.
 * A subclass needs to define the following methods:
 * <ul>
 *  <li> computeFamilyScore(...)
 *  <li> computeComplexityPenalty(...)
 * </ul>
 */
public abstract class DecomposableScoringMethod extends ScoringMethod {
    protected DatasetInterface dataset;
    private BayesianNetwork bnTheCacheIsFor = null;
    // cache for storing the change increse of family score for given alteration
    // !!! this isn't the delta score, delta score is given by change of family
    //     scores and by change complexity penalization
    private HashMap<AlterationAction, Double> deltaFamilyScoreCache = new HashMap<AlterationAction, Double>();

    
    /**
     * Create a generic scoring object that computes the scores based on supplied dataset.
     * As this class keeps cache of delta-family scores the dataset is not
     * supposed to change because no such change is intercepted and isn't
     * reflected.
     */
    public DecomposableScoringMethod(DatasetInterface dataset) {
        this.dataset = dataset;
    }
    
    @Override
    public double absoluteScore(BayesianNetwork bn) {
        // the likelihood-score part
        double likelihoodScore = 0;
        for(Node node : bn.getNodes())
            likelihoodScore += this.computeFamilyScore(node);
        // the dimension part
        double structurePenalization = this.computeComplexityPenalty(bn);
        return likelihoodScore + structurePenalization;
    }
    
    /**
     * Compute the score increase when the given action is applied on the supplied network.
     * @throws BNLibIllegalStructuralModificationException When the alteration
     *         cannot be applied to given network.
     */
    public final double deltaScore(BayesianNetwork bn, AlterationAction action) throws BNLibIllegalStructuralModificationException {
        Double deltaFamilyScore = this.deltaFamilyScorecacheLookup(bn, action);
        if(deltaFamilyScore == null) {
            deltaFamilyScore = this.computeDeltaFamilyScore(bn, action);
            this.deltaFamilyScorecachePut(bn, action, deltaFamilyScore);
        }
        double dimensionPenaltyChange = this.computeIncreaseOfComplexityPenalty(bn, action);
        return deltaFamilyScore + dimensionPenaltyChange;
    }
    
    private Double deltaFamilyScorecacheLookup(BayesianNetwork bn, AlterationAction action) {
        if(this.bnTheCacheIsFor != bn)
            return null;
        return this.deltaFamilyScoreCache.get(action);
    }
    
    private void deltaFamilyScorecachePut(BayesianNetwork bn, AlterationAction action, Double value) {
        if(this.bnTheCacheIsFor != bn) {
            this.deltaFamilyScoreCache.clear();
            this.bnTheCacheIsFor = bn;
        }
        this.deltaFamilyScoreCache.put(action, value);
    }
    
    /**
     * Determines the delta family score if given action is applied.
     * @throws BNLibIllegalStructuralModificationException If the structural modification
     *         is invalid.
     * @throws BNLibIllegalArgumentException If the action is unknown.
     */
    private double computeDeltaFamilyScore(BayesianNetwork bn, AlterationAction action) throws BNLibIllegalStructuralModificationException, BNLibIllegalArgumentException {
        double scoreOld, scoreNew;
        if(action instanceof AlterationActionAddEdge || action instanceof AlterationActionRemoveEdge) {
            Variable childVar = action.getChildVariable();
            Node childNode = bn.getNode(childVar);
            scoreOld = this.computeFamilyScore(childNode);
            action.apply(bn);
            scoreNew = this.computeFamilyScore(childNode);
            action.undo(bn);
        }
        else if(action instanceof AlterationActionReverseEdge) {
            Variable childVar = action.getChildVariable(),
                     parentVar = action.getParentVariable();
            Node childNode = bn.getNode(childVar),
                 parentNode = bn.getNode(parentVar);
            scoreOld = this.computeFamilyScore(childNode) + this.computeFamilyScore(parentNode);
            action.apply(bn);
            scoreNew = this.computeFamilyScore(childNode) + this.computeFamilyScore(parentNode);
            action.undo(bn);
        }
        else
            throw new BNLibIllegalArgumentException("Unknown action type.");
        
        return scoreNew - scoreOld;
    }
    
    /** In the BIC formula, how does the dimension term change? */
    private double computeIncreaseOfComplexityPenalty(BayesianNetwork bn, AlterationAction action) throws BNLibIllegalStructuralModificationException {
        double penaltyOld = this.computeComplexityPenalty(bn);
        action.apply(bn);
        double penaltyNew = this.computeComplexityPenalty(bn);
        action.undo(bn);
        return penaltyNew - penaltyOld;
    }
    
    /**
     * Cached values of delta family score for some AlterationAction have to be invalidated.
     * If we make add or remove (X,Y) action, then parents of Y change and hence
     * any cached value of delta family score that was computed using Parents(Y)
     * is no longer valid. If we make a reverse (X,Y) action, then both parents
     * of X and of Y change, hence any cached delta family score value that was
     * computed using Parents(X) or Parents(Y) is no longer valid.
     * Cached value of add/remove (X,Y) is invalid if Parents(Y) changed.
     * Cached value of reverse (X,Y) is invalid if Parents(X) or Parents(Y) changed.
     * @throws BNLibIllegalArgumentException When the action is of unknown type.
     */
    public final void notifyNetworkAlteration(AlterationAction actionTaken) {
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
            throw new BNLibIllegalArgumentException("Unknown action type.");
        
        // determine what cached mutual information changes are invalidated by the action
        Set<AlterationAction> cachedActions = this.deltaFamilyScoreCache.keySet();
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
                throw new BNLibIllegalArgumentException("Unknown action type.");
        }
    }
    
    
    // methods implemented by children (BIC of Bayesian score)
    
    protected abstract double computeFamilyScore(Node x);
    
    protected abstract double computeComplexityPenalty(BayesianNetwork bn);
}
