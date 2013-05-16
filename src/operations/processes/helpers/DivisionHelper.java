package operations.processes.helpers;

import java.util.HashMap;
import java.util.HashSet;

import control.EquilibriumException;
import control.parameters.Parameters;
import structures.cell.AbstractCell;

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
public class DivisionHelper {

	// Since arrays are objects, and Java objects are pass by reference,
	// access to the original lattice reference will allow this object's
	// internal methods to update the lattice appropriately
	private AbstractCell[][] lattice;
	private Parameters p;
	private LifeCycleHelper manager;
	
	public DivisionHelper(Parameters p, LifeCycleHelper manager, AbstractCell[][] lattice) {
		this.p = p;
		this.lattice = lattice;
		this.manager = manager;
	}

	public void process(AbstractCell cell) throws EquilibriumException {
		// DEBUG CODE: move this to a regression test after problem solved
		/*HashMap<AbstractCell, Integer> oldCoords = new HashMap<AbstractCell, Integer>();
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.H(); y++) {
				int cell_x = lattice[x][y].x();
				int cell_y = lattice[x][y].y();
				if (x != cell_x || y != cell_y)
					throw new RuntimeException("Misplaced cell: (" + x + ", " + y + ")");
				oldCoords.put(lattice[x][y], cell_y * p.W() + cell_x);
			}
		}*/
		// END DEBUG
		
		Integer test = cell.y() * p.W() + cell.x();
		HashMap<Integer, Offset> candidates = findCandidates(test);
		
		// If this cell can't divide, signal to stop the simulation
		if (candidates.isEmpty()) {
			for (int x = 0; x < p.W(); x++)
				for (int y = 0; y < p.W(); y++)
					if (lattice[x][y].getType() == AbstractCell.EMPTY) {
						System.out.println("Location unable to divide: " + x + " " + y);
					}
			throw new EquilibriumException(AbstractCell.OTHER, -1);
		}
		
		// Choose a candidate as the placement site
		Object[] candidatesArr = candidates.values().toArray();
		int index = p.getRandom().nextInt(candidates.size());
		Offset offset = (Offset) candidatesArr[index];

		// Shove toward that coordinate
		int x0 = cell.x();
		int y0 = cell.y();

		// BEGIN DEBUG
		//checkLocations();
		// END DEBUG
		
		shove(x0, y0, offset.dx, offset.dy);
		
		// At this point, a reference to the dividing cell exists at both
		// the old location and the new one!
		
		// Divide the cell into two cells, each of which has half of the original
		// biomass. At the end of this step, lattice[x0][y0] should point to the
		// "new" cell, and lattice[t.x][t.y] should point to the "original" cell.
		// (They are identical at this point, so this is just for troubleshooting.)
		manager.fission(cell, x0, y0);
		
		// BEGIN DEBUG
		//checkLocations();
		//doAssertions(oldCoords);
		// END DEBUG
		
	}
	
	
	/**
	 * Shoves a cell in a random path from the origin to the target
	 * of length equal to the Manhattan distance. In each iteration,
	 * it chooses whether to go horizontally or vertically with a
	 * probability weighted by the number of moves remaining in that
	 * direction.
	 */
	private void shove(int x0, int y0, int dx, int dy) {
			
		// Base case: we've reached the target
		if (dx == 0 && dy == 0)
			return;
		
		// Choose whether to go horizontally or vertically, weighted
		// by the number of steps remaining in each direction
		int d = Math.abs(dx) + Math.abs(dy);
		
		int n = p.getRandom().nextInt(d);
		
		// Take a step in the chosen direction.
		int xNext = x0;
		int yNext = y0;
		int dxNext = dx;
		int dyNext = dy;
		if (n < Math.abs(dx)) {
			xNext += sign(dx);
			dxNext -= sign(dx);
		} else {
			yNext += sign(dy);
			dyNext -= sign(dy);
		}		
		shove(xNext, yNext, dxNext, dyNext);
		
		manager.set(xNext, yNext, manager.get(x0, y0));
		
		// NOTE: This implies the "parent" cell ends up getting shoved
		// and the "child" cell occupies the origin. (This does not matter
		// because they're identical; but it affects tracking/debugging.)
	}
	
