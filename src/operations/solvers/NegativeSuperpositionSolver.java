package operations.solvers;

import structures.distributions.AbstractPointDistribution;
import control.parameters.Parameters;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import java.util.*;

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
 * Similar to the positive superposition solver, and utilizes most of its methods,
 * except that it calculates the concentration for a field of all cooperators, and
 * then subtracts the "contribution" of each cheater.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class NegativeSuperpositionSolver extends PositiveSuperpositionSolver {

	// Highest possible concentration of catalyst, given that all cells are cooperators.
	private Double ceiling;
	
	public NegativeSuperpositionSolver(Parameters p, AbstractPointDistribution dist) {
		super(p, dist);		
		calcCeiling();		
	}

	private void calcCeiling() {
		ceiling = 0D;

		// Maximum per-cell concentration of catalyst occurs when
		// all cells are cooperators. Then each cell has its own
		// production plus the production of all other cells.		
		
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				ceiling += dist.get(x, y);
			}
		}
	}
	
	@Override
	public Vector solve(Vector source) {
		List<Integer> defectors = findDefectors(source);
		
		return solve(defectors);
	}
	
	@Override
	/** 
	 * This method is exactly the same as the corresponding one
	 * in the PositiveSuperpositionSolver, except that it starts
	 * at the ceiling value and subtracts the contribution from
	 * each defector, rather than starting at 0 and adding the
	 * contribution from each producer.
	 * 
	 * WARNING: This method has the same API as the one for
	 * the positive superposition solver, but in this method the
	 * second argument is a list of defector locations.
	 *
	 */
	public Vector solve(List<Integer> defectors) {
		Vector result = new DenseVector(p.N());
		
		int m = defectors.size();
		
		// Consider each coordinate in the system (whether
		// cooperator, cheater, dead or empty).
		for (int i = 0; i < p.N(); i++) {
			
			Double c = ceiling;
			
			for (int j = 0; j < m; j++) {
				
				c -= getContribution(i, defectors.get(j));
			}
			
			result.set(i, c);
		}
		
		return result;
	}

	/**
	 * Produce a list of all defector coordinates.
	 * 
	 * @return
	 */
	private ArrayList<Integer> findDefectors(Vector source) {
		ArrayList<Integer> defectors = new ArrayList<Integer>(p.N() / 2);
		
		for (int i = 0; i < p.N(); i++) {
			if (source.get(i) == 0)
				defectors.add(i);
		}

		return defectors;
	}
	
	public double getCeiling() {
		return ceiling;
	}
}
