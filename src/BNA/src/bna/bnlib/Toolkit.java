// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

import bna.bnlib.sampling.*;
import java.lang.reflect.Array;
import java.util.*;


/**
 * General purpose functions that are inconvinient to be placed elsewhere.
 */
public class Toolkit {
    /** Maximal difference of two double values that are considered equal. */
    public static final double DOUBLE_EPS = 1e-4;
    
    
    /** Are two real numbers considered equal (with some tolerance)? */
    public static boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) <= Toolkit.DOUBLE_EPS;
    }
    
    /**
     * Prints the headline and each item of given collection on one line using toString().
     */
    public static void dumpCollection(String headline, Iterable c) {
        System.out.println(headline + ":");
        for(Object item : c)
            System.out.println(" * " + item.toString());
    }
    
    public static <T> boolean unique(T[] array) {
        HashSet<T> set = new HashSet<T>();
        for(T obj : array)
            set.add(obj);
        return set.size() == array.length;
    }
    
    public static <T> boolean unique(Collection<T> collection) {
        HashSet<T> set = new HashSet<T>();
        set.addAll(collection);
        return set.size() == collection.size();
    }
    
    /** Determine number of all possible assignments to given variables. */
    public static int cardinality(Variable[] X) {
        int c = 1;
        for(Variable x : X)
            c *= x.getCardinality();
        return c;
    }
    
    /**
     * Check whether the given vector is a valid assignment to given variables.
     * For this to be true assignment has to have the same number of values
     * as there are variables and for each variable the assignment must be
     * within bounds for corresponding variable.
     */
    public static boolean validateAssignment(Variable[] vars, int[] assignment) {
        if(vars == null || assignment == null || vars.length != assignment.length)
            return false;
        for(int i = 0 ; i < vars.length ; i++) {
            if(vars[i].getCardinality() <= assignment[i] || assignment[i] < 0)
                return false;
        }
        return true;
    }
    
    /** Compute relative entropy between two networks with identical structure. */
    /*public static double networkDistanceRelativeEntropy(BayesianNetwork bnExact, BayesianNetwork bnApprox) {
        try {
            double relativeEntropy = 0;
            for(Node nodeExact : bnExact.getNodes()) {
                Node nodeApprox = bnApprox.getNode(nodeExact.getVariable());
                if(!Toolkit.areEqual(nodeExact.getParentVariables(), nodeApprox.getParentVariables()))
                    throw new BayesianNetworkRuntimeException("A variable differs in parents in the two networks.");
                VariableSubsetMapper exactToApproxMapper = new VariableSubsetMapper(nodeExact.getScope(), nodeApprox.getScope());
                // relative entropy decomposes over the nodes in a BN
                if(nodeExact.getParentCount() > 0) { // node with parents
                    Factor parentsJointProbabilityFactor = Toolkit.inferJointDistribution(bnExact, nodeExact.getParentVariables());
                    for(int[] parentsAssignment : parentsJointProbabilityFactor) {
                        double innerSumOverX = 0;
                        int[] exactParentsAssignment = parentsAssignment;
                        int[] exactAssignment = new int[1 + parentsAssignment.length],
                              approxAssignment = new int[1 + parentsAssignment.length];
                        System.arraycopy(exactParentsAssignment, 0, exactAssignment, 1, exactParentsAssignment.length);
                        for(int x = 0 ; x < nodeExact.getVariable().getCardinality() ; x++) {
                            exactAssignment[0] = x;
                            exactToApproxMapper.map(exactAssignment, approxAssignment);
                            double pXgivenP = nodeExact.getProbability(exactAssignment),
                                   qXgivenP = nodeApprox.getProbability(approxAssignment);
                            if(pXgivenP > 0 && qXgivenP > 0)
                                innerSumOverX += pXgivenP * Math.log(pXgivenP / qXgivenP);
                        }
                        double parentsProb = parentsJointProbabilityFactor.getProbability(parentsAssignment);
                        relativeEntropy += parentsProb * innerSumOverX;
                    }
                }
                else { // node without parents
                    double innerSumOverX = 0;
                    for(int x = 0 ; x < nodeExact.getVariable().getCardinality() ; x++) {
                        double pX = nodeExact.getProbability(new int[]{x}),
                               qX = nodeApprox.getProbability(new int[]{x});
                        if(pX > 0 && qX > 0)
                            innerSumOverX += pX * Math.log(pX / qX);
                    }
                    relativeEntropy += innerSumOverX;
                }
            }
            return relativeEntropy;
        }
        catch(BayesianNetworkRuntimeException bnrex) {
            throw new BayesianNetworkRuntimeException("The two networks aren't structurally identical.");
        }
        catch(BayesianNetworkException bnex) {
            throw new BayesianNetworkRuntimeException("Internal sampling error while computing relative entropy.");
        }
    }*/
    
    /** Compute relative entropy between two networks with identical structure. */
    public static double networkDistanceRelativeEntropy2(BayesianNetwork bnExact, BayesianNetwork bnApprox) {
        final double MINIMAL_Q_PROB = 1e-5;
        try {
            double relativeEntropy = 0;
            for(Node nodeExact : bnExact.getNodes()) {
                // check if the two nodes have the same parents
                Node nodeApprox = bnApprox.getNode(nodeExact.getVariable());
                if(!Toolkit.areEqual(nodeExact.getParentVariables(), nodeApprox.getParentVariables()))
                    throw new BayesianNetworkRuntimeException("A variable differs in parents in the two networks.");
                
                // relative entropy decomposes over the nodes in a BN
                if(nodeExact.getParentCount() > 0) { // node with parents
                    Variable scopeVar = nodeExact.getVariable();
                    Variable[] scopeParents = nodeExact.getParentVariables();
                    Variable[] scope = Toolkit.union(new Variable[]{scopeVar}, scopeParents); // ensure the indexing
                    VariableSubsetMapper scopeToExactMapper = new VariableSubsetMapper(scope, nodeExact.getScope()),
                                         scopeToApproxMapper = new VariableSubsetMapper(scope, nodeApprox.getScope());
                    int[] scopeAssignment = new int[scope.length],
                          exactAssignment = new int[scope.length],
                          approxAssignment = new int[scope.length];
                    Factor parentsJointProbabilityFactor = Toolkit.inferJointDistribution(bnExact, scopeParents);
                    for(int[] scopeParentsAssignment : parentsJointProbabilityFactor) { // sum over instantiations of parents
                        double sumOverX = 0;
                        System.arraycopy(scopeParentsAssignment, 0, scopeAssignment, 1, scope.length - 1);

                        for(int x = 0 ; x < nodeExact.getVariable().getCardinality() ; x++) {
                            scopeAssignment[0] = x;
                            scopeToExactMapper.map(scopeAssignment, exactAssignment);
                            scopeToApproxMapper.map(scopeAssignment, approxAssignment);
                            
                            double pXgivenP = nodeExact.getProbability(exactAssignment),
                                   qXgivenP = nodeApprox.getProbability(approxAssignment);
                            if(pXgivenP > 0) {
                                if(qXgivenP == 0)
                                    qXgivenP = MINIMAL_Q_PROB; // correction so that all values are accounted for
                                sumOverX += pXgivenP * Math.log(pXgivenP / qXgivenP);
                            }
                            else if(pXgivenP < 0 || qXgivenP < 0)
                                throw new BayesianNetworkRuntimeException("unexpected");
                        }
                        double pParents = parentsJointProbabilityFactor.getProbability(scopeParentsAssignment);
                        relativeEntropy += pParents * sumOverX;
                    }
                }
                else { // node without parents
                    double sumOverX = 0;
                    for(int[] assignment : nodeExact.getFactor()) {
                        double pX = nodeExact.getProbability(assignment),
                               qX = nodeApprox.getProbability(assignment);
                        if(pX > 0) {
                            if(qX == 0)
                                qX = MINIMAL_Q_PROB;
                            sumOverX += pX * Math.log(pX / qX);
                        }
                        else if(pX < 0 || qX < 0)
                            throw new BayesianNetworkRuntimeException("unexpected");
                    }
                    relativeEntropy += sumOverX;
                }
            }
            return relativeEntropy;
        }
        catch(BayesianNetworkRuntimeException bnrex) {
            bnrex.printStackTrace();
            throw new BayesianNetworkRuntimeException("Internal error while computing relative entropy.");
        }
        catch(BayesianNetworkException bnrex) {
            throw new BayesianNetworkRuntimeException("Internal error while computing relative entropy.");
        }
    }
    
    private static Factor inferJointDistribution(BayesianNetwork bn, Variable[] vars) throws BayesianNetworkException {
        final long SAMPLES_COUNT = 500 * 1000;
        final int THREAD_COUNT = 5;
        SampleProducer sampleProducer = new WeightedSampleProducer(bn, vars, new Variable[]{}, new Variable[]{}, new int[]{});
        QuerySamplerMultithreaded querySamplerMultithreaded = new QuerySamplerMultithreaded(sampleProducer, THREAD_COUNT);
        SamplingController samplingController = new SamplingController(SAMPLES_COUNT / THREAD_COUNT);
        querySamplerMultithreaded.sample(samplingController);
        return querySamplerMultithreaded.getSamplesCounterNormalized();
    }
    
    public static <T> boolean isSubset(T[] superset, T[] subset) {
        if(superset.length < subset.length)
            return false;
        for(Object o : subset) {
            if(!Toolkit.arrayContains(superset, o))
                return false;
        }
        return true;
    }
    
    public static <T> boolean areEqual(T[] set1, T[] set2) {
        return Toolkit.isSubset(set1, set2) && Toolkit.isSubset(set2, set1);
    }
    
    public static <T> boolean areDisjoint(T[] set1, T[] set2) {
        for(Object o : set1) {
            if(Toolkit.arrayContains(set2, o))
                return false;
        }
        return true;
    }
    
    public static <T> boolean areDisjoint(Collection<T> set1, Collection<T> set2) {
        for(Object o : set1) {
            if(set2.contains((T)o))
                return false;
        }
        return true;
    }
    
    /** From set2 appends all elements not in set1 to set1. */
    public static <T> T[] union(T[] set1, T[] set2) {
        if(set1.length == 0)
            return Arrays.copyOf(set2, set2.length);
        else if(set2.length == 0)
            return Arrays.copyOf(set1, set1.length);
        else {
            ArrayList<T> resultList = new ArrayList<T>();
            for(T o1 : set1)
                resultList.add(o1);
            for(T o2 : set2)
                if(!Toolkit.arrayContains(set1, o2))
                    resultList.add(o2);
            // overcome the "generic array creation" problem
            Class<?> componentType = set1[0].getClass();
            return Toolkit.collectionToGenericArray(resultList, componentType);
        }
    }
    
    /** From set1 removes all elements present in set2. */
    public static <T> T[] difference(T[] set1, T[] set2) {
        if(set1.length == 0 || set2.length == 0)
            return Arrays.copyOf(set1, set1.length);
        else {
            ArrayList<T> resultList = new ArrayList<T>();
            for(T o1 : set1) {
                if(!Toolkit.arrayContains(set2, o1))
                    resultList.add(o1);
            }
            // overcome the "generic array creation" problem
            Class<?> componentType = set1[0].getClass();
            return Toolkit.collectionToGenericArray(resultList, componentType);
        }
    }
    
    /** Check whether the given array contains specified object. */
    public static <T> boolean arrayContains(T[] array, T obj) {
        for(T arrayObj : array)
            if(arrayObj.equals(obj))
                return true;
        return false;
    }
    
    /**
     * Find index of obj in array or -1.
     * @param array
     * @param obj
     * @return Index of obj in array or -1.
     */
    public static <T> int indexOf(T[] array, T obj) {
        for(int i = 0 ; i < array.length ; i++)
            if(array[i].equals(obj))
                return i;
        return -1;
    }
    
    /**
     * Return a valid random index to array uniformDistribution according to
     * uniform distribution defined by array uniformDistribution containing
     * values for respective indices (higher value on index i means higher
     * probability of returning i). Sum of the uniformDistribution array is sum.
     * 
     * @param uniformDistribution Array whose values define probabilities of
     *                            returning respective indices.
     * @param sum Sum of all values in the uniformDistribution array (this
     *            parameter is needed only to speed up the computation).
     * @param rand Random generator to be used.
     * @return Valid random index to array uniformDistribution according
     *         to uniform distribution defined by uniformDistribution values.
     */
    public static int randomIndex(double[] uniformDistribution, double sum, Random rand) {
        boolean divideBySum = !Toolkit.doubleEquals(sum, 1.0);
        double rnd = rand.nextDouble();
        double probabilitiesScan = 0;
        for(int i = 0 ; i < uniformDistribution.length ; i++) {
            double probI = divideBySum ? uniformDistribution[i] / sum : uniformDistribution[i];
            probabilitiesScan += probI;
            if(rnd <= probabilitiesScan)
                return i;
        }
        if(Toolkit.doubleEquals(1.0, probabilitiesScan)) {
            // we are within the epsilon tolerance
            return uniformDistribution.length - 1;
        }
        // if the uniformDistribution are normalized, it shouldn't come to this
        throw new BayesianNetworkRuntimeException(String.format("Invalid probabilities sum %.3f.", probabilitiesScan));
    }
    
    /** Compute transitive closure of given relation. */
    public static boolean[][] transitiveClosure(boolean[][] relation) {
        boolean[][] closure = new boolean[relation.length][];
        for(int i = 0 ; i < relation.length ; i++)
            closure[i] = Arrays.copyOf(relation[i], relation[i].length);
        
        for(int k = 0 ; k < relation.length ; k++) {
            for(int i = 0 ; i < relation.length ; i++) {
                for(int j = 0 ; j < relation.length ; j++)
                    closure[i][j] = closure[i][j] || (closure[i][k] && closure[k][j]);
            }
        }
        
        return closure;
    }
    
    /** Method to overcome the "generic array creation" problem. */
    private static <T> T[] collectionToGenericArray(Collection<T> collection, Class<?> componentType) {
        T[] array = (T[])Array.newInstance(componentType, collection.size());
        int i = 0;
        for(T obj : collection)
            array[i++] = obj;
        return array;
    }
}
