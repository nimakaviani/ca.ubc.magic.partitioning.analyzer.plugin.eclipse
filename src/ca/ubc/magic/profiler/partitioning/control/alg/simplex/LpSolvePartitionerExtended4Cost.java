package ca.ubc.magic.profiler.partitioning.control.alg.simplex;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import lpsolve.LpSolve;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math.optimization.linear.Relationship;

import ca.ubc.magic.profiler.dist.model.Module;

public class LpSolvePartitionerExtended4Cost extends SimplexPartitionerExtended4Cost {
	LpSolve solver; 
	
	@Override
	public void doPartition() {
		try {
			LinearObjectiveFunction objectiveFunction = mSimplexModel.getObjectiveFunction();
			Collection<LinearConstraint> constraints = mSimplexModel.getConstraints();
			RealVector v = new ArrayRealVector();
			double[] coefficients = v.append(0.0).append(objectiveFunction.getCoefficients()).toArray();
			int numVariables = coefficients.length;
			solver = LpSolve.makeLp(constraints.size(), numVariables);
			solver.setObjFn(coefficients);
			solver.setRh(0, objectiveFunction.getConstantTerm());
//			System.out.println("OBJECTIVE: " + Arrays.toString(coefficients));
			for(int i=1;i <= solver.getNcolumns();i++) {
				solver.setBinary(i, true);
			}
			Iterator<LinearConstraint> it = constraints.iterator();
			while(it.hasNext()) {
				final LinearConstraint lc = it.next();
				v = new ArrayRealVector();
				final double[] lcArray = v.append(0.0).append(lc.getCoefficients()).toArray();
				solver.addConstraint(lcArray, apacheToLpSolve(lc.getRelationship()), lc.getValue());
//				System.out.println("CONSTRAINT: " + Arrays.toString(lcArray) + " " + lc.getRelationship().name() + " " + lc.getValue());
			}
			solver.setMinim();
			solver.solve();
			double[] solution = solver.getPtrVariables();
			int i = 0;            
			System.out.println("SOLUTION: (" + Double.toString(solver.getObjective()) +")");
			
			for (;i < solution.length-1; i++){
				if (i >= mSize)
                    break;
				double current = solution[i];
				Module m = mModuleModel.getModuleMap().get(mSimplexModel.getNode(i));
				m.setPartitionId(2 - (new Double(current)).intValue());                    
			}
			
		}catch(Exception e){
			throw new RuntimeException(e.getMessage());
		}
	}
	 
	 @Override
	 public String getSolution() {
	       try {
			return Double.toString(solver.getObjective());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	 }
	 
	 public static int apacheToLpSolve(Relationship r) {
		 if(r.equals(Relationship.EQ)) {
			 return 3;
		 } else if(r.equals(Relationship.GEQ)) {
			 return 2;
		 } else if(r.equals(Relationship.LEQ)) {
			 return 1;
		 } else {
			 throw new IllegalArgumentException();
		 }
	 }
}
