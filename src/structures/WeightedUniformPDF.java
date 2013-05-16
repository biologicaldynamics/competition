package structures;

import java.util.NavigableMap;
import java.util.TreeMap;

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
 * Stores a set of integers, each associated
 * with a weighting w. When sample() is called,
 * it returns one of the integers with
 * probability w / sum(w) of any particular
 * individual being selected.
 * 
 * If an individual is added to the set with
 * probability 0, it is never stored.
 * 
 * @author dbborens@princeton.edu
 *
 */
public class WeightedUniformPDF {

	private Parameters p;
	
	// A red-black tree representing the PDF.
	private NavigableMap<Double, Integer> pdf = new TreeMap<Double, Integer>();
	
	// The highest value in the PDF so far. By scaling to this number, the PDF is
	// normalized to 1 and becomes a true PDF.
	private Double top = 0D;
	
	// Indicates that the PDF has been finalized and is ready for sampling.
	private boolean ready;
	
	public WeightedUniformPDF(Parameters p) {
		this.p = p;
	}
	
	/**
	 * Adds a token to the distribution with the specified
	 * weight. If the token has weight zero, it is not added.
	 * Returns true iff the token is added to the distribution.
	 * 
	 * Repeat tokens are allowed; in that case two different
	 * regions of the PDF map to the same outcome.
	 * 
	 */
	public boolean add(Integer token, Double weight) {
		if (ready)
			throw new IllegalStateException("Attempted to modify PDF after it was finalized.");
		
		// If the token has zero weight, ignore it
		if (weight < p.epsilon())
			return false;
		
		pdf.put(top, token);
		
		top += weight;
		
		return true;
	}
	
	/**
	 * Makes it illegal to add more tokens to the distribution, and
	 * legal to start sampling tokens. This is to prevent bugs where
	 * the distribution is sampled while it is still being populated. 
	 */
	public void makeReady() throws EmptyPDFException {
		ready = true;
		
		if (pdf.size() == 0)
			throw new EmptyPDFException();
	}
	
	/**
	 * Samples one token at random, based on the PDF.
	 */
	public Integer sample() {
		if (!ready)
			throw new IllegalStateException("Attempted to sample the PDF before it was finalized.");
		
		// Scale a random number between 0 and 1 to the size of the unscaled PDF
		Double rand = p.getRandom().nextDouble() * top;
		
		return pdf.floorEntry(rand).getValue();
	}
}
