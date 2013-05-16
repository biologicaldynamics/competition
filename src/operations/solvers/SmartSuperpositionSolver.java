package operations.solvers;

import java.util.ArrayList;

import structures.distributions.AbstractPointDistribution;
import control.parameters.Parameters;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.SparseVector;

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
 * Uses either the positive or negative superposition solver depending
 * on whether there are more cooperators or more cheaters.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class SmartSuperpositionSolver extends AbstractRDSolver {

	protected PositiveSuperpositionSolver pos;
	protected NegativeSuperpositionSolver neg;
	
	public SmartSuperpositionSolver(Parameters p, AbstractPointDistribution dist) {
		super(p, dist);
		
		pos = new PositiveSuperpositionSolver(p, dist);		
		neg = new NegativeSuperpositionSolver(p, dist);
		
	}

	@Override
	public Vector solve(Vector source) {
		if (p.getProduction() < p.epsilon())
			return trivialSolution();
		
		ArrayList<Integer> producers = new ArrayList<Integer>(p.N() / 2);
		ArrayList<Integer> defectors = new ArrayList<Integer>(p.N() / 2);
		
		for (int i = 0; i < p.N(); i++) {
			if (source.get(i) == 0)
				defectors.add(i);
			else
				producers.add(i);
		}
		
		if (defectors.size() > producers.size())
			return pos.solve(producers);
		else
			return neg.solve(defectors);
	}

	protected Vector trivialSolution() {
		Vector solution = new SparseVector(p.N());
		return solution;
	}

	@Override
	public double getSourceConcentration() {

		return pos.getSourceConcentration();
	}
	
	

}
