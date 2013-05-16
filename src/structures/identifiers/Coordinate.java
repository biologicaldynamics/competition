package structures.identifiers;

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
 * Lightweight coordinate in a 2D continuum
 * with time dimension.
 */
public class Coordinate {
	
	private int x;
	private int y;
	private double t;
	
	public Coordinate(int x, int y, double t) {
		this.x = x;
		this.y = y;
		this.t = t;
	}
	
	public int x() {
		return x;
	}
	
	public int y() {
		return y;
	}
	
	public double t() {
		return t;
	}
}
