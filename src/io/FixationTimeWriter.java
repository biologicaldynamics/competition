package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import structures.cell.AbstractCell;

import control.parameters.Parameters;

/**
 *
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
 * 
 * Minimal state writer that records only the random 
 * seed and the number of time steps to each type of
 * fixation.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class FixationTimeWriter {
	private BufferedWriter cbw;
	private BufferedWriter pbw;
	private Parameters p;
	
	public FixationTimeWriter(Parameters p) {
		this.p = p;
		try {
			mkDir();
			String cheatFixFileName = p.getRootPath() + "/cheat.fix.txt";
			File cheatFixFile = new File(cheatFixFileName);
			FileWriter cfw = new FileWriter(cheatFixFile);
			cbw = new BufferedWriter(cfw);
			
			String prodFixFileName = p.getRootPath() + "/prod.fix.txt";
			File prodFixFile = new File(prodFixFileName);
			FileWriter pfw = new FileWriter(prodFixFile);
			pbw = new BufferedWriter(pfw);
			
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Record a fixation event.
	 * 
	 * @param fixationType 	Blue or red fixation?
	 * @param randomSeed	Random number generator (to allow repeatability using same code base)
	 * @param steps			Number of simulation time steps until fixation
	 * @param time			Natural time units to fixation, based on Doob-Gillespie algorithm
	 */
	public void push(byte fixationType, long randomSeed, int steps, double gillespieTime) {
		StringBuilder sb = new StringBuilder();
		sb.append(randomSeed);
		sb.append('\t');
		sb.append(steps);
		sb.append('\t');
		sb.append(gillespieTime);
		sb.append('\n');
		try {
			if (fixationType == AbstractCell.CHEATER)
				cbw.write(sb.toString());
			else if (fixationType == AbstractCell.COOPERATOR)
				pbw.write(sb.toString());
			else
				throw new IllegalStateException("Fixation writer called without fixation event!");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}		
	}
	
	public void close() {
		try {
			cbw.close();
			pbw.close();
			writeParameters();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void writeParameters() {
		try {
			String paramsFileStr = p.getRootPath() + '/' + "params.txt";			
			File paramsFile = new File(paramsFileStr);
			FileWriter fw = new FileWriter(paramsFile);
			BufferedWriter bwp = new BufferedWriter(fw);
			bwp.write(p.toString());
			bwp.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void mkDir() {
		File path = new File(p.getRootPath());
		if (!path.exists()) {
			try {
				path.mkdirs();
			} catch (Exception ex) {
				throw new RuntimeException("Could not create directory tree " + p.getRootPath());
			}			
		}
	}
}
