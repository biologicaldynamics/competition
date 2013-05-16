package structures.identifiers;

/**
 *
 * Copyright (c) 2013, David Bruce Borenstein.
 * 
 * This file is part of the source code for "Non-local interaction via diffusible resource 
 * prevents coexistence of cooperators and cheaters in a lattice model"
 * (PLOS ONE, Borenstein, et al. 2013).
 * 
 * This work is licensed under the Creative Commons 2.0 BY-NC license.
 * 
 * Attribute (BY) -- You must attribute the work in the manner specified 
 * by the author or licensor (but not in any way that suggests that they 
 * endorse you or your use of the work).
 * 
 * Noncommercial (NC) -- You may not use this work for commercial purposes.
 * 
 * For the full license, please visit:
 * http://creativecommons.org/licenses/by-nc/3.0/legalcode
 * 
 * 
 * Heavyweight class for tracking the historical minimum and maximum
 * of a scalar field changing with time.
 * 
 * Assumes finite extrema.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class Extrema {

	// Minimum
	protected double min = Double.POSITIVE_INFINITY;
	protected Coordinate argMin = null;
	
	// Maximum
	protected double max = Double.NEGATIVE_INFINITY;
	protected Coordinate argMax = null;
	
	protected int width;
	
	public Extrema (int w) {
		width = w;
	}
	
	/**
	 * Initialize all fields to a set of loaded values. Used when importing
	 * metadata from a previous run.
	 * 
	 */
	public void load(double min, Coordinate argMin, double max, Coordinate argMax) {
		this.min = min;
		this.max = max;
		this.argMin = argMin;
		this.argMax = argMax;
	}
	
	/**
	 * Compares a value to the minimum and maximum. If it goes beyond an
	 * existing extremum, it gets assigned. Returns true if an assignment
	 * was made. If it is the first value checked, by definition 
	 * it will be both the minimum and maximum.
	 */
	public boolean consider(double u, int i, double t) {
		boolean assigned = false;
		if (u > max) {
			Coordinate c = new Coordinate(i % width, i / width, t);
			max = u;
			argMax = c; 
			assigned = true;
		}
		
		if (u < min) {
			Coordinate c = new Coordinate(i % width, i / width, t);
			min = u;
			argMin = c;
			assigned = true;
		}
		
		return assigned;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(min);
		sb.append('@');
		appendArgMin(sb);
		sb.append(':');
		sb.append(max);
		sb.append('@');
		appendArgMax(sb);
		return sb.toString();
	}

	private void appendArgMax(StringBuilder sb) {
		if (argMax == null) {
			sb.append("NaN,NaN,NaN");
			return;
		}
		
		sb.append(argMax.x());
		sb.append(',');
		sb.append(argMax.y());
		sb.append(',');
		sb.append(argMax.t());
	}

	private void appendArgMin(StringBuilder sb) {
		if (argMin == null) {
			sb.append("NaN,NaN,NaN");
			return;
		}
		
		sb.append(argMin.x());
		sb.append(',');
		sb.append(argMin.y());
		sb.append(',');
		sb.append(argMin.t());
	}
	
	public double min() {
		return min;
	}
	
	public double max() {
		return max;
	}
	
	public Coordinate argMin() {
		return argMin;
	}
	
	public Coordinate argMax() {
		return argMax;
	}
}
