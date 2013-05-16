package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import operations.processes.AbstractLifeCycle;

import no.uib.cipr.matrix.Vector;

import control.parameters.Parameters;

import structures.cell.AbstractCell;
import structures.identifiers.Extrema;
import structures.identifiers.NonZeroExtrema;

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
 * Writes the state of the system to a file. To avoid lots of opening and
 * closing of files, as well as lots of panning around the disk, a single
 * file is used for all state.
 * 
 * Since disk is much slower than compute, in the future I might move this
 * to a separate thread, with a queue of data to be appended. However, since
 * that is a somewhat delicate piece of engineering, I will defer that until
 * I see how much optimizing is needed.
 * 
 * In the meantime, this uses the default Java BufferedWriter, which will
 * block the thread every time it needs to write to disk.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class BufferedStateWriter {

	private static final double log10 = Math.log(10D);

	private double prevGillespie = 0;
	
	private final String STATE_FILENAME = "data.txt";
	private final String PARAMS_FILENAME = "params.txt";
	private final String METADATA_FILENAME = "metadata.txt";
	private final String INTERVAL_FILENAME = "interval.txt";
	// Time step
	int n;
	
	// Parameters
	private Parameters p;
	
	// I/O handle for the state file
	private BufferedWriter stateWriter;
	
	// I/O handle for the interval file (What changed at each time step, and how long it took)
	private BufferedWriter intervalWriter;
	
	// Directory path to the state file
	private String simPath;
	
	// Timestamp for project
	private Date date = new Date();
	
	// Extrema for each field type
	private Extrema ec;				// Enzyme
	private Extrema eb;				// Biomass
	private Extrema ed;				// Change in biomass per unit time
	
	//public BufferedStateWriter(String stateDir, Parameters p, int n) {
	public BufferedStateWriter(Parameters p) {
		this.p = p;
		initExtrema(p);
		
		makeFiles(p);

		initFiles(p);
		
	}

	private void initFiles(Parameters p) {
		// Create the state & interval files
		String stateFileStr = simPath + '/' + STATE_FILENAME;
		String intervalFileStr = simPath + '/' + INTERVAL_FILENAME;
		
		try {
			
			if (p.getOutput().equalsIgnoreCase("FULL")) {
				File stateFile = new File(stateFileStr);
				FileWriter fw = new FileWriter(stateFile);
				stateWriter = new BufferedWriter(fw, 1048576);
			}
			
			File intervalFile = new File(intervalFileStr);
			FileWriter ifw = new FileWriter(intervalFile);
			intervalWriter = new BufferedWriter(ifw, 1048576);
			intervalWriter.append("Step,Coop,Cheat,Running time\n");
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try {
			String paramsFileStr = simPath + '/' + PARAMS_FILENAME;			
			File paramsFile = new File(paramsFileStr);
			FileWriter fw = new FileWriter(paramsFile);
			BufferedWriter bwp = new BufferedWriter(fw);
			bwp.write(p.toString());
			bwp.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void initExtrema(Parameters p) {
		ed = new NonZeroExtrema(p.W());
		ec = new NonZeroExtrema(p.W());
		eb = new Extrema(p.W());
	}

	private void makeFiles(Parameters p) {
		// Create the directory for state files, if needed
		mkDir(p.getPath(), true);
		
		if (p.isStamp()) {
			// Create a directory for the current time. Also, if it
			// does not exist, create a directory for the current date.
			//simPath = stateDir + date() + "/n=" + n;
			simPath = p.getPath() + date();
		} else {
			simPath = p.getPath();
		}
		
		mkDir(simPath, true);
		
		System.out.println(simPath);
	}

	public String getSimPath() {
		return simPath;
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
	
	/**
	 * Appends a state to the file.
	 * 
	 * @param u Undigested nutrient concentration vector.
	 * @param s Digested nutrient concentration vector.
	 * @param c Catalyst concentration vector.
	 * @param seconds Interval (in simulated time) between the last time step and the current one.
	 * @param realInterval Running time (in real time) to compute this time step.
	 * 
	 * @param state
	 */
	public void push(Vector c, AbstractLifeCycle ca, double gillespie) {
		if (p.getOutput().equalsIgnoreCase("FULL") && (prevGillespie == 0 || oom(gillespie) > oom(prevGillespie))) {
			System.out.println("Writing time step " + gillespie);
			writeVector(c, ec, gillespie, "enzyme");
			writeVector(ca.getBiomass(), eb, gillespie, "biomass");
			writeVector(ca.getDerivatives(), ed, gillespie, "dm_dt");
			writeColor(ca, gillespie);
		}
		
		int[] counts = ca.getCounts();
		
		interval(n, gillespie, counts[AbstractCell.COOPERATOR], counts[AbstractCell.CHEATER]);
		prevGillespie = gillespie;
		n++;
	}	
	
	private static int oom(double x) {
		return oom(Math.round(x));
	}
	
	private static int oom(long x) {
		Double log = Math.log(x * 1D) / log10;
		Double floor = Math.floor(log);
		Double oom = Math.pow(10, floor);		

		// We don't want "ceiling" because log(1) = 0
		return oom.intValue();
	}
	
	/**
	 * Wall clock time and simulation time for last time step.
	 * 
	 * @param simInterval
	 * @param realInterval
	 */
	private void interval(int n, double realInterval, int coop, int cheat) {
		StringBuilder sb = new StringBuilder();
		sb.append(n);
		sb.append(',');
		sb.append(coop);
		sb.append(',');
		sb.append(cheat);
		sb.append(',');
		sb.append(realInterval);
		sb.append('\n');
		try {
			intervalWriter.append(sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

	/**
	 * Write out the cell types, represented by colors. 
	 * 
	 * The format for cell color is:
	 *  
	 * >color:0
	 * 0123456701234567....
	 * 7654321076543210....
	 * >color:1
	 * ...
	 * 
	 * Recall that base colors are encoded in binary:
	 *   Red   += 4
	 *   Green += 2
	 *   Blue  += 1
	 *   
	 * Hence, our visualization is limited to 8 cell
	 * types, including null cells (which are black).
	 * 
	 * @param lattice
	 */
	private void writeColor(AbstractLifeCycle ca, double gillespie) {
		StringBuilder sb = new StringBuilder(">color:");
		sb.append(gillespie);
		sb.append('\n');

		for (int y = 0; y < p.W(); y++) {
			for (int x = 0; x < p.W(); x++) {
				sb.append(ca.getTypeAt(x, y));
			}
			sb.append('\n');
		}

		try {
			stateWriter.write(sb.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Writes a vector to the file. The format is as follows:
	 * 
	 * The format is as follows:
	 * 
	 *  >first_field:0
	 *  0.012345	0.012345	0.012345....
	 *  0.012345	0.012345	0.012345....
	 *  >second_field:0
	 *  0.012345	0.012345	0.012345....
	 *  0.012345	0.012345	0.012345....
	 *  >first_field:1
	 *  ...
	 *  
	 *  The delimiters are tabs, and there are p.W() tokens per line.
	 */
	private void writeVector(Vector v, Extrema extrema, double gillespie, String title) {


		StringBuilder sb = new StringBuilder();;
		sb.append('>');
		sb.append(title);
		sb.append(':');
		sb.append(gillespie);
		sb.append("\n");
		for (int i = 0; i < v.size(); i++) {
			Double u = v.get(i);
			extrema.consider(u, i, gillespie);
			sb.append(v.get(i));
			if (i % p.W() == p.W() - 1)
				sb.append('\n');
			else
				sb.append('\t');
		}
		
		try {
			stateWriter.write(sb.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Finalizes the file. Writes a summary file.
	 */
	public void close() {
		// Close the state data file.
		System.out.println("Final Gillespie time: " + prevGillespie);
		try {
			
			if (p.getOutput().equalsIgnoreCase("FULL"))
				stateWriter.close();
			
			intervalWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		if (p.getOutput().equalsIgnoreCase("FULL")) {
			// Write the metadata file.
			try {
				File metadata = new File(simPath + '/' + METADATA_FILENAME);
				FileWriter mfw = new FileWriter(metadata);
				BufferedWriter mbw = new BufferedWriter(mfw);
				
				mbw.write("dm_dt>");
				mbw.write(ed.toString());
				mbw.write('\n');
	
				mbw.write("enzyme>");
				mbw.write(ec.toString());
				mbw.write('\n');
				
				mbw.write("biomass>");
				mbw.write(eb.toString());
	
				mbw.close();
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
