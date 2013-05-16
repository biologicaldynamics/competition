package control.parameters;

import java.util.Random;

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
 * 
 * An object representing all the parameters of the system, and which is used by virtually all components of
 * the system to define behavior.
 * 
 * The "MAX_R" parameter, defined in the parameters file, is not explicitly tracked as a parameter because it
 * is only used while initializing the other parameters. It is defined below:
 * 
 * r.get("MAX_R") -- 
 * 	 The entries in the operator matrix the contribution of a certain region of
 *	 space and of time. The magnitude of that contribution scales with the size of
 *	 the area and the interval of time represented by the matrix.
 *	
 *	 For the DiffusionOperator class, which represents the heat equation, solutions
 *	 become unstable when d * dt / (dx)^2 > 0.25. This is because:
 *	
 *	 u(x, y, t+1) = u(x, y, t) + r * [u(x+1, y, t) + u(x-1, y, t) - u(x, y+1, t) + u(x, y-1, t) - 4u(x, y, t)]
 *	
 *	 Where r = d * dt / (dx)^2. Plainly, for r > 0.25, more than 100% of the concentration
 *	 contributes to the flux, which is absurd.
 *	
 *	 We enforce a reasonable R by scaling all operators to a suitable dt for the given r.get("dx").
 *
 * All other paramerers are defined in the code, as well as in the algorithm writeup.
 * 
 * @author dbborens@princeton.edu
 *
 */
public abstract class Parameters {
	
	// Machine epsilon
	protected double epsilon = calcEpsilon();
	
	// Number of cells wide (each cell is 1 sq. micron)
	protected int width;
	
	// Matrix size is the product of these
	protected int dimension;
	
	
	// Time step (in seconds)
	protected double dt;
	
	// Length step (in microns)
	protected double dx;
	
	// See below for descriptions
	protected double diffusion;
	protected double decay;
	protected double benefit;
	protected double threshold;
	protected double production;
	protected double growth;
	
	// How long to let the simulation run if it doesn't reach equilibrium first
	protected int maxTimeStep;
	
	// How many instances to run
	protected int replicates;
	
	// Number of cheaters (applies for certain ICs only)
	protected int icArgument;

	// Numer of cheaters required before concluding the simulation and
	// calculating the radial distribution function (-1 means don't halt for RDF)
	protected int haltCount;
	
	// Output path (does not recognize ~ as home directory)
	protected String path;
	
	// Root output path (since "path" changes when the run is iterated)
	protected String rootPath;
	
	protected String cellOperator;
	
	// Put time stamp in output path?
	protected boolean stamp;
	
	// Output mode. Permitted options:
	//	FULL --> full output (visualizations, etc) + rollup stats
	// 	REDUCED --> Only interval, parameters, rollup stats
	//  SPARSE --> Rollup stats only
	protected String output;
	
	// What initial condition to use?
	protected String ic;
	
	// Initialize cooperators / cheaters with random starting values 
	protected boolean randomizeCheaters;
	protected boolean randomizeCooperators;
	
	protected boolean infiniteGamma;

	protected String stringRepresentation;
	
	/*
	 * A random number generator seed, used as an argument to the
	 * constructor for Random(). If this is set to "*" in the parameters file,
	 * a new seed is generated. This makes it possible to re-run old simulations
	 * deterministically.
	 */
	protected long randomSeed;
	
	protected Random random;
	//private HashMap<String, Double> params = new HashMap<String, Double>(11);

	protected int currentReplicate = 0;
	
	/** PRIVATE METHODS **/

	/**
	 * Find the machine epsilon for this computer (i.e., the value at which
	 * double-precision floating points can no longer be distinguished.)
	 * 
	 * Adapted from the Wikipedia article "Machine epsilon" (retrieved 3/18/2012)
	 * 
	 */
	private double calcEpsilon() {
        double machEps = 1.0d;
        
        do {
           machEps /= 2d;
        } while (1d + (machEps/2d) != 1d);
        
        return machEps;
	}
	
