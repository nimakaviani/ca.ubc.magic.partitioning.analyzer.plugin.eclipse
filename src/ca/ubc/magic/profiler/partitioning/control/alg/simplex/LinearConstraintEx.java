package ca.ubc.magic.profiler.partitioning.control.alg.simplex;

import org.apache.commons.math.optimization.linear.Relationship;

public class LinearConstraintEx {

	private int count;
	private double[] row;
	private int[] colno;
	Relationship type;
	double rh;
	
	public LinearConstraintEx(int count, double[] row, int[] colno,
			Relationship type, double rh) {
		this.count = count;
		this.row = row;
		this.colno = colno;
		this.type = type;
		this.rh = rh;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double[] getRow() {
		return row;
	}

	public void setRow(double[] row) {
		this.row = row;
	}

	public int[] getColno() {
		return colno;
	}

	public void setColno(int[] colno) {
		this.colno = colno;
	}

	public Relationship getType() {
		return type;
	}

	public void setType(Relationship type) {
		this.type = type;
	}

	public double getRh() {
		return rh;
	}

	public void setRh(double rh) {
		this.rh = rh;
	}
	
}
