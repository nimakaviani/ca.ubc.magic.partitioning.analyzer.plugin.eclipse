/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.magic.profiler.partitioning.control.alg.simplex;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math.optimization.linear.Relationship;

/**
 *
 * @author nima
 */
public class SimplexModelExtended4Cost extends SimplexModelExtended{
    
     public SimplexModelExtended4Cost(int size) {
         super(size);
     }
     
    @Override
    public LinearObjectiveFunction getObjectiveFunction(){
        int variablesSize = size + edgeList.size();
        double[] mergedWeight = new double[variablesSize];
        double   constantTerm = 0.0;
        for (int i = 0; i < size; i++) {
            mergedWeight[i] = nodeWeight[i][0] - nodeWeight[i][1]; 
            constantTerm += nodeWeight[i][1];
        }
       for (int i = size; i < variablesSize; i++) {
           int index = (i - size);
           Edge e = edgeList.get(index);
           mergedWeight[i]   = adjacencyMatrix[e.targetId][e.sourceId];
       }
       printArray("Cloud Merged weights: ", mergedWeight);
       return new LinearObjectiveFunction(mergedWeight, constantTerm);
    }        
    
    public Collection<LinearConstraintEx> getConstraintsEx(){
        ArrayList<LinearConstraintEx> constraints = new ArrayList<LinearConstraintEx>();
        int indexer = 0, tmpIndexer = 0, sourceIndex = 0, targetIndex = 0;
        int variablesSize = size + edgeList.size();
        
        // the loop initializes the coefficients of the paired constraints where
        // it is required for two modules to be placed together. 
       
        for (String[] pair : pairSet){
            int p1Index = nodeIndexMap.indexOf(pair[0]);
            int p2Index = nodeIndexMap.indexOf(pair[1]);
            double[] coefficients = new double[2];      
            coefficients[0] = 1;
            coefficients[1] = -1;
            int[] cols = new int[] {p1Index + 1, p2Index + 1};
            LinearConstraintEx constraint = new LinearConstraintEx(2, coefficients, cols, Relationship.EQ, 0);
            constraints.add(constraint);
        }
        
        // the loop initializes the coefficients for the real variables
        for (int i = 0 ; i < size; i++){
            List<Double> values = new LinkedList<Double>();
            List<Integer> cols = new LinkedList<Integer>();
            for (int j = 0; j < variablesSize; j++){
                if (indexer == j) {
                    values.add(1.0);
                    cols.add(j + 1);
                }
            }
            
            String nodeId = nodeIndexMap.get(indexer);
            if (sourceSet.contains(nodeId))
                constraints.add(new LinearConstraintEx(values.size(), toArrayDouble(values), toArrayInt(cols), Relationship.EQ, 1));
            else if (targetSet.contains(nodeId))
                constraints.add(new LinearConstraintEx(values.size(), toArrayDouble(values), toArrayInt(cols), Relationship.EQ, 0));
            else{                    
                constraints.add(new LinearConstraintEx(values.size(), toArrayDouble(values), toArrayInt(cols), Relationship.GEQ, 0));
                constraints.add(new LinearConstraintEx(values.size(), toArrayDouble(values), toArrayInt(cols), Relationship.LEQ, 1));
            }
            indexer++;
        }
        
        // the loop initializes the coefficients for the guard values ensuring
        // generation of differential weights for the model whose values will
        // not be negative to alter the results of evaluations.
        for (int i = size; i < variablesSize; i++){
        	List<Double> values = new LinkedList<Double>();
        	List<Integer> cols = new LinkedList<Integer>();
        	tmpIndexer = (indexer - size);
        	sourceIndex = edgeList.get(tmpIndexer).sourceId;
        	targetIndex = edgeList.get(tmpIndexer).targetId;
            for (int j = 0; j < variablesSize; j++) {
                    if (j == sourceIndex || j == indexer) {
                        values.add(1.0);
                        cols.add(j + 1);
                    }
                    else if (j == targetIndex) {
                    	  values.add(-1.0);
                          cols.add(j + 1);
                    }
            }
         
            constraints.add(new LinearConstraintEx(values.size(), toArrayDouble(values), toArrayInt(cols), Relationship.GEQ, 0));
            constraints.add(new LinearConstraintEx(values.size(), toArrayDouble(values), toArrayInt(cols), Relationship.LEQ, 1));
            indexer++;
        }                
        return constraints;
    }
    
    public double[] toArrayDouble(List<Double> list) {
    	double[] result = new double[list.size()];
    	int i = 0;
    	for(Double d : list) {
    		result[i] = d;
    		i++;
    	}
    	return result;
    }
    
    public int[] toArrayInt(List<Integer> list) {
    	int[] result = new int[list.size()];
    	int i = 0;
    	for(Integer d : list) {
    		result[i] = d;
    		i++;
    	}
    	return result;
    }
    
}
