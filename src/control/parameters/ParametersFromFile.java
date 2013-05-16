package control.parameters;

import io.ParameterReader;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * /**
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
 * @author dbborens@princeton.edu
 *
 */
public class ParametersFromFile extends Parameters {
	/** CONSTRUCTORS **/
	public ParametersFromFile (String paramsPath) throws IOException {
		
		System.out.println("Epsilon=" + epsilon);
		File pFile = new File(paramsPath);
		ParameterReader r = new ParameterReader(pFile);
		
		dx = r.get("dx");
		width = r.getWidth();
		randomSeed = r.getSeed();
		stamp = r.isStamp();
		output = r.getOutput();
		ic = r.getIC();
		icArgument = r.getIcArgument();
		randomizeCheaters = r.isRandomizeCheaters();
		randomizeCooperators = r.isRandomizeCooperators();
		infiniteGamma = r.isInfiniteGamma();
		cellOperator = r.getCellOperator();
		maxTimeStep = r.getMaxTimeStep();
		replicates = r.getReplicates();
		dimension = width * width;
		random = new Random(randomSeed);
		path = r.getPath();
		haltCount = r.getHaltCount();
		
		stringRepresentation = r.toString();

		calc_dt(r);
		
		scaleParameters(r);		
		
		validateAndInit();
		
	}
	

	

	/**
	 * It is straightforward to see that, if diffusion in 2D is based on 
	 * concentration from the four cardinal neighbors, then the solution to  
	 * the heatequation in 2D will fail to converge if D * dt / dx^2 > 0.25, 
	 * because in that case more than 100% of the solute in a given area 
	 * diffuses in and out. In practice, we require a much lower limit for
	 * this quantity in order to ensure convergence even with the added
	 * reaction and decay terms.
	 */
	private void calc_dt(ParameterReader r) {
		double d = r.get("DIFFUSION");
		dt = (r.get("MAX_R") * Math.pow(dx, 2)) / d;
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
	private void scaleParameters(ParameterReader r) {
		double dx_squared = Math.pow(dx, 2d);
		
		// Diffusion constants are in units of length^2/time, so scale them by dt/(dx)^2:
		diffusion = r.get("DIFFUSION") * dt / dx_squared;
		
		// Enzyme decays at a rate of 1/time, so scale by dt:
		decay = r.get("DECAY") * dt;
		
		// If cells each take up one square (dx^2), then nutrient uptake proceeds at a rate of
		// 1 / time. So we scale it by dt:
		benefit = r.get("BENEFIT") * dt;

		// Enzyme production is in units of concentration/time = mol / (area * time), so scale by
		// dt * (dx)^2.
		production = r.get("PRODUCTION") * dt * dx_squared;

		// Basal growth/decay rate is in units of concentration/time = mol / (area * time), so scale
		// by dt * (dx)^2.
		growth = r.get("GROWTH") * dt * dx_squared;
		
		// Threshold "biomass" (actually concentration) is given in mol / area. Each cell takes up
		// one square. Hence we scale by dx^2.
		threshold = r.get("THRESHOLD") * dx_squared;
	}
}
