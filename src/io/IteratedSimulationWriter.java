package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import no.uib.cipr.matrix.Vector;
import operations.processes.AbstractLifeCycle;
import structures.cell.AbstractCell;
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
 * A condensed data representation of a simulation run in
 * multiple replicates. It only retains certain aggregate
 * metrics, and only writes to disk after the last simulation
 * has been completed, greatly reducing I/O dependence.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class IteratedSimulationWriter {

	private Parameters p;
	
	// We track ensemble sums, and the number of simulations still
	// running, rather than just an average. This allows us to take
	// into consideration systematic error caused by fixation events.
	
	// Ensemble sum global growth rate differential. We expect this to be zero
	// for neutral cases. See AbstractLifeCycle for details.
	private double[] popDeltas;
	
	// Ensemble sum per-cell growth rate differential. We only expect this to
	// be zero in neutral cases if there are exactly the same number of each
	// cell type at the frontier. If there is 1 invading cheater with a 100% 
	// chance of replacing a resident cooperator, and four frontier residents 
	// each with a 25% chance of replacing the invader, then the delta is 
	// 0.25 - 1.0 = -0.75. See AbstractLifeCycle for details.
	private double[] cellDeltas;
	
	// Ensemble sum cheater count
	private int[] cheaterCount;

	// Number of simulations still running at time step  i
	private int[] stillRunning;
	
	// Maximum cheater histogram
	private int[] cheaterCounts;
	
	// Cheater fixation time histogram
	private int[] cheaterFixations;

	// Cooperator fixation time histogram
	private int[] cooperatorFixations;
	
	// Ensemble average frontier length as a function of time (cooperators + cheaters)
	private int[] frontiers;
	
	// Ensemble average probability of cheater growth (i.e., probability of ANY cheater
	// replacing ANY cooperator in the next step)
	private double[] cheaterGrowthProbs;
	
	// Time step in the current simulation.
	private Integer step;
	
	// Index of the current simulation. (First simulation is 0, etc.)
	private Integer sim;
	
	// The maximum number of cheaters observed in THIS run of the simulation.
	private Integer localMaxCheat;
	
	// Timestamp for project
	private Date date = new Date();
	
	/**
	 *  Only initialize data structures at construction time.
	 *  Files are initialized and written at close.
	 */
	public IteratedSimulationWriter(Parameters p) {
		
		this.p = p;
		
		// The following can all range from 0 to the maximum time step.
		cellDeltas = new double[p.maxTimeStep() + 1];
		popDeltas = new double[p.maxTimeStep() + 1];
		cheaterGrowthProbs = new double[p.maxTimeStep() + 1];
		cheaterCount = new int[p.maxTimeStep() + 1];
		stillRunning = new int[p.maxTimeStep() + 1];
		cheaterFixations = new int[p.maxTimeStep() + 1];
		cooperatorFixations = new int[p.maxTimeStep() + 1];
		frontiers = new int[p.maxTimeStep() + 1];

		
		// There is a maximum of N cheaters, where N = H * W.
		cheaterCounts = new int[p.N() + 1];
		
		// I was intializing these because I was using the wrapper classes, which
		// don't automatically initialize to zero (because they start out as null
		// pointers). Then I realized that this was at least doubling the memory
		// footprint of these arrays, since both a header and the data must be
		// stored, and Java rounds its storage requirements for objects. So
		// maybe I can delete these initializations.
		for (int i = 0; i <= p.maxTimeStep(); i++) {
			cellDeltas[i] = 0D;
			popDeltas[i] = 0D;
			cheaterCount[i] = 0;
			stillRunning[i] = 0;
			cheaterFixations[i] = 0;
		}
		
		for (int i = 0; i <= p.N(); i++) {
			cheaterCounts[i] = 0;
		}
		
		step = 0;
		sim = 0;
		localMaxCheat = -1;
	}

	public void push(Vector template, AbstractLifeCycle ca, int frontier, double cheatGrowProbability) {

		int count = ca.getCounts()[AbstractCell.CHEATER];
		
		Double[] rates = ca.getFrontierGrowthRate();
		
		// The "push" method is called whether or not one of the cell types
		// has gone to fixation. (The cell update process is also called after
		// "push" has completed.) Hence, it is possible that one of the cell types
		// no longer exists. If this is the case, then the deltas are not well-
		// defined for this instance, so exclude it from the ensemble averages.
		if (!rates[1].equals(Double.NaN)) {
			popDeltas[step] += rates[0];
			cellDeltas[step] += rates[1];
			frontiers[step] += frontier;
			cheaterGrowthProbs[step] += cheatGrowProbability;
			
			//System.out.println(rates[0] + "\t" + rates[1] + "\t" + cellDeltas[step] + "\t" + popDeltas[step]);
		}
		
		cheaterCount[step] += count;
		
		stillRunning[step] += 1;
		
		if (localMaxCheat < count)
			localMaxCheat = count;
		
		step++;
	}

	public void concludeTrial(byte fixationType) {
		cheaterCounts[localMaxCheat] += 1;
		
		if (fixationType == AbstractCell.CHEATER)
			cheaterFixations[step] += 1;
		else if (fixationType == AbstractCell.COOPERATOR)
			cooperatorFixations[step] += 1;
		else
			System.err.println("Simulation ended with no equilibrium or undefined equilibrium state.");
		
		step = 0;
		sim++;
		localMaxCheat = -1;
		
	}

	public void close() {
		if (localMaxCheat != -1)
			System.err.println("Shutting down before the end of a simulation. An error may have occurred."); 

		System.out.println("Finalizing aggregate state data.");
		String simPath;
		
		if (p.isStamp()) {
			// Create a directory for the current time. Also, if it
			// does not exist, create a directory for the current date.
			//simPath = stateDir + date() + "/n=" + n;
			simPath = p.getRootPath() + date();
		} else {
			simPath = p.getRootPath();
		}

		mkDir(simPath, true);
		
		System.out.println(simPath);
		

		writeTimeHistograms(simPath);
		
		writeCountHistogram(simPath);
		
		// If we're not in "sparse" mode, this has already been done
		if (p.getOutput().equalsIgnoreCase("SPARSE") || p.getOutput().equalsIgnoreCase("MINIMAL"))
			writeParameters(simPath);
	}

	private void writeParameters(String simPath) {
		try {
			String paramsFileStr = simPath + '/' + "params.txt";			
			File paramsFile = new File(paramsFileStr);
			FileWriter fw = new FileWriter(paramsFile);
			BufferedWriter bwp = new BufferedWriter(fw);
			bwp.write(p.toString());
			bwp.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void writeCountHistogram(String simPath) {
		try {
			String fn = simPath + '/' + "countHisto.txt";
			File fh = new File(fn);
			FileWriter fw = new FileWriter(fh);
			BufferedWriter bw = new BufferedWriter(fw);		
			bw.write("Max Cheat\tCount\n");
			
			for (Integer n = 0; n <= p.N(); n++) {
				// Max cheat
				bw.write(n.toString());
				bw.write('\t');
				
				bw.write(String.valueOf(cheaterCounts[n]));
				bw.write('\n');
			}
			bw.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void writeTimeHistograms(String simPath) {
		try {
			String fn = simPath + '/' + "timeHistos.txt";
			File fh = new File(fn);
			FileWriter fw = new FileWriter(fh);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write("Step\tStill running\tCheater fixation events\tCoop fixation events\tAv cheater count\tAv pop delta\tAv cell delta\tAv Frontier Cells\tAv Cheat Growth Prb\n");
			for (Integer t = 0; t <= p.maxTimeStep(); t++) {
				// Time step
				bw.write(t.toString());
				bw.write('\t');
				
				// Simulations still running
				bw.write(String.valueOf(stillRunning[t]));
				bw.write('\t');
				
				// Fixation time histogram
				bw.write(String.valueOf(cheaterFixations[t]));
				bw.write('\t');

				// Fixation time histogram
				bw.write(String.valueOf(cooperatorFixations[t]));
				bw.write('\t');
								
				// The "push" method is called whether or not a cell type has gone to fixation. In cases where
				// one cell type has gone to fixation, the deltas are not well-defined and we exclude them from
				// the running totals. (See the note in the "push" method.) As a result, we use the
				// number of instances still running in the NEXT time step to normalize the deltas, but not
				// for the ensemble average cheater fraction. If we didn't, we'd be dividing the ensemble total
				// by the wrong number of instances.
				
				if (t < p.maxTimeStep() && stillRunning[t + 1] > 0) {
					// Cheater count: INCLUDE the instances that went to fixation (i.e., use 't')
					Double cheatCount = Double.valueOf(cheaterCount[t]) / Double.valueOf(stillRunning[t] );				
					bw.write(cheatCount.toString());
					bw.write('\t');
					
					// All metrics based on the frontier length must EXCLUDE fixations (use 't+1')
					
					// Growth rate differential: EXCLUDE fixations
					Double popDelta = popDeltas[t] / Double.valueOf(stillRunning[t + 1]);				
					bw.write(popDelta.toString());
					bw.write('\t');
					
					// Per-cell rate differential: EXCLUDE fixations
					Double cellDelta = cellDeltas[t] / Double.valueOf(stillRunning[t + 1]);				
					bw.write(cellDelta.toString());
					bw.write('\t');
					
					// Mean frontier length: EXCLUDE fixations
					Integer frontier = frontiers[t] / Integer.valueOf(stillRunning[t + 1]);				
					bw.write(frontier.toString());
					bw.write('\t');

					// Cheater growth probability: EXCLUDE fixations
					Double cgp = cheaterGrowthProbs[t] / Double.valueOf(stillRunning[t + 1]);				
					bw.write(cgp.toString());
					bw.write('\n');

				} else {
					bw.write("0.0\t0.0\t0.0\t0.0\t0.0\n");
				}
			}
			bw.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private String date() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd/HH'h'mm'm'ss's'");
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}
	
	private void mkDir(String pathStr, boolean recursive) {
		File path = new File(pathStr);
		if (!path.exists()) {
			try {
				if (recursive)
					path.mkdirs();
				else
					path.mkdir();
			} catch (Exception ex) {
				System.out.println("Could not create directory tree " + pathStr);
				throw new RuntimeException(ex);
			}			
		}
	}
}
