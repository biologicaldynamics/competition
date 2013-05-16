package operations.processes;

import structures.cell.ic.InitialCondition;
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
 * Same as continuous replacement, but subtracts gamma x C(0, 0) from the 
 * growth rate of every cell. For all values of gamma below gamma*, the
 * worst-case growth rate is a single cooperator surroudned by an infinite
 * population of cheaters, or gamma x C(0, 0). Above gamma*, the worst
 * case growth rate becomes a single cheater surrounded by other cheaters
 * (the growth rate is beta). When gamma = gamma*, C(0, 0) x gamma = beta
 * and the two are equal.
 * 
 * The purpose of this growth process is to emphasize signal over noise
 * for gamma < gamma*. Above gamma*, it stops being meaningful because
 * some growth rates can be negative.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class ZeroBaselineReplacement extends ContinuousReplacement {

	private double baseline;
	public ZeroBaselineReplacement(Parameters p, InitialCondition ic, double sourceConcentration) {
		super(p, ic);
		
		baseline = sourceConcentration * p.getBenefit();
		//System.out.println(baseline);
		if (baseline >= p.getGrowth())
			throw new IllegalArgumentException("Illegal parameters for ZeroBaselineReplacement: gamma * C(0, 0) > beta.");
		
		
	}
	
	protected double calcChangeRate(int x, int y, double catalyst) {
		double oldRate = super.calcChangeRate(x, y, catalyst);
		
		double newRate = oldRate - baseline;
		
		// This happens because the iterative solver accumulates rounding errors, which eventually lead to values that are slightly
		// above or below the baseline values.
		if (newRate < 0) {
			newRate = 0D;
		}
		
		return newRate;
	}

}
