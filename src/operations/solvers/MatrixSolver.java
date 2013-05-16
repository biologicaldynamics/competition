package operations.solvers;

import control.parameters.Parameters;
import operations.matrix.Decay;
import operations.matrix.Diffusion;
import structures.MatrixUtils;
import structures.distributions.AbstractPointDistribution;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CGS;
import no.uib.cipr.matrix.sparse.CompDiagMatrix;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.DiagonalPreconditioner;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.Preconditioner;

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
 * Solve a diffusion-decay matrix for a new
 * source vector on every pass. This approach
 * is necessary only if the sources are not
 * independent of one another--e.g., they
 * quorum sense or have feedback.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class MatrixSolver extends AbstractRDSolver {

	private CompRowMatrix D;
	private Matrix I;
	private double sourceConcentration;
	
	public MatrixSolver (Parameters p, AbstractPointDistribution dist) {
		super(p, dist);
		System.out.println("Using the matrix solver.");
		
		sourceConcentration = dist.get(0, 0);
		
		Diffusion op = new Diffusion(p);
		D = new CompRowMatrix(op);
		
		D.scale(-1D);
		I = MatrixUtils.I(p.N());
		//CellularAutomaton ca = new AlternatingLayer(p, 16);
	}
	@Override
	public Vector solve(Vector source) {
		// The matrix we will solve
		Matrix operator = enzymeOperator(p);
		int n = p.N();
		Vector template = source.copy();
		
		System.out.println("Solving this matrix");
		System.out.println(MatrixUtils.matrixForm(operator));
		
		System.out.println("\n\nUsing this vector");
		System.out.println(source);
		
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
		//System.exit(-1);		
		return sol;
	}
	
	// Construct the matrix to be solved for enzyme concentration
	private Matrix enzymeOperator(Parameters p) {
		
		CompDiagMatrix A = new CompDiagMatrix(p.N(), p.N());
		A.add(new Decay(p.N(), p.getDecay()));
		A.add(MatrixUtils.I(p.N()));
		
		Diffusion grad = new Diffusion(p);
		grad.scale(-1D);
		
		A.add(grad);

		//System.out.println(MatrixUtils.matrixForm(A));
		//System.exit(-1);
		
		return A;
	}
	@Override
	public double getSourceConcentration() {
		return sourceConcentration;
	}
	
	
	
}
