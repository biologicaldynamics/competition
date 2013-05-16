package operations.processes.helpers;

import structures.cell.AbstractCell;
import structures.cell.DeadCell;
import structures.cell.EmptyCell;
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
 * @author dbborens@princeton.edu
 *
 */
public class LifeCycleHelper {

	// If true, dead cells are set to EMPTY; otherwise they are set to DEAD
	protected final boolean VACATE_ON_DEATH = false;
	
	private Parameters p;
	private AbstractCell[][] lattice;
	
	public LifeCycleHelper(Parameters p, AbstractCell[][] lattice) {
		this.p = p;
		this.lattice = lattice;
	}
	
	/**
	 * The shoving algorithm moves the parent cell in the direction
	 * shoved. It is therefore displaced. Put the child in the origin.
	 * 
	 * @param origin
	 * @param target
	 */
	public void fission(AbstractCell cell, int x, int y) {
		// Divide biomass in half
		cell.halveBiomass();
		
		// Create a duplicate cell object
		AbstractCell child = cell.duplicate(x, y);
		
		// Place child in the origin
		set(x, y, child);
	}
		
	/**
	 * Returns an x value that takes into account horizontal periodic
	 * boundary conditions.
	 */
	public int wrap(int x, int w) {

		// Java's % operator is actually a remainder, so for negative numbers it doesn't
		// do what one might expect for a "modulo" operator
		return(x < 0 ? (x % w + w) % w : x % w);
	}
	
	/**
	 * Assign a cell to a new coordinate, honoring class requirements
	 * and boundary conditions. Note that, if the cell was previously
	 * assigned to another position, it now exists in BOTH positions
	 * in the lattice, which is an illegal state. An assignment must
	 * be made to the old position for the state to be legal.
	 */
	public void set(int x, int y, AbstractCell cell) {
		x = wrap(x, p.W());
		y = wrap(y, p.W());
		cell.setCoordinate(x, y);
		lattice[x][y] = cell;
	}
	
	/**
	 * Retrieve a cell from a coordinate, honoring periodic boundary
	 * conditions.
	 */
	public AbstractCell get(int x, int y) {
		return lattice[wrap(x, p.W())][wrap(y, p.W())];
	}
	
	
	/**
	 * "Kill" the cell at (x, y) by overwriting it with a dead/empty cell.
	 */
	public void kill(int x, int y) {
		AbstractCell cell;
		
		System.out.println("Cell (" + x + ", " + y + ") has died.");
		
		if (VACATE_ON_DEATH)
			cell = new EmptyCell(p, x, y);
		else
			cell = new DeadCell(p, x, y);
		
		assign(x, y, cell);
	}
	
	/**
	 * Assigns a cell to a lattice position. Returns the old cell.
	 */
	protected AbstractCell assign(int x, int y, AbstractCell cell) {
		AbstractCell old = lattice[x][y];
		lattice[x][y] = cell;
		
		return old;
	}
	
	
	/**
	 * Returns an x value that takes into account horizontal periodic
	 * boundary conditions.
	 */
	public int wrap(int x) {
		int w = p.W();

		// Java's % operator is actually a remainder, so for negative numbers it doesn't
		// do what one might expect for a "modulo" operator
		return(x < 0 ? (x % w + w) % w : x % w);
	}
	
	/**
	 * Returns an exponentially distributed random number.
	 */
	public double expRandom(double lambda) {
		// Get a random number between 0 (inc) and 1 (exc)
		double u = p.getRandom().nextDouble();
		
		// Inverse of exponential CDF
		return Math.log(1 - u) / (-1 * lambda);
	}
}
