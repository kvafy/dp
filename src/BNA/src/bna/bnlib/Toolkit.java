/*
 * // Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
 * // Author:  David Chaloupka (xchalo09)
 * // Created: 2013/06/02
 */

package bna.bnlib;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 */
public class Toolkit {
    public static <T> boolean unique(T[] array) {
        HashSet<T> set = new HashSet<>();
        for(T obj : array)
            set.add(obj);
        return set.size() == array.length;
    }
    
    public static <T> boolean unique(Collection<T> collection) {
        HashSet<T> set = new HashSet<>();
        set.addAll(collection);
        return set.size() == collection.size();
    }
    
    public static int cardinality(Variable[] X) {
        int c = 1;
        for(Variable x : X)
            c *= x.getCardinality();
        return c;
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
    
    public static <T> T[] union(T[] set1, T[] set2) {
        ArrayList<T> resultList = new ArrayList<>();
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
        T[] result = (T[])Array.newInstance(componentType, resultList.size());
        for(int i = 0 ; i < result.length ; i++)
            result[i] = resultList.get(i);
        return result;
    }
    
    public static <T> boolean arrayContains(T[] array, T obj) {
        for(T arrayObj : array)
            if(arrayObj == obj)
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
            if(array[i] == obj)
                return i;
        return -1;
    }
}
