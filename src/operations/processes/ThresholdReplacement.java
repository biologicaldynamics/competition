package operations.processes;

import java.util.ArrayList;

import operations.processes.helpers.ReplacementHelper;


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
 * @author dbborens@princeton.edu
 *
 */
public class ThresholdReplacement extends AbstractThresholdProcess {

	private ReplacementHelper helper;
	
	public ThresholdReplacement(Parameters p, InitialCondition ic) {
		super(p, ic);
		helper = new ReplacementHelper(p, manager);
	}
	
	@Override
	public void process(ArrayList<AbstractCell> cells) throws EquilibriumException {
		shuffle(cells);

		for (int i = 0; i < cells.size(); i++) {
			AbstractCell cell = cells.get(i);

			// If the cell is not where it used to be, it got overwritten: skip			
			if (lattice[cell.x()][cell.y()] != cell)
				continue;
			
			helper.process(cell, false);
		}
		
	}
}
