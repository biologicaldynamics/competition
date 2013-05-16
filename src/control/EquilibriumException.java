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
 * A special exception that indicates that no cells will divide or die,
 * even given an infinite amount of time. This is a signal to end the
 * simulation.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class EquilibriumException extends Exception {

	private static final long serialVersionUID = 8219045343504827225L;

	// This is a cell type from AbstractCell, where applicable. If the
	// life cycle process does not support this coding, it uses
	// AbstractCell.OTHER.
	byte fixationType;
	double gillespieTime;
	
	public EquilibriumException(byte fixationType, double gillespieTime) {
		super();
		
		this.fixationType = fixationType;
		this.gillespieTime = gillespieTime;
	}
	
	public byte getFixationType() {
		return fixationType;
	}
	
	public double getGillespieTime() {
		return gillespieTime;
	}
}
