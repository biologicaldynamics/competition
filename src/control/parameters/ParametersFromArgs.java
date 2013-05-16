package control.parameters;

/**
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
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class ParametersFromArgs extends Parameters {

	public ParametersFromArgs(String[] args) {
		HashMap<String, String> r = tokenize(args);
		
		dx = Double.valueOf(r.get("dx"));
		width = Integer.valueOf(r.get("W"));
		path = r.get("PATH");
		stamp = Boolean.valueOf(r.get("STAMP"));
		ic = r.get("IC");
		icArgument = Integer.valueOf(r.get("IC_ARGUMENT"));
		randomizeCheaters = Boolean.valueOf(r.get("RANDOMIZE_CHEATERS"));
		randomizeCooperators = Boolean.valueOf(r.get("RANDOMIZE_COOPERATORS"));	
		infiniteGamma = Boolean.valueOf(r.get("INFINITE_GAMMA"));
		cellOperator = r.get("CELL_OPERATOR");
		maxTimeStep = Integer.valueOf(r.get("MAX_TIME_STEP"));
		output = r.get("OUTPUT");
		dimension = width * width;
		assignRandom(r);
		stringRepresentation = displayForm(r);
		haltCount = Integer.valueOf(r.get("HALT_COUNT"));		
		replicates = Integer.valueOf(r.get("REPLICATES"));
		calc_dt(r);
		
		// This gets the rest of the parameters also
		scaleParameters(r);
		
		validateAndInit();

	}

	private void assignRandom(HashMap<String, String> r) {
		if (r.get("RANDOM_SEED").equals("*")) {
			randomSeed = System.currentTimeMillis();
			random = new Random(randomSeed);
			
			// This is cloodgy, but...
			String rsStr = ((Long) randomSeed).toString();
			r.put("RANDOM_SEED", rsStr);
		} else {
			randomSeed = Long.valueOf(r.get("RANDOM_SEED"));
			random = new Random(randomSeed);
			
		}
	}
	
	private void calc_dt(HashMap<String, String> r) {
		double d = Double.valueOf(r.get("DIFFUSION"));
		double max_r = Double.valueOf(r.get("MAX_R"));
		dt = (max_r * Math.pow(dx, 2)) / d;
	}
	
	private HashMap<String, String> tokenize(String[] args) {
		HashMap<String, String> r = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			String[] tuple = args[i].trim().split("=");
			if (tuple.length != 2)
				throw new IllegalArgumentException("Unrecognized token " + args[i]);
			
			
			r.put(tuple[0], tuple[1]);
		}
		
		return r;
	}
	
	public String displayForm(HashMap<String, String> params) {
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> i = params.keySet().iterator();
		while (i.hasNext()) {
			String s = i.next();
			sb.append(s);
			sb.append('=');
			sb.append(params.get(s));
			sb.append('\n');			
		}
		
		return sb.toString();
	}
	
	/**
	 * The parameters are provided in units of seconds and microns. However, depending
	 * on the spatial and temporal resolution of the simulation, the effective parameters
	 * may be different.
	 * 
	 * If the time step is dt=0.5 sec, then quantities that vary like 1/time should be twice
	 * as big, and quantities that vary with time should be half as big. So we are scaling
	 * by the RECIPROCAL of the step size, because that's how many steps fit into the base
	 * unit. (I'm sure this is obvious to everyone but me.)
	 * 
	 * This method recalculates these values and stores them in primitives for fast lookup.
	 */
	private void scaleParameters(HashMap<String, String> r) {
		double dx_squared = Math.pow(dx, 2d);
		
		// Diffusion constants are in units of length^2/time, so scale them by dt/(dx)^2:
		diffusion = Double.valueOf(r.get("DIFFUSION")) * dt / dx_squared;
		
		// Enzyme decays at a rate of 1/time, so scale by dt:
		decay = Double.valueOf(r.get("DECAY")) * dt;
		
		// If cells each take up one square (dx^2), then nutrient uptake proceeds at a rate of
		// 1 / time. So we scale it by dt:
		benefit = Double.valueOf(r.get("BENEFIT")) * dt;

		// Enzyme production is in units of concentration/time = mol / (area * time), so scale by
		// dt * (dx)^2.
		production = Double.valueOf(r.get("PRODUCTION")) * dt * dx_squared;

		// Basal growth/decay rate is in units of concentration/time = mol / (area * time), so scale
		// by dt * (dx)^2.
		growth = Double.valueOf(r.get("GROWTH")) * dt * dx_squared;
		
		// Threshold "biomass" (actually concentration) is given in mol / area. Each cell takes up
		// one square. Hence we scale by dx^2.
		threshold = Double.valueOf(r.get("THRESHOLD")) * dx_squared;
	}
}
