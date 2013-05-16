package structures.cell.ic;

import control.parameters.Parameters;
import structures.cell.AbstractCell;

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
public abstract class InitialCondition {

	AbstractCell[][] lattice;

	public InitialCondition(Parameters p) {
		lattice = new AbstractCell[p.W()][p.W()];
	}
	
	public AbstractCell[][] getConfiguration() {
		return lattice;
	}
	
	/**
	 * Assigns a cell to a lattice position. Returns the old cell.
	 */
	protected AbstractCell assign(int x, int y, AbstractCell cell) {
		AbstractCell old = lattice[x][y];
		lattice[x][y] = cell;
		
		return old;
	}
}