	/**
	 * Check if the space at (x, y) is:
	 *   (1) empty
	 *   (2) legal
	 *   (3) already specified as a candidate
	 *
	 * If it is all of these things, add it as a candidate.
	 * 
	 */
	private void test(int x, int y, int dx, int dy, HashMap<Integer, Offset> candidates) {
		
		int wrapped_x = manager.wrap(x, p.W());
		int wrapped_y = manager.wrap(y, p.W());
		
		Integer canonical = wrapped_y * p.W() + wrapped_x;
		Offset offset = new Offset(dx, dy);
		if(manager.get(x, y).getType() == AbstractCell.EMPTY && ! candidates.containsKey(canonical))
			candidates.put(canonical, offset);
	}
	

	
	/**
	 * Find legal sites for cell division. In this instance, we procede
	 * out in a diamond (equal Manhattan distance) from the original cell:
	 * 
	 * distance=1:
	 * 
	 *           x 
	 *          xOx
	 *           x 
	 *          
	 * distance=2:
	 * 
	 *           x 
	 *          x x
	 *         x O x
	 *          x x
	 *           x
	 *         
	 * And so on, where "x" represents a possible candidate and
	 * O represents the original cell.
	 * 
	 */
	private HashMap<Integer, Offset> findCandidates(int c) {
		int n = 1;
		
		int x = c % p.W();
		int y = c / p.W();
		
		// We don't want to store the same candidate twice, or we'll bias
		// the outcome, so we hash the legal candidates by canonical coordinate.
		// But we want to preserve the wrap direction, so we preserve the periodic
		// coordinate (where x + n w = x and y + n h = y), which is used as the
		// final candidate location.
		HashMap<Integer, Offset> candidates = new HashMap<Integer, Offset>(8);
		
		while (candidates.isEmpty()) {
			//System.out.println(c + "\t" + n);
			for (int dx = 0; dx <= n; dx++) {
				int dy = n - dx;
				
				//System.out.println(n + "\t" + dx + "\t" + dy);
				boolean maxed_x = Math.abs((dx * 2 + (p.W() % 2))) > p.W();
				boolean maxed_y = Math.abs((dy * 2 + (p.W() % 2))) > p.W();
				
				if (maxed_x && maxed_y)
					return candidates;
				
				test(x + dx, y + dy, dx, dy, candidates);
				test(x - dx, y + dy, dx * -1, dy, candidates);
				test(x + dx, y - dy, dx, dy * -1, candidates);
				test(x - dx, y - dy, dx * -1, dy * -1, candidates);					
			}
			n++;
		}
		// Return the nearest legal candidates
		return candidates;
	}
	
	private int cmp(int p, int q) {
		if (p < q)
			return -1;
		else if (p > q)
			return 1;
		else
			return 0;
	}
	

	/**
	 * Returns 1 * the sign of input.
	 */
	private int sign(int i) {
		return cmp(i, 0);
	}
	
	/**
	 * A series of assertions for debugging the shoving process.
	 */
	private void doAssertions(HashMap<AbstractCell, Integer> oldCoords) {
		
		HashSet<AbstractCell> remaining = new HashSet<AbstractCell>();
		
		int liveCellsOld = 0;
		int deadCellsOld = 0;
		
		int liveCellsNow = 0;
		int deadCellsNow = 0;
		
		// Every cell is where it thinks it is
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				int cell_x = lattice[x][y].x();
				int cell_y = lattice[x][y].y();
				int cell_idx = cell_y * p.W() + cell_x;
				
				if (x != cell_x || y != cell_y)
					throw new RuntimeException("Misplaced cell: (" + x + ", " + y + ") !=" + (cell_x) + ", " + (cell_y));

				if (lattice[x][y].getType() == AbstractCell.EMPTY)
					deadCellsNow++;
				else
					liveCellsNow++;
				
				// No cell exists more than once
				if (remaining.contains(cell_idx))
					throw new RuntimeException("A cell appears twice!");
				
				if (oldCoords.containsKey(lattice[x][y])) {
					Integer oldCoord = oldCoords.get(lattice[x][y]);
					int old_x = oldCoord % p.W();
					int old_y = oldCoord / p.W();
					// No cell has moved more than 1 cell in the x direction in the course of a cell division (including wrapping around).
					if ((Math.abs(old_x - x) > 1) && (Math.abs(old_x - x) != p.W() - 1))
						throw new RuntimeException("A cell was shoved more than 1 square in the x direction. Old: " + oldCoord + "; new: " + (cell_x) + ", " + (cell_y));
					
					// No cell has moved more than 1 cell in the y direction in the course of a cell division.
					if (Math.abs(old_y - y) > 1 && (Math.abs(old_y - y) != p.W() - 1))
						throw new RuntimeException("A cell was shoved more than 1 square in the y direction. Old: " + oldCoord + "; new: " + (cell_x) + ", " + (cell_y));
				}
				remaining.add(lattice[x][y]);
			}
		}
	}	

	private void checkLocations() {
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				if (x != lattice[x][y].x() || y != lattice[x][y].y())
					throw new RuntimeException("Misplaced cell: (" + x + ", " + y + ") != (" + lattice[x][y].x() + ", " +lattice[x][y].y() + ")");
			}
		}
	}
	
	private class Offset {
		public int dx;
		public int dy;
		
		public Offset(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}
	}
}
