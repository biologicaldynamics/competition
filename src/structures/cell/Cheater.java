package structures.cell;

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
 * A cell that uses enzyme, but does not produce it.
 *
 */
public class Cheater extends AbstractCell {

	public Cheater(Parameters p, int x, int y) {
		super(p, x, y);
		if (p.randomizeCheaters())
			biomass = p.getRandom().nextDouble() * p.getThreshold();
		else
			biomass = p.getThreshold() / 2d;
	}

	@Override
	public AbstractCell duplicate(int xx, int yy) {
		Cheater cell = new Cheater(p, xx, yy);
		cell.biomass = biomass;
		cell.oldDerivative = oldDerivative;
		return cell;
	}

	@Override
	public double getProduction() {
		return 0d;
	}

	@Override
	public byte getType() {
		return AbstractCell.CHEATER;
	}

}
