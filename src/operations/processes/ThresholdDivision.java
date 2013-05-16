package operations.processes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import operations.processes.helpers.DivisionHelper;


import structures.cell.AbstractCell;
import structures.cell.ic.InitialCondition;


import control.EquilibriumException;
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
 * A cell operator that divides cells when they reach a threshold size.
 * 
 * Cells divide in the direction that will require the minimum number of displacements. If there
 * are multiple candidate locations, it chooses one at random. Because the shoving affects grid
 * availability, this process cannot execute simultaneously. (Possibly it can, but it's not worth
 * the trouble to figure out how.) Instead, we randomize the update order each time. 
 * 
 * For the "Case 0" divider, both boundaries are periodic. 
 * 
 */
public class ThresholdDivision extends AbstractThresholdProcess {
	
	private DivisionHelper helper;
	public ThresholdDivision(Parameters p, InitialCondition ic) {
		super(p, ic);
		
		helper = new DivisionHelper(p, manager, lattice);
	}



	/**
	 * Go through a list of dividing cells and divide them, shoving other
	 * cells in the process.
	 * @param dividingCells
	 */
	public void process(ArrayList<AbstractCell> cells) throws EquilibriumException {
		// Randomize the processing order of the cells to avoid systematic bias
		shuffle(cells);

		// If one of the cells can't divide, the system is full; don't keep trying
		
		// Go through the shuffled list of cells and move them.
		for (int i = 0; i < cells.size(); i++) {

			helper.process(cells.get(i));

		}
	
	}

	
	/**
	 * Shuffle an array according to the Fisher-Yates
	 * method (repeated swapping).
	 */
	protected void shuffle(ArrayList<AbstractCell> v) {
		int n = v.size();
		
		for (int i = n-1; i > 0; i--) {
			int j = p.getRandom().nextInt(i);
			AbstractCell swap = v.get(i); 
			v.set(i, v.get(j));
			v.set(j, swap);
		}
	}







	

}