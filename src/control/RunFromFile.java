package control;

import java.io.IOException;

import control.parameters.Parameters;
import control.parameters.ParametersFromFile;
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
 */
public class RunFromFile extends AbstractRunner {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		Parameters p = new ParametersFromFile("/Users/dbborens/Dropbox/Eclipse/efficient/sandbox/test.txt");
		run(p);
	}

}
