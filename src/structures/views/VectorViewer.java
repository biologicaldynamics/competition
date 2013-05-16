package structures.views;

import no.uib.cipr.matrix.Vector;

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
 * Read-only vector viewer with scaling. Scale is defined at
 * construction: it could be a global range for all time
 * points, or specific to this vector.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class VectorViewer {

	protected Vector v;

	private double max;
	
	private double min;
	
	private double range;
	
	public VectorViewer(Vector v, double min, double max) {
		this.v = v;
		this.min = min;
		this.max = max;
		range = max - min;
	}
	
	public double max() {
		return max;
	}
	
	public double min() {
		return min;
	}
	
	public double get(int i) {
		return v.get(i);
	}

	public int size() {
		return v.size();
	}
	
	/** 
	 * Returns a value scaled from 0 to 1, with 0 corresponding
	 * to the minimum value of the vector and 1 corresponding to the
	 * maximum.
	 * 
	 * @param i Index into the vector.
	 */
	public double getScaled(int i) {
		if (range == 0)
			return 0;
		
		// If we are using non-zero extrema, return zeros as zeros; otherwise, normalize
		if (min != 0 && v.get(i) == 0d)
			return 0d;
					
		if (v.get(i) < min)
			throw new RuntimeException(v.get(i) + " < " + min + " @ " + i);
		
		double x = (v.get(i) - min) / range;		
		return x;
	}
}

