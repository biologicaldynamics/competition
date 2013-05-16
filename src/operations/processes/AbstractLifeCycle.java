package operations.processes;

import io.BufferedStateWriter;
import io.IteratedSimulationWriter;
import io.PhaseWriter;

import java.util.ArrayList;

import operations.processes.helpers.LifeCycleHelper;

import no.uib.cipr.matrix.BandMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;

import structures.cell.AbstractCell;
import structures.cell.DeadCell;
import structures.cell.EmptyCell;
import structures.cell.ic.InitialCondition;
import control.EquilibriumException;
import control.RdfHaltException;
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
public abstract class AbstractLifeCycle {
	
	protected AbstractCell[][] lattice;
	protected Parameters p;
	protected LifeCycleHelper manager;
	
	public AbstractLifeCycle(Parameters p, InitialCondition ic) {
		this.p = p;
		lattice = ic.getConfiguration();
		manager = new LifeCycleHelper(p, lattice);
	}
	
	/**
	 * Creates or destroys cells based on the growth rate of each ce
	 * 
	 * @param c A vector of steady-state catalyst concentrations
	 * 
	 * @throws EquilibriumException when the system is at equilibrium.
	 */
	public abstract void turnover(Vector c, BufferedStateWriter bsw, IteratedSimulationWriter isw, PhaseWriter pw) throws EquilibriumException, RdfHaltException;
	
	protected void buildLattice(int w, int h) {
		lattice = new AbstractCell[w][h];
	}
	
	
	/**
	 * Shuffle an array according to the Fisher-Yates
	 * method (repeated swapping).
	 */
	protected void shuffle(ArrayList<AbstractCell> v) {
		int n = v.size();
		
		for (int i = n-1; i > 0; i--) {
			int j = p.getRandom().nextInt(i);
			AbstractCell swap = v.get(i); 
			v.set(i, v.get(j));
			v.set(j, swap);
		}
	}

	
	public Vector getDerivatives() {
		Vector dm_dt = new DenseVector(p.N());
		for (int y = 0; y < p.W(); y++) {
			for (int x = 0; x < p.W(); x++) {
				int i = y * p.W() + x;
				double derivative = lattice[x][y].getOldDerivative();
				//if (derivative == 0 && lattice[x][y].getType() != Cell.EMPTY)
				//	throw new IllegalStateException();
				
				double derivativeSeconds = derivative / p.dt();
				dm_dt.set(i, derivativeSeconds);
			}
		}
		
		return dm_dt;
	}
	
	public Vector getBiomass() {
		Vector v = new DenseVector(p.N());
		
		for (int y = 0; y < p.W(); y++) {
			for (int x = 0; x < p.W(); x++) {
				// See algorithm write-up for coordinate system
				int i = y * p.W() + x;
				
				double biomass = lattice[x][y].getBiomass();
				v.set(i, biomass);
			}
		}
		
		return v;
	}
	
	/**
	 * Creates a duplicate cell with duplicate coordinates.
	 * 
	 * @return
	 */
	public AbstractCell getDuplicateCellAt(int x, int y) {
		AbstractCell cell = lattice[x][y].duplicate(x, y);
		return cell;
	}

	public byte getTypeAt(int x, int y) {
		return lattice[x][y].getType();
	}
	
	protected void setBiomass(int x, int y, double biomass) {
		lattice[x][y].setBiomass(biomass);
	}
	
