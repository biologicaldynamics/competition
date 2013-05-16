package control;

import io.Renderer;
import io.StateReader;

import java.io.File;
import java.io.IOException;

import control.parameters.Parameters;
import control.parameters.ParametersFromFile;

import structures.views.StateViewer;

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
 * @author dbborens@princeton.edu
 *
 */
public class Visualizer {

	private final String FORMAT = "%1.1e.png";
	private final int FREQ = 1;
	
	public Visualizer(String path) throws IOException {
		this(path, new ParametersFromFile(path + "/params.txt"));
	}

	public Visualizer(String simPath, Parameters p) {
		StateViewer lastState = null;
		StateReader reader = new StateReader(simPath, p);
		Renderer home = new Renderer(p, simPath, FORMAT, FREQ);
		int n = 0;
		while (true) {	
			StateViewer state = reader.getNext();
			if (state == null)
				break;
			home.refresh(state);
			System.out.print(".");
			n++;
			if (n % 80 == 0)
				System.out.println();
			lastState = state;
		}
		home.finalize(lastState);
	}
	
}
