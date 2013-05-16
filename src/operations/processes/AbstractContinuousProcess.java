package operations.processes;

import io.BufferedStateWriter;
import io.IteratedSimulationWriter;
import io.PhaseWriter;

import java.util.ArrayList;

import no.uib.cipr.matrix.Vector;

import structures.cell.AbstractCell;
import structures.cell.ic.InitialCondition;
import control.EquilibriumException;
import control.RdfHaltException;
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
public abstract class AbstractContinuousProcess extends AbstractLifeCycle {


	public AbstractContinuousProcess(Parameters p, InitialCondition ic) {
		super(p, ic);
	}

	
	public abstract void turnover(Vector c, BufferedStateWriter bsw, IteratedSimulationWriter isw, PhaseWriter pw) throws EquilibriumException, RdfHaltException;


}
