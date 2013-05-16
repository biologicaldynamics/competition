package operations.matrix;

import no.uib.cipr.matrix.BandMatrix;

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
 * @author dbborens@princeton.edu
 *
 */
public class Decay extends BandMatrix {

	/**
	 * Constructs a decay operator matrix, which is just
	 * the identity matrix times some constant.
	 *  
	 * @param n The size of the matrix.
	 * 
	 * @param decay The decay rate. 
	 *  
	 */
	public Decay(int n, double decay) {
		super(n, 0, 0);
		for (int i = 0; i < n; i++) {
			set(i, i, decay);
		}
	}
}
