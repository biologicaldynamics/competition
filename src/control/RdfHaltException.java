package control;

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
 * Signals that the simulation has halted itself because the desired
 * population fraction has been reached. When this exception is thrown,
 * the simulation will record the radial distribution function, conclude
 * the simulation as if the final time step has been reached, and then
 * begin the next simulation.
 *  
 * @author dbborens@princeton.edu
 *
 */
public class RdfHaltException extends Exception {

	private static final long serialVersionUID = 1718803511575978345L;

	// Actual number of invaders observed (could be greater than target fraction,
	// depending on update rules)
	private int particles;
	
	public RdfHaltException(int particles) {
		super("Target population fraction reached. Calculating RDF.");
		this.particles = particles;
	}

	public int getParticles() {
		return particles;
	}
}
