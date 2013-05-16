package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

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
 * 
 * Reads parameters from a file. See Parameters.java for a description of each
 * parameter.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class ParameterReader {
	
	// Non-FP parameters (the rest are stored as doubles)
	private Integer w;
	private Long seed;
	private String path;
	private boolean stamp;
	private boolean reciprocates;
	private boolean randomizeCheaters;
	private boolean randomizeCooperators;
	private boolean infiniteGamma;
	private String ic;
	private String output;
	private Integer icArgument;
	private String cellOperator;
	private Integer maxTimeStep;
	private Integer replicates;
	private Integer haltCount;
	
	private HashMap<String, Double> params = new HashMap<String, Double>(11);
	
	public ParameterReader(File pFile) throws IOException {
		readParams(pFile);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("W=");
		sb.append(w);
		sb.append("\nRANDOM_SEED=");
		sb.append(seed);
		sb.append("\nPATH=");
		sb.append(path);
		sb.append("\nSTAMP=");
		sb.append(stamp);
		sb.append("\nOUTPUT=");
		sb.append(output);
		sb.append("\nRANDOMIZE_CHEATERS=");
		sb.append(randomizeCheaters);
		sb.append("\nRANDOMIZE_COOPERATORS=");
		sb.append(randomizeCooperators);		
		sb.append("\nIC=");
		sb.append(ic);
		sb.append("\nIC_ARGUMENT=");
		sb.append(icArgument);
		sb.append("\nRECIPROCATES=");
		sb.append(reciprocates);
		sb.append("\nINFINITE_GAMMA=");
		sb.append(infiniteGamma);
		sb.append("\nCELL_OPERATOR=");
		sb.append(cellOperator);
		sb.append("\nHALT_COUNT=");
		sb.append(haltCount);
		sb.append("\nMAX_TIME_STEP=");
		sb.append(maxTimeStep);
		sb.append("\nREPLICATES=");
		sb.append(replicates);
		
		// Note: we record the random seed used, whether or not it was generated
		// in this run--the params file output should allow us to reproduce this
		// run exactly
		
		Iterator<String> i = params.keySet().iterator();
		while (i.hasNext()) {
			sb.append('\n');
			String s = i.next();
			sb.append(s);
			sb.append('=');
			sb.append(params.get(s));
		}
		
		return sb.toString();
	}
	
	private void readParams(File pFile) throws IOException {
		FileReader fr = new FileReader(pFile);
		BufferedReader br = new BufferedReader(fr);
		
		String next = br.readLine();
		
		while(next != null) {
			String[] tokens = next.trim().split("=");
			
			// Handle non-double values
			if (tokens[0].equals("W")) {
				w = Integer.valueOf(tokens[1]);
				
			} else if (tokens[0].equals("PATH")) {
				path = tokens[1];
				
			} else if (tokens[0].equals("IC")) {
				ic = tokens[1];
				
			} else if (tokens[0].equals("CELL_OPERATOR")) {
				cellOperator = tokens[1];
				
			} else if (tokens[0].equals("IC_ARGUMENT")) {
				icArgument = Integer.valueOf(tokens[1]);
				
			} else if (tokens[0].equals("REPLICATES")) {
				replicates = Integer.valueOf(tokens[1]);
				
			} else if (tokens[0].equals("MAX_TIME_STEP")) {
				maxTimeStep = Integer.valueOf(tokens[1]);
				
			} else if (tokens[0].equals("STAMP")) {
				stamp = Boolean.valueOf(tokens[1]);
				
			} else if (tokens[0].equals("OUTPUT")) {
				output = tokens[1];
				
			} else if (tokens[0].equals("RECIPROCATES")) {
				reciprocates = Boolean.valueOf(tokens[1]);
				
			} else if (tokens[0].equals("INFINITE_GAMMA")) {
				infiniteGamma = Boolean.valueOf(tokens[1]);	
				
			} else if (tokens[0].equals("RANDOMIZE_CHEATERS")) {
				randomizeCheaters = Boolean.valueOf(tokens[1]);
				
			} else if (tokens[0].equals("RANDOMIZE_COOPERATORS")) {
				randomizeCooperators = Boolean.valueOf(tokens[1]);
				
			} else if (tokens[0].equals("HALT_COUNT")) {
				haltCount = Integer.valueOf(tokens[1]);
				
			// Assign specified random seed, or a new seed
			} else if (tokens[0].equals("RANDOM_SEED")) {
				if (tokens[1].equals("*"))
					seed = System.currentTimeMillis();
				else
					seed = Long.valueOf(tokens[1]);
			
			// Everything else is a double
			} else {
				try {
				Double value = Double.valueOf(tokens[1]);
				params.put(tokens[0], value);
				} catch (NumberFormatException ex) {
					System.out.println("Bad format or parameter not recognized: " + tokens[0] + "=" + tokens[1]);
				}
			}
			
			next = br.readLine();
		}
	}
	
	public Double get(String key) {
		return params.get(key);
	}
	
	public Integer getWidth() {
		return w;
	}
	
	public Long getSeed() {
		return seed;
	}

	public String getPath() {
		return path;
	}
	
	public boolean isStamp() {
		return stamp;
	}
	
	public String getIC() {
		return ic;
	}

	public boolean isReciprocates() {
		return reciprocates;
	}

	public int getIcArgument() {
		return icArgument;
	}

	public boolean isRandomizeCheaters() {
		return randomizeCheaters;
	}
	
	public boolean isRandomizeCooperators() {
		return randomizeCooperators;
	}
	
	public String getCellOperator() {
		return cellOperator;
	}

	public int getMaxTimeStep() {
		return maxTimeStep;
	}

	public String getOutput() {
		return output;
	}

	public int getReplicates() {
		return replicates;
	}

	public boolean isInfiniteGamma() {
		return infiniteGamma;
	}

	public int getHaltCount() {
		return haltCount;
	}
}
