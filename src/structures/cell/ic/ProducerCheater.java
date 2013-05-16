package structures.cell.ic;

import structures.cell.*;
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
 * @author dbborens@princeton.edu
 *
 */
public class ProducerCheater extends InitialCondition {

	public ProducerCheater(Parameters p) {
		super(p);
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				EmptyCell empty = new EmptyCell(p, x, y);
				assign(x, y, empty);

			}
		}

		for (int x = (p.W() / 2) - 2; x <= (p.W() / 2) + 2; x++) {
			for (int y = (p.W() / 2) - 2; y <= (p.W() / 2) + 2; y++) {
				AbstractCell c = new Producer(p, x, y);
				assign(x, y, c);
			}
			
		}
		
		
		int x = p.W() / 2;
		int y = p.W() / 2;

		AbstractCell c = new Cheater(p, x, y);
		
		//Cell founder = new TwoToneCell(p, c);
		assign(x, y, c);
		
	}
}
