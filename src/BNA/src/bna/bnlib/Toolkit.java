// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/02/06

package bna.bnlib;

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
            if(vars[i].getCardinality() <= assignment[i])
                return false;
        }
        return true;
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
        ArrayList<T> resultList = new ArrayList<T>();
        for(T o1 : set1)
            resultList.add(o1);
        for(T o2 : set2)
            if(!Toolkit.arrayContains(set1, o2))
                resultList.add(o2);
        Class<?> componentType;
        if(set1.length > 0)
            componentType = set1[0].getClass();
        else if(set2.length > 0)
            componentType = set2[0].getClass();
        else
            componentType = Object.class;
        // overcome the "generic array creation" problem
        T[] result = (T[])Array.newInstance(componentType, resultList.size());
        for(int i = 0 ; i < result.length ; i++)
            result[i] = resultList.get(i);
        return result;
    }
    
    /** From set1 removes all elements present in set2. */
    public static <T> T[] difference(T[] set1, T[] set2) {
        ArrayList<T> resultList = new ArrayList<T>();
        for(T o1 : set1) {
            if(!Toolkit.arrayContains(set2, o1))
                resultList.add(o1);
        }

        Class<?> componentType;
        if(set1.length > 0)
            componentType = set1[0].getClass();
        else if(set2.length > 0)
            componentType = set2[0].getClass();
        else
            componentType = Object.class;
        // overcome the "generic array creation" problem
        T[] result = (T[])Array.newInstance(componentType, resultList.size());
        for(int i = 0 ; i < result.length ; i++)
            result[i] = resultList.get(i);
        return result;
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
}
