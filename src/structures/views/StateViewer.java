package structures.views;

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
 * A wrapper class for the complete state of the system at a given time.
 * 
 *
 */
public class StateViewer {

	private double gillespie;
	
	private VectorViewer dm_dt;
	private VectorViewer enzyme;
	private VectorViewer biomass;
	private Byte[] color;
	
	public StateViewer(VectorViewer d, VectorViewer c, VectorViewer b, Byte[] color, double gillespie) {
		dm_dt = d;
		enzyme = c;
		biomass = b;
		
		this.color = color;
		this.gillespie = gillespie;
	}
	
	public VectorViewer getEnzyme() {
		return enzyme;
	}
	
	public VectorViewer getBiomass() {
		return biomass;
	}
	
	public VectorViewer getDerivatives() {
		return dm_dt;
	}
	
	public Byte[] getColor() {
		return color;
	}
	
	public double getGillespie() {
		return gillespie;
	}
}
