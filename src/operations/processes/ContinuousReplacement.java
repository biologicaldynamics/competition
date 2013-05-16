package operations.processes;

import io.BufferedStateWriter;
import io.IteratedSimulationWriter;
import io.PhaseWriter;

import no.uib.cipr.matrix.Vector;

import operations.processes.helpers.ReplacementHelper;


import structures.EmptyPDFException;
import structures.WeightedUniformPDF;
import structures.cell.AbstractCell;
import structures.cell.ic.InitialCondition;
import control.EquilibriumException;
import control.RdfHaltException;
import control.parameters.Parameters;

/**
 *  *
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
 * Chooses a random cell to replace a neighbor. The probability of being
 * selected is the growth rate * the proportion of neighbors that are
 * competitors. (So internal cells are never selected.)
 * 
 * @author dbborens@princeton.edu
 *
 */
public class ContinuousReplacement extends AbstractContinuousProcess {

	private ReplacementHelper helper;
	
	private double gillespie = 0;
	
	public ContinuousReplacement(Parameters p, InitialCondition ic) {
		super(p, ic);
		helper = new ReplacementHelper(p, manager);
	}
	
	private void process(Integer target) throws EquilibriumException {
		int x = target % p.W();
		int y = target / p.W();		
	
		AbstractCell cell = manager.get(x, y);

		helper.process(cell, true);
	}

	@Override
	public void turnover(Vector c, BufferedStateWriter bsw, IteratedSimulationWriter isw, PhaseWriter pw) throws EquilibriumException, RdfHaltException {
		//System.out.println("STARTING TIME STEP");
		int numCheats = 0;
		int numCoops = 0;
		
		double coopWeight = 0;
		double cheatWeight = 0;
		
		int frontier = 0;
		
		WeightedUniformPDF pdf = new WeightedUniformPDF(p);
		
		// TODO: This block is too verbose
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				
				if (manager.get(x, y).getType() == AbstractCell.COOPERATOR)
					numCoops++;
				else if (manager.get(x, y).getType() == AbstractCell.CHEATER)
					numCheats++;
				else
					throw new IllegalStateException("A cell that is neither a cheater nor a cooperator was found in a birth-death process.");
				
				// Get index
				int i = y * p.W() + x;
				
				// Get catalyst
				double catalyst = c.get(i);
				
				// Get growth rate (also set derivative field for display)
				double growthRate = calcChangeRate(x, y, catalyst);
							
				// Count up competing neighbors for every cell
				int competitors = countCompetitors(x, y);
				
				if (growthRate < 0D) {
					System.err.println("Negative growth rate (" + growthRate + ") in continuous replacement process. (RANDOM_SEED=" + p.getRandomSeed() + "). Cell has " + competitors + " competitors. Is a cooperator? " + (manager.get(x, y).getType() == AbstractCell.COOPERATOR));
					growthRate = 0D;
				}
				
				// Keep track of cooperator and cheater total weights
				if (manager.get(x, y).getType() == AbstractCell.COOPERATOR) {
					//System.out.println("   Cooperator: g=" + growthRate + " with " + competitors + " neighbors. (Weight=" + (growthRate * competitors) + ")");
						coopWeight += growthRate * competitors;
				} else if (manager.get(x, y).getType() == AbstractCell.CHEATER) {
					cheatWeight += growthRate * competitors;
					//System.out.println("   Cheater: g=" + growthRate + " with " + competitors + " neighbors. (Weight=" + (growthRate * competitors) + ")");
				} else
					throw new IllegalStateException("A cell that is neither a cheater nor a cooperator was found in a birth-death process.");
				
				// If it has at least one competitor, add it to the distribution.
				// The weighting is PROPORTIONAL TO THE NUMBER OF COMPETITORS, because we will force
				// the cell to replace a competitor.
				if (competitors > 0 && growthRate > p.epsilon()) {
					pdf.add(i, growthRate * competitors);	
				}
				
				if (competitors > 0) {
					frontier += competitors;
				}
			}
		}
		
		if (!(p.getOutput().equalsIgnoreCase("SPARSE") || p.getOutput().equalsIgnoreCase("MINIMAL")))
			bsw.push(c, this, gillespie);
		
		// Probability that ANY cheater replaces ANY cooperator in the next step
		double cheatGrowProbability = cheatWeight / (coopWeight + cheatWeight);
		
		pw.push(numCheats, frontier, cheatGrowProbability);
		
		//System.out.println("END OF STEP: p(cheat)=" + cheatGrowProbability + 
		//		" because total cheater weight=" + cheatWeight + " and total coop weight=" + coopWeight + ".\n");
		if (!p.getOutput().equalsIgnoreCase("MINIMAL"))
			isw.push(c,  this, frontier, cheatGrowProbability);
		
		if (p.getHaltCount() != -1 && numCheats == p.getHaltCount())
			throw new RdfHaltException(numCheats);
		
		// The Doob-Gillespie waiting time is an exponentially distributed random number 
		double ttlWeight = coopWeight + cheatWeight;
		if (ttlWeight > p.epsilon()) {
			double time = manager.expRandom(ttlWeight);
			
			gillespie += time;
		}
		
		// Finalize distribution
		try {
			pdf.makeReady();
		} catch (EmptyPDFException ex) {
			if (numCheats == p.N())				
				throw new EquilibriumException(AbstractCell.CHEATER, gillespie);
			else if (numCoops == p.N())
				throw new EquilibriumException(AbstractCell.COOPERATOR, gillespie);
			else
				throw new IllegalStateException("Equilibrium was reached with neither cooperators nor cheaters reaching fixation!");
		}
		
		// Choose a target from the weighted distribution
		Integer target = pdf.sample();

		process(target);
	}

	protected double calcChangeRate(int x, int y, double catalyst) {
		return manager.get(x, y).getChangeRate(catalyst, true);
	}

	private int countCompetitors(int x, int y) {
		
		byte selfType = manager.get(x, y).getType();
		int count = 0;
		
		AbstractCell target = manager.get(x + 1, y);
		if (target.getType() != selfType) {
			count++;
		}		
		
		target = manager.get(x - 1, y);
		if (target.getType() != selfType) {
			count++;
		}		

		target = manager.get(x, y + 1);
		if (target.getType() != selfType) {
			count++;
		}		
		
		target = manager.get(x, y - 1);
		if (target.getType() != selfType) {
			count++;
		}		

		// The self shouldn't be of a non-self type
		if (count == 5)
			throw new IllegalStateException("Consistency violation: no self type recognized");
		
		return count;
	}
}
