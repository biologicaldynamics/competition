package structures.views;

import no.uib.cipr.matrix.Matrix;

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
 * A read-only wrapper class for Matrix objects.
 * Finds the matrix maximu
 * @author dbborens@princeton.edu
 *
 */
public class MatrixViewer {

	private Matrix m;
		
	public MatrixViewer(Matrix m) {
		this.m = m;
	}
	
	public double get(int i, int j) {
		return m.get(i, j);
	}
	
	public int numRows() {
		return m.numRows();
	}
	
	public int numColumns() {
		return m.numColumns();
	}
}