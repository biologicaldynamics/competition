package io;

import structures.views.StateViewer;

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
 * Renders the state to graphics files.
 */
public class Renderer {
	
	// Time step counter
	int i = 0;
	
	//protected CatalystWriter portal;
	protected CellMapWriter lattice;
	protected TwoPaneMapWriter dual;
	//protected DerivativeWriter heat;
	protected int modulo;
	
	public Renderer(Parameters p, String path, String format, int modulo) {
		//portal = new CatalystWriter(p, path, format);
		lattice = new CellMapWriter(p, path, format);
		dual = new TwoPaneMapWriter(p, path, format);
		//heat = new DerivativeWriter(p, path, format);
		this.modulo = modulo;
	}
	
	public void refresh(StateViewer state) {
		if (i % modulo == 0) {
			// Render a composite heat map of the concentration fields
			//portal.refresh(state);
			
			// Render a color map of cell locations, types and biomass
			lattice.refresh(state);
			
			dual.refresh(state);
			
			//heat.refresh(state); 
		}
		i++;
	}

	public void finalize(StateViewer state) {
		// Render a composite heat map of the concentration fields
		//portal.refresh(state);
		
		// Render a color map of cell locations, types and biomass
		lattice.refresh(state);
		
		dual.refresh(state);

		//heat.refresh(state); 		
	}
}
