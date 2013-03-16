// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/14

package bna.bnlib.learning;

import bna.bnlib.Factor;
import bna.bnlib.Variable;
import bna.bnlib.misc.LRUCache;
import java.util.Arrays;


/**
 * Provides caching of mutual information queries over given dataset.
 * Please not that if the underlying dataset is modified, the cached values
 * are no longer valid.
 */
public class CachedDataset implements DatasetInterface {
    private Dataset dataset;
    private LRUCache<TwoSetsOfVariables, Double> mutualInformationCache;
    
    
    public CachedDataset(Dataset dataset, int cacheCapacity) {
        this.dataset = dataset;
        this.mutualInformationCache = new LRUCache<TwoSetsOfVariables, Double>(cacheCapacity);
    }

    @Override
    public Variable[] getVariables() {
        return this.dataset.getVariables();
    }

    @Override
    public int getSize() {
        return this.dataset.getSize();
    }

    @Override
    public void addRecord(int[] record) {
        throw new UnsupportedOperationException("Record addition is not supported for a cached dataset.");
    }

    @Override
    public Factor computeFactor(Variable[] scope) {
        return this.dataset.computeFactor(scope);
    }

    @Override
    public double mutualInformation(Variable[] set1, Variable[] set2) {
        TwoSetsOfVariables infObject = new TwoSetsOfVariables(set1, set2);
        Double infValue = this.mutualInformationCache.get(infObject);
        if(infValue == null) {
            infValue = this.dataset.mutualInformation(set1, set2);
            this.mutualInformationCache.put(infObject, infValue);
        }
        return infValue;
    }
    
    
    /** We cache values of mutual information for 2-tuples of variable sets ({X}, Parents(X)). */
    class TwoSetsOfVariables {
        Variable[] set1, set2;
        public TwoSetsOfVariables(Variable[] set1, Variable[] set2) {
            this.set1 = set1;
            this.set2 = set2;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + Arrays.deepHashCode(this.set1);
            hash = 37 * hash + Arrays.deepHashCode(this.set2);
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if(!(o instanceof TwoSetsOfVariables))
                return false;
            TwoSetsOfVariables mo = (TwoSetsOfVariables)o;
            return Arrays.equals(this.set1, mo.set1) && Arrays.equals(this.set2, mo.set2);
        }
    }
}
