package operations;

import control.EquilibriumException;
import control.RdfHaltException;
import control.parameters.Parameters;

import io.BufferedStateWriter;
import io.FixationTimeWriter;
import io.IteratedSimulationWriter;
import io.PhaseWriter;
import io.RdfWriter;
import structures.cell.AbstractCell;
import structures.cell.ic.*;
import structures.distributions.AbstractPointDistribution;
import structures.distributions.ZeroOriginDistribution;
import no.uib.cipr.matrix.*;
import operations.processes.*;
import operations.solvers.*;

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
 * Control class for forward integration in time. Constructs
 * an initial condition, and then iterates it forward by
 * repeatedly performing cell divisions and updating the
 * catalyst distribution until either equilibrium is reached
 * or the maximum number of cycles have been performed.
 *
 */
public class Simulator {

	private AbstractRDSolver solver;
	private String simPath;
	private double sourceConcentration;
	
	public Simulator(Parameters p, IteratedSimulationWriter isw, FixationTimeWriter ftw,
			RdfWriter rdf, PhaseWriter pw) {
		
		this(p, new ZeroOriginDistribution(p), isw, ftw, rdf, pw);
	}
	
	public Simulator(Parameters p, AbstractPointDistribution dist, IteratedSimulationWriter isw,
			FixationTimeWriter ftw, RdfWriter rdf, PhaseWriter pw) {
		
		InitialCondition ic = makeInitialCondition(p);
		
		
		//solver = new MatrixSolver(p, dist);
		//solver = new PositiveSuperpositionSolver(p, dist);
		//solver = new NegativeSuperpositionSolver(p, dist);
		//solver = new SmartSuperpositionSolver(p, dist);
		solver = new IterativeSmartSolver(p, dist);
		
		sourceConcentration = solver.getSourceConcentration();
		
		
		//System.exit(0);
		AbstractLifeCycle ca = makeLifeCycle(p, ic);

		BufferedStateWriter bsw = null;

		try {
			
			if (p.getOutput().equalsIgnoreCase("SPARSE") || p.getOutput().equalsIgnoreCase("MINIMAL")) {
				bsw = null;
				simPath = null;
			} else {
				bsw = new BufferedStateWriter(p);
				simPath = bsw.getSimPath();
			}
			// Push the initial condition to the state writer
			Vector template = new DenseVector(p.N());
			
			for (int i = 0; i < p.maxTimeStep(); i++) {
				//long start = System.nanoTime();
				try {
					Vector c = iterate(p, ca, isw, bsw, pw, template);
					template = c.copy();
				} catch (EquilibriumException e) {

					if (!(p.getOutput().equalsIgnoreCase("SPARSE") || p.getOutput().equalsIgnoreCase("MINIMAL")))
						System.out.println("System reached equilibrium.");

					if (e.getFixationType() != AbstractCell.OTHER)
						ftw.push(e.getFixationType(), p.getRandomSeed(), i, e.getGillespieTime());
					
					conclude(p, isw, bsw, e.getFixationType());
					return;
				} catch (RdfHaltException e) {
					if (!(p.getOutput().equalsIgnoreCase("SPARSE") || p.getOutput().equalsIgnoreCase("MINIMAL")))
						System.out.println("System halted for RDF.");
					
					rdf.push(ca, e.getParticles());
					
					conclude(p, isw, bsw, AbstractCell.OTHER);
					return;
				}
				
				//long finish = System.nanoTime();
				//long microseconds = (finish - start) / 1000;
				
				//if (i % 10 == 0)
				//	System.out.println(microseconds);
			}
			

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Something went horribly wrong! Attempting to shut down gracefully...");
			if (!(p.getOutput().equalsIgnoreCase("SPARSE") || p.getOutput().equalsIgnoreCase("MINIMAL")))
				bsw.close();
			if (!p.getOutput().equalsIgnoreCase("MINIMAL"))
				isw.close();
			
			rdf.close();
			
			ftw.close();
			pw.close();
			
			throw new RuntimeException(ex);
		}
		
		conclude(p, isw, bsw, AbstractCell.OTHER);
	}

	private void conclude(Parameters p, IteratedSimulationWriter isw,
			BufferedStateWriter bsw, byte fixationType) {
		
		if (!(p.getOutput().equalsIgnoreCase("SPARSE") || p.getOutput().equalsIgnoreCase("MINIMAL")))
			bsw.close();
		
		if (!(p.getOutput().equalsIgnoreCase("MINIMAL")))
			isw.concludeTrial(fixationType);
	}

	private AbstractLifeCycle makeLifeCycle(Parameters p, InitialCondition ic) {
		if (p.getCellOperator().equals("ThresholdReplacement"))
			return  new ThresholdReplacement(p, ic);
		else if (p.getCellOperator().equals("ThresholdDivision"))
			return new ThresholdDivision(p, ic);
		else if (p.getCellOperator().equals("ContinuousReplacement"))
			return new ContinuousReplacement(p, ic);
		else if (p.getCellOperator().equals("ContinuousDivision"))
			return new ContinuousDivision(p, ic);
		else if (p.getCellOperator().equals("ZeroBaselineReplacement"))
			return new ZeroBaselineReplacement(p, ic, sourceConcentration);
		else
			throw new IllegalArgumentException("Unrecognized cell operator " + p.getCellOperator());
	}

	private InitialCondition makeInitialCondition(Parameters p) {
		if (p.getIC().equals("ProducerCheater")) {
			// One cheater in a 5x5 block of cooperators, with the rest
			// of the space empty
			return new ProducerCheater(p);
			
		} else if (p.getIC().equals("ProducerCheaterClump")) {
			// A 3x3 block of cheaters inside of a 9x9 block of cooperators.
			// Hence, a total of 9 cheaters and 72 cooperators. The rest
			// of the space is empty.
			return new ProducerCheaterClump(p);
			
		} else if (p.getIC().equals("TwoCities")) {
			// A single cheater on one side and a cooperator on the other, 
			// placed as far apart as possible on a horizontal line.
			return new TwoCities(p);
			
		} else if (p.getIC().equals("WellMixed")) {
			// A space-filling population consisting of cooperators and
			// cheaters in user-specified proportions.
			return new WellMixed(p);
			
		} else if (p.getIC().equals("SingleInvader")) {
			// Actually just a special case of "WellMixed," except the
			// invader is at the center of the simulation for convenience
			return new SingleProducer(p);
			
		} else if (p.getIC().equals("CheaterDisc")) {
			// A disc of cheaters with specified radius in a field of producers.
			return new CheaterDisc(p);
			
		} else if (p.getIC().equals("ProducerDisc")) {
			// A disc of producers with specified radius in a field of cheaters.
			return new ProducerDisc(p);
			
		} else if (p.getIC().equals("TwoDomains")) {
			// A disc of producers with specified radius in a field of cheaters.
			return new TwoDomains(p);
			
		} else
			throw new IllegalArgumentException("Unrecognized IC " + p.getIC());
	}
	
	public String getSimPath() {
		return simPath;
	}


	private Vector iterate(Parameters p, AbstractLifeCycle ca, IteratedSimulationWriter isw, 
			BufferedStateWriter bsw, PhaseWriter pw, Vector template) throws EquilibriumException, RdfHaltException {

		Vector c = solver.solve(ca.getProduction());
		
		ca.turnover(c, bsw, isw, pw);
		
		return c;
	}


	
}
