package operations.solvers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import structures.distributions.AbstractPointDistribution;
import control.parameters.Parameters;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

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
public class PositiveSuperpositionSolver extends AbstractRDSolver {

	// Distribution underlying the solver.
	protected AbstractPointDistribution dist;
	
	public PositiveSuperpositionSolver(Parameters p, AbstractPointDistribution dist) {
		super(p, dist);
		this.dist = dist;		
	}
	
	protected void writeDistribution() {
		File distFile = new File(p.getPath() + "distribution.txt");
		File underFile = new File(p.getPath() + "underlying.txt");
		
		System.out.println("Writing distribution to " + distFile.getName());
		
		try {
			FileWriter fw = new FileWriter(distFile);
			FileWriter uw = new FileWriter(underFile);
			BufferedWriter bw = new BufferedWriter(fw);
			BufferedWriter ub = new BufferedWriter(uw);
			
			for (int i = 0; i < p.N(); i++) {
				Double a = getContribution(i, dist.origin());
				Double b = getContribution(dist.origin(), i);
				
				if (a != b)
					throw new IllegalStateException();
				
				String toAppend = a.toString();
				bw.append(toAppend);
				bw.append('\t');
				
				ub.append(dist.getSolution()[i].toString());
				ub.append('\t');
				
				if (i > 0 && i % p.W() == 0) {
					bw.append('\n');
					ub.append('\n');
				}
			}
			ub.close();
			bw.close();
		}catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
	}


	@Override
	/**
	 * Find the set of all cooperators in the system. For each
	 * coordinate in the system, add the contribution from each
	 * cooperator. We assume that the size of the system greatly
	 * exceeds the length scale of the solute diffusion (i.e.,
	 * that solute from one cooperator goes below machine epsilon
	 * before wrapping around the periodic boundary conditions)
	 * and therefore only factor in the contribution over the
	 * shortest distance between each source cell and a given
	 * position.
	 *  
	 */
	public Vector solve(Vector source) {
		// Get list of producer coordinates
		List<Integer> producers = findProducers(source);
		
		return solve(producers);
	}

	public Vector solve(List<Integer> producers) {
		Vector result = new DenseVector(p.N());

		int m = producers.size();
		
		// Consider each coordinate in the system (whether
		// cooperator, cheater, dead or empty).
		for (int i = 0; i < p.N(); i++) {

			
			Double c = 0.0;
			// Consider each producer.
			for (int j = 0; j < m; j++) {				
				
				// Add contribution from that producer.
				c += getContribution(i, producers.get(j));
			}
			
			result.set(i, c);
		}
		
		return result;
	}
	

	protected Double getContribution(int i, int k) {
		int dx = xOffset(i, k);
		int dy = yOffset(i, k);
		
		return dist.get(dx, dy);		
	}


	/**
	 * Find shorter offset in the y direction from y0 to
	 * y1 given periodic boundary conditions. Since
	 * the distribution is symmetric about the source,
	 * we don't care about the sign.
	 * 
	 * @param i
	 * @param k
	 * @return
	 */
	protected int yOffset(int source, int target) {
		int ys = source / p.W();
		int yt = target / p.W();

		return ys - yt;

	}

	/**
	 * Find shorter offset in the x direction from x0 to
	 * x1 given periodic boundary conditions. Since the
	 * distribution is symmetric about the source, we don't
	 * care about the sign.
	 * 
	 */
	protected int xOffset(int source, int target) {
		int xs = source % p.W();
		int xt = target % p.W();

		return xs - xt;
	}

	/**
	 * Produce a list of all producer coordinates. Assumes
	 * that all producers release the same quantity of solute.
	 * 
	 * @return
	 */
	private ArrayList<Integer> findProducers(Vector source) {
		ArrayList<Integer> producers = new ArrayList<Integer>(p.N() / 2);
		
		
		for (int i = 0; i < p.N(); i++) {
			if (source.get(i) != 0)
				producers.add(i);
		}

		return producers;
	}

	/**
	 * Returns the distribution underlying this solver.
	 */
	public AbstractPointDistribution getDistribution() {
		return dist;
	}

	@Override
	public double getSourceConcentration() {
		return dist.get(0, 0);
	}
}
