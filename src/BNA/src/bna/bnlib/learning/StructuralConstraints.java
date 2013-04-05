// Project: Bayesian networks applications (Master's thesis), BUT FIT 2013
// Author:  David Chaloupka (xchalo09)
// Created: 2013/03/29

package bna.bnlib.learning;

import bna.bnlib.Variable;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Specifies the constraints pruning the space of feasible network structures.
 * Default configuration is "no constraints".
 */
public class StructuralConstraints {
    // selective connectivity (defined for each pair of variables separately)
    HashMap<Variable, Integer> variable2IndexMapping; // mapping of variable to index in connectivity matrix
    private boolean[][] connectivityMatrix;
    // agregable constraints
    private int maxParentCount;
    
    
    public StructuralConstraints(Variable[] variables) {
        // constraints by selective connectivity (all enabled except for autoconnections)
        this.connectivityMatrix = new boolean[variables.length][];
        for(int i = 0 ; i < variables.length ; i++) {
            this.connectivityMatrix[i] = new boolean[variables.length];
            Arrays.fill(this.connectivityMatrix[i], true);
            this.connectivityMatrix[i][i] = false;
        }
        this.variable2IndexMapping = new HashMap<Variable, Integer>();
        for(int i = 0 ; i < variables.length ; i++)
            this.variable2IndexMapping.put(variables[i], new Integer(i));
        // argegable constraints applied as a whole
        this.maxParentCount = Integer.MAX_VALUE;
    }
    
    public void setMaxParentCount(int count) {
        this.maxParentCount = count;
    }
    
    public boolean isOKParentsCount(int parentsCount) {
        return parentsCount <= this.maxParentCount;
    }
    
    public void setConnectionAllowed(Variable from, Variable to, boolean allowed) {
        int fromIndex = this.variable2IndexMapping.get(from),
            toIndex = this.variable2IndexMapping.get(to);
        this.connectivityMatrix[fromIndex][toIndex] = allowed;
    }
    
    public void disallowBeParent(Variable var) {
        int variableCount = this.variable2IndexMapping.size();
        int varIndex = this.variable2IndexMapping.get(var);
        for(int i = 0 ; i < variableCount ; i++)
            this.connectivityMatrix[varIndex][i] = false;
    }
    
    public void disallowBeChild(Variable var) {
        int variableCount = this.variable2IndexMapping.size();
        int varIndex = this.variable2IndexMapping.get(var);
        for(int i = 0 ; i < variableCount ; i++)
            this.connectivityMatrix[i][varIndex] = false;
    }
    
    public boolean isConnectionAllowed(Variable from, Variable to) {
        Integer fromIndex = this.variable2IndexMapping.get(from),
                toIndex = this.variable2IndexMapping.get(to);
        return this.connectivityMatrix[fromIndex][toIndex];
    }
}
