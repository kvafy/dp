// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib.misc;

import bna.bnlib.*;
import bna.bnlib.sampling.QuerySamplerMultithreaded;
import bna.bnlib.sampling.SampleProducer;
import bna.bnlib.sampling.SamplingController;
import bna.bnlib.sampling.WeightedSampleProducer;
import java.awt.Point;
import java.lang.reflect.Array;
import java.util.*;


/** General purpose functions that are inconvinient to be placed elsewhere. */
public class Toolkit {
    /** Maximal difference of two double values that are considered equal. */
    public static final double DOUBLE_EPS = 1e-4;
    
    /** Are two real numbers considered equal (with given tolerance)? */
    public static boolean doubleEquals(double a, double b, double eps) {
        return Math.abs(a - b) <= eps;
    }
    
    /** Are two real numbers considered equal (with default tolerance)? */
    public static boolean doubleEquals(double a, double b) {
        return Toolkit.doubleEquals(a, b, Toolkit.DOUBLE_EPS);
    }
    
    /**
     * Prints the headline and each item of given collection on one line using toString().
     */
    public static void dumpCollection(String headline, Iterable c) {
        System.out.println(headline + ":");
        for(Object item : c)
            System.out.println(" * " + item.toString());
    }
    
    /** Are all elements in the array unique? (using their equals and hashCode methods) */
    public static <T> boolean unique(T[] array) {
        HashSet<T> set = new HashSet<T>();
        for(T obj : array)
            set.add(obj);
        return set.size() == array.length;
    }
    
    /** Are all elements in the collection unique? (using their equals and hashCode methods) */
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
    
    /**
     * Compute relative entropy (aka. KL-divergence) between two networks with identical structure.
     * @throws BNLibIllegalArgumentException When the two networks aren't structurally equal.
     */
    public static double networkDistanceRelativeEntropy(BayesianNetwork bnExact,
                                                         Map<Variable, Factor> distributionsOverParents,
                                                         BayesianNetwork bnApprox) 
                                                    throws BNLibIllegalArgumentException {
        final double MINIMAL_Q_PROB = 1e-6; // hard constant used when probability Q(x) = 0 && P(x) != 0
        double relativeEntropy = 0;
        for(Node nodeExact : bnExact.getNodes()) {
            // check if the two nodes have the same parents
            Node nodeApprox = bnApprox.getNode(nodeExact.getVariable());
            if(!bnExact.equalsStructurally(bnApprox))
                throw new BNLibIllegalArgumentException("A variable differs by its parents in the two networks.");

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
                //Factor parentsJointProbabilityFactor = Toolkit.inferJointDistribution(bnExact, scopeParents);
                Factor parentsJointProbabilityFactor = distributionsOverParents.get(nodeExact.getVariable());
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
                            throw new BNLibInternalException("unexpected probabilities");
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
                        throw new BNLibInternalException("unexpected probabilities");
                }
                relativeEntropy += sumOverX;
            }
        }
        return relativeEntropy;
    }
    
    /** Infer the joint probability distribution P(v1, ..., vn). */
    public static Factor inferJointDistribution(BayesianNetwork bn, Variable[] vars) {
        final long SAMPLES_COUNT = 100 * 1000 * 1000;
        final int THREAD_COUNT = 5;
        SampleProducer sampleProducer = new WeightedSampleProducer(bn, vars, new Variable[]{}, new Variable[]{}, new int[]{});
        QuerySamplerMultithreaded querySamplerMultithreaded = new QuerySamplerMultithreaded(sampleProducer, THREAD_COUNT);
        SamplingController samplingController = new SamplingController(SAMPLES_COUNT / THREAD_COUNT);
        querySamplerMultithreaded.sample(samplingController);
        return querySamplerMultithreaded.getSamplesCounterNormalized();
    }
    
    /** Are all elements of subset contained in the superset? (using equals method) */
    public static <T> boolean isSubset(T[] superset, T[] subset) {
        if(superset.length < subset.length)
            return false;
        for(Object o : subset) {
            if(!Toolkit.arrayContains(superset, o))
                return false;
        }
        return true;
    }
    
    /** Do the two arrays contain the same set of objects? (using equals method) */
    public static <T> boolean areEqual(T[] set1, T[] set2) {
        return Toolkit.isSubset(set1, set2) && Toolkit.isSubset(set2, set1);
    }
    
    /** Do the two arrays contain disjoint sets of objects? (using equals method) */
    public static <T> boolean areDisjoint(T[] set1, T[] set2) {
        for(Object o : set1) {
            if(Toolkit.arrayContains(set2, o))
                return false;
        }
        return true;
    }
    
    /** Do the two collections contain disjoint sets of objects? (using equals method) */
    public static <T> boolean areDisjoint(Collection<T> set1, Collection<T> set2) {
        for(Object o : set1) {
            if(set2.contains((T)o))
                return false;
        }
        return true;
    }
    
    /** From set2 appends all elements, that are not present in set1, to set1. (using equals method) */
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
    
    /** From set1 removes all elements present in set2. (using equals method) */
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
     * @throws BNLibIllegalArgumentException When the values in given distribution
     *         don't sum to one.
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
        throw new BNLibIllegalArgumentException(String.format("Invalid probabilities sum %.3f.", probabilitiesScan));
    }
    
    /** Compute transitive closure of given relation using Warshall's algorithm. */
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
    
    /**
     * Compute the angle (in radians) of vector determined by the specified points.
     * The resulting angle is from interval [0, 2pi)
     */
    public static double angleOfVector(Point begin, Point end) {
        double dx = end.x - begin.x,
               dy = end.y - begin.y;
        
        if(dx == 0)
            return (dy >= 0) ? Math.PI / 2 : 3.0 / 2.0 * Math.PI;
        else {
            if(dx > 0)
                return Math.atan(dy / dx);
            else
                return Math.PI + Math.atan(dy / dx);
        }
    }
    
    /** Takes two angles in the interval [0, 2pi) and returns their difference [0, pi). */
    public static double angleDiff(double a, double b) {
        if(a > b)
            return angleDiff(b, a); // ensure a <= b
        return Math.min(b - a, 2 * Math.PI - b + a);
    }
    
    /** Compute unique floating point numbers when considering epsilon tolerance. */
    public static int countPseudouniqueNumbers(Collection<Double> col) {
        if(col.isEmpty())
            return 0;
        List<Double> colSorted = new ArrayList<Double>();
        colSorted.addAll(col);
        Collections.sort(colSorted);
        int count = 1;
        for(int i = 1 ; i < colSorted.size() ; i++) {
            if(!Toolkit.doubleEquals(colSorted.get(i - 1), colSorted.get(i)))
                count++;
        }
        return count;
    }
    
    /** Joins all the strings to a single string, each two occurences separated by given separator. */
    public static String stringJoin(String[] strs, String separator) {
        if(strs == null)
            return null;
        if(separator == null)
            separator = "";
        StringBuilder sb = new StringBuilder();
        if(strs.length > 0)
            sb.append(strs[0]);
        for(int i = 1 ; i < strs.length ; i++) {
            sb.append(separator);
            sb.append(strs[i]);
        }
        return sb.toString();
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
