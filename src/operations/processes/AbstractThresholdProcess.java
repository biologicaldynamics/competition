package operations.processes;

import io.BufferedStateWriter;
import io.IteratedSimulationWriter;
import io.PhaseWriter;

import java.util.ArrayList;

import no.uib.cipr.matrix.Vector;

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
public abstract class AbstractThresholdProcess extends AbstractLifeCycle {

	public AbstractThresholdProcess(Parameters p, InitialCondition ic) {
		super(p, ic);
	}

	public abstract void process(ArrayList<AbstractCell> cells) throws EquilibriumException;

	/**
	 * Finds cells that will divide or die the soonest and updates
	 * the system.
	 * 
	 * @return 
	 */
	public void turnover(Vector c, BufferedStateWriter bsw, IteratedSimulationWriter isw, PhaseWriter pw) throws EquilibriumException {

		throw new UnsupportedOperationException("Visualizations are not currently implemented correctly in the Threshold Process. " + 
				"Since the same loop both updates cell properties and kills cells, one cannot call the state writer in between updating " + 
				"properties and killing/creating cells. However, doing it at the end results in the cell state lagging behind the field " + 
				"states. Consider setting biomass to zero for cells that are marked to die, designating them for death in some way, writing" +
				"the state and THEN destroying them, before creating new cells.");
		
		/**
		// Find time interval
		double interval = calcMinInterval(c);
		
		// As a naive guess, pre-allocate enough memory for up to W cells
		// to divide at the same time. 
		ArrayList<AbstractCell> toDivide = new ArrayList<AbstractCell>(p.W());
		
		// Project out biomass of all cells over that interval
		for (int y = 0; y < p.H(); y++) {
			for (int x = 0; x < p.W(); x++) {
				int i = y * p.W() + x;
				
				// A side effect of the 'metabolise' method is to store the rate
				// of change in the 'derivative' field. This will be transferred
				// to any child cell, should the cell divide.
				int outcome = lattice[x][y].metabolise(c.get(i), interval);
				
				// Designate dividing cells for division.
				if (outcome == 1)
					toDivide.add(lattice[x][y]);
				
				// Remove dying cells.
				else if (outcome == -1)
					manager.kill(x, y);
			}
		}
		
		
		// Perform cell division process on dividing cells in random order.
		process(toDivide);
		
		bsw.push(c, this, 0D);
		**/
	}
	
	/**
	 * Find the cell closest to death or division, given its current biomass
	 * and its local steady-state digested substrate concentration.

	 * @return
	 */
	protected double calcMinInterval(Vector c) throws EquilibriumException {
		// Find the cell closest to death or division
		double minInterval = Double.POSITIVE_INFINITY;
		
		for (int y = 0; y < p.W(); y++) {
			for (int x = 0; x < p.W(); x++) {
				int i = y * p.W() + x;
				
				double interval = lattice[x][y].criticalTime(c.get(i));
				
				if (interval < minInterval) {
					minInterval = interval;
				}
			}
		}
		
		if (minInterval == Double.POSITIVE_INFINITY)
			throw new EquilibriumException(AbstractCell.OTHER, -1);
		
		return minInterval;
	}
}
