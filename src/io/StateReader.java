package io;

import java.io.*;

import structures.identifiers.Coordinate;
import structures.identifiers.Extrema;
import structures.views.StateViewer;
import structures.views.VectorViewer;
import no.uib.cipr.matrix.*;
import no.uib.cipr.matrix.sparse.SparseVector;

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
 */
public class StateReader {

	private static final String DATA_FILENAME = "data.txt";
	private static final String METADATA_FILENAME = "metadata.txt";
	
	// The StateReader always reads one line past the current state, so
	// we hold onto that line for the next call to getNext().
	private String prevLine = null;
	
	private Parameters p;
	
	Extrema ed;
	Extrema ec;
	Extrema eb;
	
	// Make sure time step index agrees with the one in the file.
	private double gillespie = 0;
	
	private BufferedReader br;
	public StateReader(String path, Parameters p) {
		this.p = p;
		File dataFile = new File(path + '/' + DATA_FILENAME);
		File metadataFile = new File(path + '/' + METADATA_FILENAME);
		
		try {
			// First read the metadata file to get the extrema
			extractMetadata(metadataFile);
			FileReader fr = new FileReader(dataFile);
			br = new BufferedReader(fr);
			prevLine = br.readLine().trim();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
	}
	
	private void extractMetadata(File metadataFile) throws IOException {
		FileReader mfr = new FileReader(metadataFile);
		BufferedReader mbr = new BufferedReader(mfr);
		String next = mbr.readLine();

		if (p.getProduction() < p.epsilon())
			ec = null;
		else
			ec = new Extrema(p.W());
		
		ed = new Extrema(p.W());
		eb = new Extrema(p.W());
		
		while (next != null) {
			next = next.trim();
			// So inelegant...this could be all done in one line with regex...oh well, I'm tired
			String[] mapping = next.split(">");
			String key = mapping[0];
			String value = mapping[1];
			
			if (key.equals("dm_dt"))
				loadExtrema(value, ed);
			else if (key.equals("enzyme")) {
				// If there is no production, don't load extrema for solute
				if (ec != null)
					loadExtrema(value, ec);
			} else if (key.equals("biomass"))
				loadExtrema(value, eb);
			else
				throw new IOException("Unrecognized metadata field " + key);
			
			next = mbr.readLine();
		}
	}
	
	// David, this is some ugly code...
	private void loadExtrema(String tokenize, Extrema e) {
		String[] minMax = tokenize.split(":");
		String[] minArg = minMax[0].split("@");
		String[] maxArg = minMax[1].split("@");
		
		Double min = Double.valueOf(minArg[0]);
		Double max = Double.valueOf(maxArg[0]);
		
		String[] minCoords = minArg[1].split(",");
		String[] maxCoords = maxArg[1].split(",");

		Coordinate argMin = new Coordinate (
				Integer.valueOf(minCoords[0]),
				Integer.valueOf(minCoords[1]),
				Double.valueOf(minCoords[2])
		);
		
		Coordinate argMax = new Coordinate (
				Integer.valueOf(maxCoords[0]),
				Integer.valueOf(maxCoords[1]),
				Double.valueOf(maxCoords[2])
		);

		e.load(min, argMin, max, argMax);
		
	}
	
	public StateViewer getNext() {
		try {
			// If prevLine is null, we're at the end of the file
			if (prevLine == null)
				return null;
			
			if (prevLine.startsWith(">")) {
				String[] tokens = prevLine.split(":");
				gillespie = Double.valueOf(tokens[1]);

				return readStates();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return null;
	}
	
	private StateViewer readStates() throws IOException {
		double gCurrent = gillespie;
		
		VectorViewer d = null;		// Undigested nutrient
		VectorViewer c = null;		// Enzyme
		
		VectorViewer b = null;		// Biomass
		
		Byte[] colors = new Byte[p.N()];
		while (prevLine != null) {
			// We come into this loop with a header line in prevLine
			String[] tokens = prevLine.split(">")[1].split(":");
			
			// When we reach the next time step, stop reading
			gillespie = Double.valueOf(tokens[1]);
			if (!p.epsilonEquals(gillespie, gCurrent))
				break;
			
			if (tokens[0].equals("dm_dt"))
				d = readVector(ed);
			else if (tokens[0].equals("enzyme")) {
				if (p.getProduction() < p.epsilon()) {
					c = null;
					fastForward();					
				} else {
					c = readVector(ec);
				}
			} else if (tokens[0].equals("biomass"))
				b = readVector(eb);
			else if (tokens[0].equals("color"))
				colors = readColors();
			else
				throw new IOException("Unrecognized field " + tokens[0]);
		}
		
		if (d == null)
			throw new IOException("Missing data for biomass derivatives");

		if (c == null && p.getProduction() > p.epsilon())
			throw new IOException("Missing data for enzyme");
		
		System.out.println(gCurrent);
		return new StateViewer(d, c, b, colors, gCurrent);
	}
	
	/**
	 * Skip ahead to the next field or end of file
	 */
	private Byte[] readColors() throws IOException {
		Byte[] colors = new Byte[p.N()];
		
		prevLine = br.readLine();
		
		// Line counter. Should reach p.H().
		int i = 0;
		
		while (prevLine != null && !(prevLine.startsWith(">"))) {
			
			// This gives a byte representing the ASCII value of the characters
			byte[] values = prevLine.trim().getBytes();
			if (values.length != p.W())
				throw new IOException("Unexpected line length! Expected " + p.W() + " but got " + values.length);
			
			for (int j = 0; j < p.W(); j++) {
				// ASCII digits start at 48
				colors[i * p.W() + j] = (byte) (values[j] - 48);
			}
			
			prevLine = br.readLine();
			i++;
		}
		
		return colors;
	}

	private VectorViewer readVector(Extrema ex) throws IOException {
		Vector v = new DenseVector(p.N());
		prevLine = br.readLine();
		
		// Line counter. Should reach p.H(). 
		int i = 0;
		
		// Iterate until we hit the end of the file or the start of a new data field
		while (prevLine != null && !(prevLine.startsWith(">"))) {
			String[] tokens = prevLine.trim().split("\t");
			if (tokens.length != p.W()) {
				throw new IOException("Unexpected column count: expected " + p.W() + ", but got " + tokens.length);
			}
			
			// Each row contains p.W() values (tab-delimited), and we expect p.H() rows. 
			for (int j = 0; j < p.W(); j++) {
				double x = Double.valueOf(tokens[j]);
				v.set(i * p.W() + j, x);
			}
			prevLine = br.readLine();
			i++;
		}
		
		if (i != p.W())
			throw new IOException("Unexpected row count: expected " + p.W() + ", but got " + i);

		return new VectorViewer(v, ex.min(), ex.max());
	}
	
	/**
	 * Ignore the entire vector and return 0. This is a band-aid for the
	 * degenerate case where there is no catalyst.
	 *  
	 * @return
	 * @throws IOException
	 */
	private void fastForward() throws IOException {
		prevLine = br.readLine();

		// Iterate until we hit the end of the file or the start of a new data field
		while (prevLine != null && !(prevLine.startsWith(">"))) {			
			prevLine = br.readLine();
		}
	}
}
