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
 * Represents a lattice position in the cellular automaton
 * model.
 * 
 */
public abstract class AbstractCell {
	
	protected int x;
	protected int y;
	
	protected Parameters p;
	
	public static final byte EMPTY = 0;
	public static final byte CHEATER = 1;
	public static final byte COOPERATOR = 2;
	public static final byte DEAD = 3;
	public static final byte OTHER = -1;
	
	protected double oldDerivative;
	
	// Cells start out with half the threshold biomass.
	protected double biomass;
	
	public AbstractCell(Parameters p, int x, int y) {
		this.p = p;
		this.x = x;
		this.y = y;
		oldDerivative = 0;
	}
	
	/**
	 * Make a copy of the cell object, including state.
	 * 
	 * TODO: At the moment, this will get called once per cell per timestep. This
	 * is an expensive operation in real terms because it requires garbage collection
	 * and reallocation, so this should be a top target for optimization later on. But
	 * make sure that there are regression tests to ensure normal behavior before you
	 * start reusing cell objects, or you could get some really obscure bugs.
	 * 
	 * @param c The coordinate to which the new cell is assigned.
	 * 
	 * @return a by-value duplicate of the cell object, including subclass.
	 */
	public abstract AbstractCell duplicate(int xx, int yy);
	
	// TODO Test me
	public double getBiomass() {
		return biomass;
	}
	
	/**
	 * Return the cell type. This is either "dead" (white) "empty" (black) or one of the 
	 * 6 other colors represented by an RGB color spectrum.
	 */
	public abstract byte getType();

	public void setBiomass(double biomass) {
		this.biomass = biomass;
	}
	
	public void halveBiomass() {
		this.biomass /= 2.0;
	}
	
	public int x() {
		return x;
	}
	
	public int y() {
		return y;
	}
	
	public void setCoordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Calculates the time until a cell either grows or divides.
	 * 
	 * Given enough time, a cell will either divide or die.
	 * Which one it does is determined by whether the equilibrium
	 * nutrient uptake rate exceeds the nutrient depletion rate.
	 * 
	 * @param s Equilibrium nutrient concentration.
	 * @return
	 */
	public double criticalTime(double c) {
		double changeRate = getChangeRate(c, false);
		
		// Growing: calculate time until cell division
		if (changeRate > 0)
			return (p.getThreshold() - biomass) / changeRate;
		
		// Starving: calculate time until death
		else if (changeRate < 0)
			return (biomass / changeRate) * -1d;
		
		// Equilibrium: nothing will ever happen
		else
			return Double.POSITIVE_INFINITY;
	}
	
	public double getChangeRate(double c, boolean setDerivative) {
		
		double changeRate;
		if (p.isInfiniteGamma())
			changeRate = c;
		else {
			changeRate = p.getGrowth() + (p.getBenefit() * c) - getProduction();
		}
		if (setDerivative)
			oldDerivative = changeRate;
		
		return changeRate;		
	}
	
	/**
	 * Projects the biomass forward in time by delta_t,
	 * given local equilibrium substrate concentration s.
	 * 
	 * @param delta_t Change in time (in units of dt).
	 * @param s Local substrate concentration.
	 * 
	 * @return
	 *   1, if the cell divides;
	 *   0, if the cell neither dies nor divides;
	 *  -1, if the cell dies.
	 */
	public int metabolise(double c, double delta_t) {
		double cr = getChangeRate(c, true);
		biomass += cr * delta_t;
		if (biomass >= (p.getThreshold() - p.epsilon())) {
			return 1;
		} else if (biomass <= p.epsilon()) {
			return -1;
		} else
			return 0;
	}
	
	/**
	 * Returns the amount of enzyme produced by this cell, if any.
	 */
	public abstract double getProduction();
	
	public double getOldDerivative() {
		
		return oldDerivative;
	}
}
