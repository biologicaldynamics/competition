package operations.processes.helpers;

import java.util.ArrayList;

import structures.cell.AbstractCell;
import control.EquilibriumException;
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
public class ReplacementHelper {

	// Since arrays are objects, and Java objects are pass by reference,
	// access to the original lattice reference will allow this object's
	// internal methods to update the lattice appropriately
	private Parameters p;
	private LifeCycleHelper manager;
	
	public ReplacementHelper(Parameters p, LifeCycleHelper manager) {
		this.p = p;
		this.manager = manager;
	}
	
	public void process (AbstractCell cell, boolean requireCompetitor) throws EquilibriumException {
		Tuple target;
		if (requireCompetitor)
			target = chooseCompetitorNeighbor(cell);
		else
			target = chooseTorusNeighbor(cell);

		fission(cell, target);	
	}
	
	
	private Tuple chooseCompetitorNeighbor(AbstractCell cell) {
		int x = cell.x();
		int y = cell.y();
		ArrayList<Tuple> targets = new ArrayList<Tuple>(4);
		
		byte selfType = manager.get(x, y).getType();
		
		AbstractCell target = manager.get(x + 1, y);
		if (target.getType() != selfType) {
			targets.add(new Tuple(x + 1, y));
		}		
		
		target = manager.get(x - 1, y);
		if (target.getType() != selfType) {
			targets.add(new Tuple(x - 1, y));
		}		

		target = manager.get(x, y + 1);
		if (target.getType() != selfType) {
			targets.add(new Tuple(x, y + 1));
		}		
		
		target = manager.get(x, y - 1);
		if (target.getType() != selfType) {
			targets.add(new Tuple(x, y - 1));
		}		
		
		int i = p.getRandom().nextInt(targets.size());
		
		return targets.get(i);
	}


	private Tuple chooseTorusNeighbor(AbstractCell cell) {
		int x = cell.x();
		int y = cell.y();
		
		Tuple[] targets = new Tuple[4];
		
		// Right (or wrap around)
		targets[0] = new Tuple(manager.wrap(x+1, p.W()), y);
		
		// Left (or wrap around)
		targets[1] = new Tuple(manager.wrap(x-1, p.W()), y);
		
		// Up (or wrap around)
		targets[2] = new Tuple(x, manager.wrap(y+1, p.W()));
		
		// Down (or wrap around)
		targets[3] = new Tuple(x, manager.wrap(y-1, p.W()));
		
		int i = p.getRandom().nextInt(4);
		
		//System.out.println("  Dividing cell: (" + x + ", " + y + "). Target: (" + targets[i].x + ", " + targets[i].y + ").");

		return targets[i];
	}
	
	private class Tuple {
		public int x;
		public int y;
		
		public Tuple(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	private void fission(AbstractCell cell, Tuple t) {
		manager.fission(cell, t.x, t.y);
	}
}
