package structures.cell.ic;

import structures.cell.*;
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
 * A well mixed population of cheaters and cooperators, in the proportion
 * specified in the IC. The species with the larger initial fraction
 * is assigned to every location in the simulation. Then individuals of
 * the other kind are assigned at random.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class WellMixed extends InitialCondition {

	public WellMixed(Parameters p) {
		super(p);

		boolean moreCheaters = (p.getIcArgument() > (p.N() / 2));
		
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				if (moreCheaters)
					placeCheater(p, x, y);
				else 
					placeCooperator(p, x, y);
			}
		}

		// We place the dominant kind first, and then "sprinkle" the other kind,
		// so that we don't wind up spending a really long time on the "sprinkle"
		// step (looking for sites not already "sprinkled"). There may be a better
		// way to do this.
		if (moreCheaters)
			sprinkleCooperators(p);
		else
			sprinkleCheaters(p);
	}
	

	private void sprinkleCheaters(Parameters p) {
		for (int i = 0; i < p.getIcArgument(); i++) {
			int x = p.getRandom().nextInt(p.W());
			int y = p.getRandom().nextInt(p.W());

			while (lattice[x][y].getType() == AbstractCell.CHEATER) {
				x = p.getRandom().nextInt(p.W());
				y = p.getRandom().nextInt(p.W());
			}
			//System.out.println("Invader: " + x + ", " + y);
			placeCheater(p, x, y);
		}				
	}
	
	private void sprinkleCooperators(Parameters p) {
		int numCooperators = p.N() - p.getIcArgument();
		for (int i = 0; i < numCooperators; i++) {
			int x = p.getRandom().nextInt(p.W());
			int y = p.getRandom().nextInt(p.W());

			while (lattice[x][y].getType() == AbstractCell.COOPERATOR) {
				x = p.getRandom().nextInt(p.W());
				y = p.getRandom().nextInt(p.W());
			}
			//System.out.println("Invader: " + x + ", " + y);
			placeCooperator(p, x, y);
		}		
	}
	
	private void placeCooperator(Parameters p, int x, int y) {
		AbstractCell c;
		c = new Producer(p, x, y);
		assign(x, y, c);
	}
	
	private void placeCheater(Parameters p, int x, int y) {
		AbstractCell c = new Cheater(p, x, y);
		assign(x, y, c);
	}
}
