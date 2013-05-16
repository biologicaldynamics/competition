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
 * An empty cell location. Can be overwritten by other cells
 * during cell division. Doesn't consume or produce anything.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class EmptyCell extends AbstractCell {

	
	public EmptyCell(Parameters p, int x, int y) {
		super(p, x, y);
		oldDerivative = 0;

		// Empty cells have no biomass
		biomass = 0;
	}
	
	@Override
	// TODO Test me
	public AbstractCell duplicate(int xx, int yy) {
		EmptyCell cell = new EmptyCell(p, xx, yy);
		cell.biomass = biomass;
		cell.oldDerivative = oldDerivative;
		return cell;
	}

	@Override
	public double getChangeRate(double c, boolean setDerivative) {
		if (setDerivative)
			oldDerivative = 0;
		
		return 0;
	}
	
	@Override
	/**
	 * Empty cells will never grow or die.
	 */
	public double criticalTime(double c) {
		return Double.POSITIVE_INFINITY;
	}
	
	@Override
	/**
	 * Empty cells don't metabolise.
	 */
	public int metabolise(double c, double delta_t) {
		return 0;
	}
	
	@Override
	public byte getType() {
		return AbstractCell.EMPTY;
	}
	
	@Override
	public double getProduction() {
		// Dead cells don't produce any enzyme
		return 0;
	}

}
