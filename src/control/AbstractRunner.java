package control;

import java.io.IOException;

import io.FixationTimeWriter;
import io.IteratedSimulationWriter;
import io.PhaseWriter;
import io.RdfWriter;
import operations.Simulator;
import structures.distributions.AbstractPointDistribution;
import structures.distributions.ZeroOriginDistribution;
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
 * Runs the simulation, possibly multiple times depending on the parameters.
 * 
 * Performs prefactor steps only once, even if simulation is to be run
 * several times.
 * 
 * @author dbborens@princeton.edu
 *
 */
public abstract class AbstractRunner {

	protected static void run(Parameters p) throws IOException {
		
		AbstractPointDistribution dist = new ZeroOriginDistribution(p);
		FixationTimeWriter ftw = new FixationTimeWriter(p);
		
		IteratedSimulationWriter isw;
		if (!p.getOutput().equalsIgnoreCase("MINIMAL"))
			isw = new IteratedSimulationWriter(p);
		else
			isw = null;
		
		RdfWriter rdf;
		
		if (p.getHaltCount() != -1)
			rdf = new RdfWriter(p);
		else
			rdf = null;
		
		PhaseWriter pw = new PhaseWriter(p);
		
		for (int i = 0; i < p.getReplicates(); i++) {
	
			Simulator solver = new Simulator(p, dist, isw, ftw, rdf, pw);
			String simPath = solver.getSimPath();
			
			if (p.getOutput().equalsIgnoreCase("FULL"))
				new Visualizer(simPath);
	
			// NOTE: Even if the random seed is set to '*' in the parameters,
			// all replicates beyond the first one will receive new random seeds.
			if (p.getReplicates() > 1) {
				p.nextReplicate();
			}
		}
		
		if (!p.getOutput().equalsIgnoreCase("MINIMAL"))
			isw.close();
		
		if (p.getHaltCount() != -1)
			rdf.close();
		
		ftw.close();
		pw.close();
		System.out.println("Done.");
	}
	
}
