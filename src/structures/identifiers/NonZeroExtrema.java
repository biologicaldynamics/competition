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
 * Same as "Extrema" but won't consider zero as a minimum or maximum. This way,
 * if a value has some fairly small set of values, all with the same sign,
 * they color plot will not show up as all one color. Note that if no non-zero
 * entries are considered, this method will malfunction.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class NonZeroExtrema extends Extrema {

	public NonZeroExtrema(int w) {
		super(w);
	}

	@Override
	public boolean consider(double u, int i, double t) {
		boolean assigned = false;
		if (u != 0 && u > max) {
			Coordinate c = new Coordinate(i % width, i / width, t);
			max = u;
			argMax = c; 
			assigned = true;
		}
		
		if (u != 0 && u < min) {
			Coordinate c = new Coordinate(i % width, i / width, t);
			min = u;
			argMin = c;
			assigned = true;
		}
		
		return assigned;
	}
}
