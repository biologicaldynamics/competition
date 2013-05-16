package operations.matrix;


import structures.MatrixUtils;
import control.parameters.Parameters;
import no.uib.cipr.matrix.*;
import no.uib.cipr.matrix.sparse.*;

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
public class Diffusion extends CompDiagMatrix{

	private int n;
	private int w;
	
	public Diffusion(Parameters p) {
		this(p.N(), p.W(), p.getDiffusion());
	}

	/**
	 * Constructs a diffusion operator given a total size, a width
	 * and a diffusion constant (scaled to some dt.)
	 * 
	 * @param n Total number of positions (n = w * h)
	 * @param w System width
	 * @param r Diffusion constant, scaled to some specified dt.
	 */
	public Diffusion(int n, int w, double r) {
		super(n, n);
		this.n = n;
		this.w = w;
		
		buildMatrix(r);
	}
	
	/**
	 * Converts a row of the matrix into a matrix of size p.W() x p.H()
	 */
	public Matrix rowAsMatrix(int r) {
			
		Vector v = new DenseVector(n);
		
		for (int q = 0; q < n; q++) {
			double value = get(r, q);
			v.set(q, value);
		}
		
		return MatrixUtils.vectorToMatrix(v, w);
	}
	
	/**
	 * For the  heat equation in 2 dimensions, 
	 * center-space differencing takes the form:
	 * 
	 * u(x, y) - D*(u(x-1, y) + u(x+1, y) + u(x, y-1) + u(x, y+1) - 4u(x, y))
	 * 
	 * The first four terms produce bands at +/ 1 and +/- p.W()IDTH. The last
	 * produces a diagonal of -4 terms.
	 * 
	 * Everything else here is for the boundary conditions.
	 * 
	 * See the writeup of the algorithm in the Succesion Modeling project directory
	 * for more detail.
	 * 
	 */
	private void buildMatrix(double r) {
		

		for (int i = 0; i < n; i++) {
			int j_min = (i > w ? i - w : 0);
			int j_max = (i < n - w ? i + w : n - 1);
			
			for (int j = j_min; j <= j_max; j++) {
				
				// x=0 at left boundary; x = p.W-1 at right boundary
				int x = i % w;
				
				// Periodic behavior at left boundary
				if (x == 0 && j == i + w - 1)
					set(i, j, r);
				
				// Periodic behavior at right boundary
				else if (x == w - 1 && j == i - w + 1)
					set(i, j, r);
							
				// Diffusive component from origin, general case
				else if (i==j)
					set(i, j, 1 - 4 * r);
				
				// Left diffusion component.
				else if (i == j + 1 && x != 0)
					set(i, j, r);
				
				// Right diffusion component
				else if (i == j - 1 && x != w - 1)
					set(i, j, r);
				
				// Upward diffusion component
				else if (i == j + w)
					set(i, j, r);
				
				// Downward diffusion component
				else if (i == j - w)
					set(i, j, r);
				
			} 
		}
		
		// Vertical periodic behavior
		for (int c = 0 ; c < w; c++) {
			// Upper
			set(c, n - w + c, r);
			
			// Lower
			set(n - w + c, c, r);
		}

	}
}
