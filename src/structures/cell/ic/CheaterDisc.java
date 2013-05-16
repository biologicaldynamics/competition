package structures.cell.ic;

import structures.cell.AbstractCell;
import structures.cell.Cheater;
import structures.cell.Producer;
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
 * A disc of cheaters, with radius specified by icArgument,
 * in a field of cooperators.
 *  
 * @author dbborens@princeton.edu
 *
 */
public class CheaterDisc extends InitialCondition {

	private int x0;
	private int y0;
	public CheaterDisc(Parameters p) {
		super(p);
		
		x0 = p.W() / 2;
		y0 = p.W() / 2;
		
		placeBackground(p);
		
		placeDisc(p);
	}

	private void placeBackground(Parameters p) {
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				AbstractCell c;
				c = new Producer(p, x, y);
				assign(x, y, c);
			}
			
		}
	}

	private void placeDisc(Parameters p) {
		for (int r = 0; r < p.getIcArgument(); r++) {
			for (int dx = 0; dx <= r; dx++) {
				int dy = r - dx;
				
				placeFour(p, dx, dy);
				
			}
		}
	}
	
	private void placeFour(Parameters p, int dx, int dy) {
		makeCheater(p, x0 + dx, y0 + dy);
		makeCheater(p, x0 + dx, y0 - dy);
		makeCheater(p, x0 - dx, y0 + dy);
		makeCheater(p, x0 - dx, y0 - dy);
	}

	private void makeCheater(Parameters p, int x, int y) {
		Cheater c = new Cheater(p, x, y);
		assign(x, y, c);
	}
}
