package structures;

import no.uib.cipr.matrix.*;

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
public abstract class MatrixUtils {
	/**
	 * Returns a matrix in a tab-separated matrix form.
	 * 
	 * @return
	 */
	public static String matrixForm(Matrix m) {
		StringBuilder sb = new StringBuilder();
		
		int r = m.numRows();
		int c = m.numColumns();
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < c; j++) {
				sb.append(m.get(i, j));
				sb.append('\t');
			}
			sb.append('\n');
		}
		
		return sb.toString();
	}
	
	public static Matrix vectorToMatrix(Vector v, int l) {
		Matrix matrix = new DenseMatrix(v.size() / l, l);
		for (int q = 0; q < v.size(); q++) {
			int i = q / l;
			int j = q % l;
			
			Double value = v.get(q);
			matrix.set(i, j, value);
		}
		
		return matrix;
	}
	
	/**
	 * Returns an identity matrix of size n.
	 * 
	 * TODO: There must be a built-in for this.
	 */
	public static Matrix I (int n) {
		Matrix m = new BandMatrix(n, 0, 0);
		for (int i = 0; i < n; i++)
			m.set(i, i, 1d);
		
		return m;
	}
	
	public static Double[] toArray(Vector vec) {
		int n = vec.size();
		
		Double[] output = new Double[n];
		
		for (int i = 0; i < n; i++)
			output[i] = vec.get(i);
		
		return output;
	}
}