	/**
	 * Make sure every parameter was provided and everything is kosher.
	 */
	protected void validateAndInit() {
		
		if (!icRequiresArgument() && icArgument != -1)
			throw new IllegalArgumentException("The initial condition you specified does not require an argument. Set NUM_CHEATERS to -1.");
		
		if (icRequiresArgument() && icArgument < 0)
			throw new IllegalArgumentException("The initial condition you specified requires an argument. Set NUM_CHEATERS greater than or equal to 0.");
		
		if (!(output.equalsIgnoreCase("MINIMAL") || output.equalsIgnoreCase("SPARSE") || output.equalsIgnoreCase("FULL") || output.equalsIgnoreCase("REDUCED")))
			throw new IllegalArgumentException("Unrecognized output mode '" + output + "'. Acceptable options are FULL, REDUCED and SPARSE.");
		
		if (isStamp())
			throw new UnsupportedOperationException("After introducing the ability to run a repeated simulation, the time stamp behavior now produces " + 
					"stupid file paths. Leave this error here until you figure out how you want this behavior to work. You could also just remove it " +
					"if you no longer use it.");
		
		if (infiniteGamma && (growth != 0D || benefit != 0D))
			throw new IllegalArgumentException("When using infinite gamma mode, the GROWTH and BENEFIT arguments must be set to 0.");

		
		rootPath = path;
	}
	
	// Returns true if the IC occupies the entire lattice.
	private boolean icRequiresArgument() {
		if (ic.equals("WellMixed"))
			return true;
		else if (ic.equals("CheaterDisc"))
			return true;
		else if (ic.equals("ProducerDisc"))
			return true;		
		
		return false;		
	}
	/** PUBLIC METHODS **/
	
	@Override
	/**
	 * A textual representation of the parameters (in their original units).
	 * This format is both human-readable and the input to the ParameterReader
	 * class.
	 */
	public String toString() {
		return stringRepresentation;
	}

	/**
	 * The random number generator for the entire system. If the same generator
	 * is always used, then simulation results can be reproduced subsequently.
	 * 
	 * @return
	 */
	public Random getRandom() {
		return random;
	}
	
	public int W() {
		return width;
	}
	
	public int N() {
		return dimension;
	}
	
	public double epsilon() {
		return epsilon;
	}

	public double getDiffusion() {
		return diffusion;
	}

	public double getDecay() {
		return decay;
	}

	public double getBenefit() {
		return benefit;
	}

	public double getThreshold() {
		return threshold;
	}

	public double getProduction() {
		return production;
	}

	public double getGrowth() {
		return growth;
	}
	
	/**
	 * Determines whether two doubles are equal to within machine epsilon.
	 * 
	 * @param p
	 * @param q
	 */
	public boolean epsilonEquals(double p, double q) {
		if (Math.abs(p - q) < epsilon)
			return true;
		
		return false;
	}

	public double dt() {
		return dt;
	}
	
	public String getPath() {
		if (replicates == 1)
			return path;
		
		StringBuilder sb = new StringBuilder(path);
		sb.append('/');
		sb.append(currentReplicate);
		sb.append('/');
		
		return sb.toString();
	}
	
	public boolean isStamp() {
		return stamp;
	}

	public Object getIC() {
		return ic;
	}
	
	public boolean randomizeCheaters() {
		return randomizeCheaters;
	}
	
	public boolean randomizeCooperators() {
		return randomizeCooperators;
	}
	
	public int getIcArgument() {
		return icArgument;
	}

	public String getCellOperator() {
		return cellOperator;
	}

	public int maxTimeStep() {
		return maxTimeStep;
	}
	
	public void nextReplicate() {
		/* Using the iterative solver, the simulation is so fast
		 * that it may complete in less than 1 ms for some cases!
		 * 
		 * As a result, the random seed may not advance without
		 * looping until the system time reflects the next ms.
		 * 
		 * On a system that records sub-millisecond system time
		 * (research computing?) this will not be a problem.
		 */
		
		long oldRandomSeed = randomSeed;
		
		while (randomSeed == oldRandomSeed)
			randomSeed = System.nanoTime();
		
		random = new Random(randomSeed);
		
		currentReplicate++;
	}

	public int getReplicates() {
		return replicates;
	}

	public String getOutput() {
		return output;
	}
	
	public String getRootPath() {
		return rootPath;
	}
	
	
	public long getRandomSeed() {
		return randomSeed;
	}
	
	public boolean isInfiniteGamma() {
		return infiniteGamma;
	}

	public int getHaltCount() {
		return haltCount;
	}
}
