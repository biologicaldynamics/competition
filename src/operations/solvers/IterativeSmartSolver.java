package operations.solvers;

import control.parameters.Parameters;
import structures.distributions.AbstractPointDistribution;
import no.uib.cipr.matrix.DenseVector;
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
 * Tracks the state from the last time step. Only updates
 * behavior from the cells that have changed since then. This means
 * that the solver only takes into consideration the behavior of two
 * cells per time step, leading to O(n) time complexity as best, worst
 * and average performance for continuous processes. 
 * 
 * If more than one cell is allowed to change per time step, this complexity
 * gets worse. In the worst case, when all cell behaviors have flipped, it has
 * O(n^2) time complexity.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class IterativeSmartSolver extends SmartSuperpositionSolver {

	private Vector prevSource = null;
	private Vector prevSolution = null;
	
	public IterativeSmartSolver(Parameters p, AbstractPointDistribution dist) {
		super(p, dist);
	}

	@Override
	public Vector solve(Vector source) {
		Vector solution;
		
		/*try {
			throw new RuntimeException();
		} catch (RuntimeException ex) {
			ex.printStackTrace();
		}*/
		// Without a template, just use the SmartSuperpositionSolver
		if (prevSolution == null) {
			solution = super.solve(source);
		} else {
			solution = iterativeSolve(source);
		}
		
		prevSolution = solution;
		
		prevSource = source;
		
		return solution;
	}

	private Vector iterativeSolve(Vector source) {
		if (p.getProduction() < p.epsilon())
			return trivialSolution();
		
		// Start with the old values.
		Vector solution = prevSolution.copy();

		//double check = 0D;
		for (int i = 0; i < p.N(); i++) {
			double delta = source.get(i) - prevSource.get(i);
			//check += delta;
			
			// If this cell became a cooperator, add its effect.
			// If there is no change, ignore it.
			if (p.epsilonEquals(delta, 0D))
				continue;
			
			else if (delta > 0D) {
				//System.out.println("Adding");
				add(i, solution);
				
			// If this cell became a cheater, subtract its effect.
			} else if (delta < 0D) {
				//System.out.println("Subtracting");
				subtract(i, solution);
				
			} else {
				throw new IllegalStateException();
			}
		}
		//System.out.println("In solver: " + check);

		//if (check < p.epsilon())
		//	throw new IllegalStateException("Nothing changed!\n");
		
		return solution;
	}

	private void subtract(int i, Vector solution) {
		for (int j = 0; j < p.N(); j++) {
			double c = solution.get(j);
			c -= getContribution(i, j);
			solution.set(j, c);
		}	
	}

	private void add(int i, Vector solution) {
		for (int j = 0; j < p.N(); j++) {
			double c = solution.get(j);
			c += getContribution(i, j);
			solution.set(j, c);
		}
	}

	protected Double getContribution(int source, int target) {
		int dx = xOffset(source, target);
		int dy = yOffset(source, target);
		
		return dist.get(dx, dy);		
	}
	
	/**
	 * Find shorter offset in the y direction from y0 to
	 * y1 given periodic boundary conditions. Since
	 * the distribution is symmetric about the source,
	 * we don't care about the sign.
	 * 
	 * @param i
	 * @param k
	 * @return
	 */
	protected int yOffset(int source, int target) {
		int ys = source / p.W();
		int yt = target / p.W();

		return ys - yt;

	}

	/**
	 * Find shorter offset in the x direction from x0 to
	 * x1 given periodic boundary conditions. Since the
	 * distribution is symmetric about the source, we don't
	 * care about the sign.
	 * 
	 */
	protected int xOffset(int source, int target) {
		int xs = source % p.W();
		int xt = target % p.W();

		return xs - xt;
	}
}