	/**
	 * Returns a vector representing the steady-state enzyme production
	 * (concentration / time) everywhere in the system.
	 * 
	 */
	public Vector getProduction() {
		Vector v = new DenseVector(p.N());
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				
				// See algorithm write-up for coordinate system
				int i = y * p.W() + x;
				
				double produced = lattice[x][y].getProduction();
				v.set(i, produced);
			}
		}
		
		return v;
	}
	
	/**
	 * Returns a matrix representing steady-state nutrient uptake
	 * (
	 * @return
	 */
	public Matrix getUptake() {
		Matrix a = new BandMatrix(p.N(), 0, 0);
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				
				// See algorithm write-up for coordinate system
				int i = y * p.W() + x;
				
				if (lattice[x][y].getType() != AbstractCell.EMPTY)
					a.set(i, i, p.getBenefit());
			}
		}
		return a;
	}

	/**
	 * Returns an array of how many of each cell type there are. This is an
	 * O(n) operation. The index of the array corresponds to the cell type.
	 */
	public int[] getCounts() {
		int[] counts = new int[4];
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				int i = lattice[x][y].getType();
				counts[i]++;
			}
		}
		
		return counts;
	}
	
	/**
	 * Returns the difference in mean growth rate between cheaters and cooperators.
	 * Positive indicates a net growth rate advantage for cheaters, and negative
	 * indicates a net growth rate advantage for cheaters.
	 * 
	 * @return a 2D double array. The first element is the absolute probability of
	 * each type attacking the other, and the second is the mean per-cell probability
	 * difference. In neutral drift, the first should just reflect the difference in
	 * cell count at the frontier, while the second should be zero. 
	 */
	public Double[] getFrontierGrowthRate() {
		double coopGrowth = 0D;
		double cheatGrowth = 0D;
		
		// Number of frontier individuals
		double numCheaters = 0D;
		double numCooperators = 0D;
		
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				double coef = 0D;
				
				byte selfType = manager.get(x, y).getType();
				
				if (selfType != AbstractCell.CHEATER && selfType != AbstractCell.COOPERATOR)
					continue;
				
				// Determine weighting based on probability of replacing a competitor.
				// If the cell is certain to replace a competitor, it gets full weighting;
				// if it can't replace a competitor, it does not receive any weighting.
				if (selfType != manager.get(x + 1, y).getType())
					coef += 0.25D;

				if (selfType != manager.get(x - 1, y).getType())
					coef += 0.25D;

				if (selfType != manager.get(x, y + 1).getType())
					coef += 0.25D;

				if (selfType != manager.get(x, y - 1).getType())
					coef += 0.25D;
 
				
				double growth = manager.get(x, y).getOldDerivative();
				
				double weightedGrowth = growth * coef;
				
				if (weightedGrowth < p.epsilon()) 
				// Assign it to the right category
				if (selfType == AbstractCell.CHEATER && weightedGrowth > p.epsilon()) {
					cheatGrowth += growth * coef;
				} else if (selfType == AbstractCell.COOPERATOR && weightedGrowth > p.epsilon()) {
					coopGrowth += growth * coef;
				// We said "continue" above, so if we ever get here, there's a typo or bug
				}
				if (selfType == AbstractCell.CHEATER && coef > 0D)
					numCheaters += 1D;
				
				else if (selfType == AbstractCell.COOPERATOR && coef > 0D)
					numCooperators += 1D;
			}
		}
		
		Double globalDelta = coopGrowth - cheatGrowth;
		
		
		Double cellDelta = (coopGrowth / numCooperators) - (cheatGrowth / numCheaters);

		//System.out.println("A\t" + numCooperators + "\t" + coopGrowth  + "\t" +  numCheaters + "\t" + cheatGrowth + "\t" + cellDelta + "\t" + globalDelta);
		
		// Sometimes, floating point errors cause non-zero values for the deltas even
		// when they are perfectly matched. In these cases, the answer will be below
		// machine epsilon, but they will confuse the user. Hence, set answers below
		// machine epsilon to zero.
		
		if (Math.abs(globalDelta) < p.epsilon())
			globalDelta = 0D;
		
		if (Math.abs(cellDelta) < p.epsilon())
			cellDelta = 0D;

		//System.out.println("B\t" + numCooperators + "\t" + coopGrowth  + "\t" +  numCheaters + "\t" + cheatGrowth + "\t" + cellDelta + "\t" + globalDelta);
		
		// Sanity check: global delta should never be greater than machine epsilon
		// in neutral cases.
		if ((p.getProduction() < p.epsilon()) && (globalDelta > p.epsilon()))
			System.err.println("Failed consistency check: non-zero fitness delta" + globalDelta + " in neutral case. RANDOM_SEED=" + p.getRandomSeed());
		
		return new Double[] {globalDelta, cellDelta};
	}
}
