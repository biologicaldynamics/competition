package structures.distributions;


import operations.matrix.Decay;
import operations.matrix.Diffusion;
import no.uib.cipr.matrix.*;
import no.uib.cipr.matrix.sparse.*;
import java.util.Arrays;

import structures.MatrixUtils;

import control.parameters.Parameters;

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
 * Precomputes the steady state distribution of some substance
 * that diffuses with constant decay from a continuous point
 * source at the origin.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class ZeroOriginDistribution implements AbstractPointDistribution {

	// The dimensionless parameter R = ( alpha * dt ) / (dx)^2 controls
	// the error of the discrete Laplacian. In two dimensions, the Laplacian
	// fails to converge if R > 0.25. We choose a much lower R to minimize
	// error. We assume that dx=1.
	
	
	private Double[] solution;
	
	private Parameters p;
	private int x0;
	private int y0;
	/**
	 * 
	 * @param h Height of the system.
	 * @param w Width of the system.
	 * @param alpha Diffusion constant.
	 * @param mu Decay constant.
	 * @param rho Production constant.
	 */
	public ZeroOriginDistribution (Parameters p) {
		this.p = p;
		
		// In the special case of zero production, skip all the logic and just
		// populate the solution with zeros.
		if (p.getProduction() < p.epsilon()) {
			System.out.println("No production: using trivial solute distribution.");
			trivialSolution();
			return;
		}
		
		// We want to solve c = [decay - diffusion]^-1 source,
		// so operator is decay-diffusion.
		CompDiagMatrix operator = buildOperator(p.getDiffusion(), p.getDecay());

		//System.out.println(MatrixUtils.matrixForm(operator));
		//System.exit(-1);
		
		// Source vector is just rho * dt at the origin.
		Vector source = new SparseVector(p.N(), 1);
		
		// Origin is (0, 0)
		x0 = 0;
		y0 = 0;
				
		source.set(origin(), p.getProduction());
		
		System.out.println("Calculating distribution...");
		solution = solve(operator, source);
		
	}

	private void trivialSolution() {
		solution = new Double[p.N()];
		
		for (int i = 0; i < p.N(); i++)
			solution[i] = 0D;
	}

	/* (non-Javadoc)
	 * @see structures.AbstractPointDistribution#origin()
	 */
	@Override
	public int origin() {
		return 0;
	}
	
	private Double[] solve (CompDiagMatrix operator, Vector source) {
		int n = p.N();
		Vector template = source.copy();
		
		IterativeSolver solver = new CGS(template);
		Preconditioner preconditioner = new DiagonalPreconditioner(n);
		preconditioner.setMatrix(operator);
		//solver.getIterationMonitor().setIterationReporter(new OutputIterationReporter());

		Vector sol = new DenseVector(n);
		
		try {
			solver.solve(operator, source, sol);
		} catch (IterativeSolverNotConvergedException ex) {
			ex.printStackTrace();
		}
		
		return MatrixUtils.toArray(sol);
		
	}
	
	private CompDiagMatrix buildOperator(double diffusion, double decay) {
		
		CompDiagMatrix A = new CompDiagMatrix(p.N(), p.N());
		A.add(new Decay(p.N(), decay));
		A.add(MatrixUtils.I(p.N()));
		
		Diffusion grad = new Diffusion(p);
		grad.scale(-1D);
		
		A.add(grad);

		return A;
	}
	
	/* (non-Javadoc)
	 * @see structures.AbstractPointDistribution#get(int, int)
	 */
	@Override
	public Double get(int x, int y) {
		
		int x1 = Math.abs(x);
		int y1 = Math.abs(y);
				
		int i = x1 + p.W() * y1;
		
		return solution[i];
	}

	/* (non-Javadoc)
	 * @see structures.AbstractPointDistribution#getSolution()
	 */
	@Override
	public Double[] getSolution() {
		return solution;
	}
}
