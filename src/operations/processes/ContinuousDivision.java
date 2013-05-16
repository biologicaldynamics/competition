package operations.processes;

import io.BufferedStateWriter;
import io.IteratedSimulationWriter;
import io.PhaseWriter;
import operations.processes.helpers.DivisionHelper;
import no.uib.cipr.matrix.Vector;
import structures.EmptyPDFException;
import structures.WeightedUniformPDF;
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
 * @author dbborens@princeton.edu
 *
 */
public class ContinuousDivision extends AbstractContinuousProcess {

	DivisionHelper helper;
	public ContinuousDivision(Parameters p, InitialCondition ic) {
		super(p, ic);
		helper = new DivisionHelper(p, manager, lattice);
	}

	public void process(Integer target) throws EquilibriumException {
		int x = target % p.W();
		int y = target / p.W();
		
		AbstractCell cell = manager.get(x, y);

		helper.process(cell);
	}

	@Override
	public void turnover(Vector c, BufferedStateWriter bsw, IteratedSimulationWriter isw, PhaseWriter pw) throws EquilibriumException {
		WeightedUniformPDF pdf = new WeightedUniformPDF(p);

		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				
				// Get index
				int i = y * p.W() + x;
				
				// Get catalyst
				double catalyst = c.get(i);
				
				// Get growth rate (also set derivative field for display)
				double growthRate = manager.get(x, y).getChangeRate(catalyst, true);
				
				// Get cell type
				byte type = manager.get(x, y).getType();
				
				// Sanity check
				if ((type == AbstractCell.DEAD || type == AbstractCell.EMPTY) && (growthRate != 0)) {
					throw new IllegalStateException("Consistency error: Empty or dead cell has a non-zero growth rate");
				}
				
				// Add non-zero cells to distribution, weighted by growth rate
				if (growthRate != 0)
					pdf.add(i, growthRate);
			}
		}
		
		if (!p.getOutput().equalsIgnoreCase("SPARSE") || p.getOutput().equalsIgnoreCase("MINIMAL"))			
			bsw.push(c, this, 0D);
		
		if (!p.getOutput().equalsIgnoreCase("MINIMAL"))
			isw.push(c,  this, 0, 0D);
		
		// Finalize distribution
		try {
			pdf.makeReady();
		} catch (EmptyPDFException ex) {
			throw new EquilibriumException(AbstractCell.OTHER, -1);
		}
		
		// Choose a target
		Integer target = pdf.sample();

		process(target);
	}
}
