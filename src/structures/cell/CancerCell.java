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
 * A "cancer" cell. It divides in every timestep, and never
 * increases or decreases in biomass. Used for diagnostics.
 * 
 * 
 * @author dbborens@princeton.edu
 *
 */
public class CancerCell extends AbstractCell {
	
	
	// What color to show up as
	private byte displayType;
	
	public CancerCell(Parameters p, int x, int y, byte displayType) {
		super(p, x, y);
		this.displayType = displayType;
		oldDerivative = 1;
		biomass = p.getThreshold();
	}

	@Override
	public AbstractCell duplicate(int xx, int yy) {
		CancerCell cell = new CancerCell(p, xx, yy, displayType);
		cell.biomass = p.getThreshold();
		return cell;
	}

	@Override
	public void halveBiomass() {
		// Does nothing.
	}
	
	@Override
	public double getBiomass() {
		return p.getThreshold();
	}

	@Override
	public byte getType() {
		return displayType;
	}

	/**
	 * If a cell can't divide, its biomass gets set to the threshold biomass
	 * and nothing else happens to it. This prevents the cells from exceeding
	 * the threshold biomass. However, cancer cells decay at a constant rate,
	 * divide every timestep, and don't eat, so we don't want them to obey
	 * this rule.
	 */
	@Override
	public void setBiomass(double biomass) {
		// Does nothing.
	}

	@Override 
	/**
	 * Always returns 0: cancer cells divides instantly
	 * 
	 * IF YOU PUT CANCER CELLS IN THE SAME SIMULATION WITH NON-CANCER CELLS,
	 * THE FOLLOWING PATHOLOGIES WILL OCCUR:
	 * 
	 *    - The non-cancer cells will never divide
	 *    - The non-cancer cells will never lose biomass
	 *    
	 * This is because the cancer cells will cause the time interval between
	 * cell update events to be 0.
	 */
	public double criticalTime(double c) {
		return 0d;
	}
	
	@Override
	/**
	 * Cancer cells divide at every time step.
	 */
	public int metabolise(double c, double delta_t) {
		return 1;
	}
	
	@Override
	public double getProduction() {
		// Produce enzyme as a normal producer cell
		return p.getProduction();
	}	
}
