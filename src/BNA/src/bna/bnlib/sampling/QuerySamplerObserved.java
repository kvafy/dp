// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/24

package bna.bnlib.sampling;

import java.util.ArrayList;


/**
 * A decorator of QuerySampler that allows to register observers that get
 * notified with each generater sample via the QuerySamplerObserver#notifySample.
 */
public class QuerySamplerObserved extends QuerySampler {
    private ArrayList<QuerySamplerObserver> observers = new ArrayList<QuerySamplerObserver>();
    
    
    public QuerySamplerObserved(SampleProducer sampleProducer) {
        super(sampleProducer);
    }
    
    private void notifyNewSample() {
        for(QuerySamplerObserver observer : this.observers)
            observer.notifySample();
    }
    
    /** Record a sampleNumber with given weight in our statistics. */
    @Override
    protected void registerSample(int[] XYVarsValues, double sampleWeight) {
        super.registerSample(XYVarsValues, sampleWeight);
        this.notifyNewSample();
    }
    
    public void registerObserver(QuerySamplerObserver observer) {
        this.observers.add(observer);
    }
    
    public void deregisterObserver(QuerySamplerObserver observer) {
        this.observers.remove(observer);
    }
}
