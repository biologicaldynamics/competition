package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

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
 * Writes the probability of cheater advancement as a
 * function of current cheater count for every time step
 * in every instance of the simulation. In other words,
 * gives C vs dC/dt.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class PhaseWriter {

	private Parameters p;
	
	// Cumulative probability of cheater growth over all observations.
	private double[] sumPcg; 			// based on cheater count
	
	// Number of times C cheaters were observed.
	private double[] instances;			// based on cheater count
	
	// PCG as a function of frontier / (# dominant type)
	private HashMap<Double, Double> cPcg;
	private HashMap<Double, Double> cInstances;
	
	public PhaseWriter(Parameters p) {
		this.p = p;
		sumPcg = new double[p.N()+1];
		instances = new double[p.N()+1];

		
		cPcg = new HashMap<Double, Double>();
		cInstances = new HashMap<Double, Double>();
		
	}
	
	public void push(int cheaterCount, int frontierCount, double pcg) {
		instances[cheaterCount]++;		
		sumPcg[cheaterCount] += pcg; 
		
		double denominator;
		
		// No divide by zero
		if (cheaterCount == 0)
			return;
		
		if (cheaterCount <= (p.N() / 2))
			denominator = -1D * cheaterCount;
		else
			denominator = ((p.N() * 1.0D) - cheaterCount);
		
		//if (cheaterCount == p.N() - 1)
		//	System.out.println("At last cheater. denominator=" + denominator + " and frontierCount is" + frontierCount);
		double curvature;
		if (denominator == 0)
			curvature = 0;
		else
			curvature = (frontierCount * 1D) / denominator;
		if (! cPcg.containsKey(curvature))
			cPcg.put(curvature, 0D);
		
		if (! cInstances.containsKey(curvature))
			cInstances.put(curvature, 0D);

		cPcg.put(curvature, cPcg.get(curvature) + pcg);
		cInstances.put(curvature, cInstances.get(curvature) + 1);
	}
	
	public void close() {
		makePhaseFile();
		makeCurvatureFile();
	}

	private void makePhaseFile() {
		try {
			mkDir();
			String fileName = p.getRootPath() + "/phase.txt";
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			
			for (int c = 1; c <= p.N(); c++) {
				double pcg = sumPcg[c] / instances[c];
				long lInstances = Math.round(instances[c]);
				
				StringBuilder sb = new StringBuilder();
				sb.append(c);
				sb.append('\t');
				sb.append(sumPcg[c]);
				sb.append('\t');
				sb.append(pcg);
				sb.append('\t');
				sb.append(lInstances);
				sb.append('\n');
				bw.write(sb.toString());
			}
			bw.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	

	private void makeCurvatureFile() {
		try {
			mkDir();
			String fileName = p.getRootPath() + "/curvature.txt";
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			
			SortedSet<Double> keys = new TreeSet<Double>(cInstances.keySet());
			
			Iterator<Double> i = keys.iterator();
			
			while (i.hasNext()) {
				Double key = i.next();
				
				if (key == Double.NaN)
					continue;
				
				double ttlPcg = cPcg.get(key);
				double instances = cInstances.get(key);
				
				double pcg = ttlPcg / instances;
				long lInstances = Math.round(instances);
				
				StringBuilder sb = new StringBuilder();
				sb.append(key);
				sb.append('\t');
				sb.append(pcg);
				sb.append('\t');
				sb.append(lInstances);
				sb.append('\n');
				bw.write(sb.toString());
			}
			bw.close();
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
