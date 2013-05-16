package operations.solvers;

import structures.distributions.AbstractPointDistribution;
import no.uib.cipr.matrix.Vector;
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
 * Base class for all reaction-diffusion process solvers.
 * They should all return the same results. The difference
 * is in the algorithm they employ.
 * 
 * 
 * @author dbborens@princeton.edu
 *
 */
public abstract class AbstractRDSolver {

	protected Parameters p;
	protected AbstractPointDistribution dist;

	
	public AbstractRDSolver(Parameters p, AbstractPointDistribution dist) {
		this.p = p;
		this.dist = dist;
	}
	
	public abstract Vector solve (Vector source);

	/**
	 * Returns the value of the distribution at C(0, 0).
	 * 
	 * @return
	 */
	public abstract double getSourceConcentration();
}
