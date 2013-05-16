package io;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import structures.views.StateViewer;

import control.parameters.Parameters;

/**
 * Displays the location, biomass, and cell type of all cells in the system.
 * Results are written to file. 
 * 
 * @author dbborens@princeton.edu
 *
 */
public class CatalystWriter {

	
	/* CONSTANTS */
	private static final int CELL_HEIGHT = 5;
	private static final int CELL_WIDTH = 5;
	
	/* STATE VARIABLES */
	
	private Parameters p;
	private String path;
	
	private int width;
	private int height;
	
	private String format;
	
	/* CONSTRUCTORS */
	public CatalystWriter(Parameters p, String basePath, String format) {
		this.p = p;
		this.format = format;
		
		width = CELL_WIDTH * p.W();
		height = CELL_HEIGHT * p.W();
		
		if (p.getProduction() < p.epsilon()) {
			System.out.println("No production: omitting solute heat map.");
			return;
		}
		
		String path = basePath + "/solutes/";
		System.out.println(path);
		mkDir(path);
		
		this.path = path;

	}
	
	private BufferedImage buildImage(StateViewer state) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
 		Graphics gfx = img.getGraphics();
 		
		for (int i = 0; i < p.N(); i++) {

			float r = ((Double) state.getEnzyme().getScaled(i)).floatValue();	
			
			int x = i % p.W();
			int y = p.W() - i / p.W() - 1;	// Graphics coordinates invert Y-axis
 				
			drawGridPoint(gfx, x, y, r, r, r);

		}
		
		return img;
	}

	private void drawGridPoint(Graphics gfx, int x, int y,
			float r, float g, float b) {
		try {
			Color c = new Color(r, g, b);
			gfx.setColor(c);
			gfx.fillRect(x * CELL_WIDTH, y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
		} catch (Exception ex) {
			System.out.println("Offending values: " + r + ", " + g + ", " + b);
			throw new RuntimeException(ex);
		}
	}

	/* PUBLIC METHODS */
	
	public void refresh(StateViewer state) {
		if (p.getProduction() < p.epsilon())
			return;
		
		BufferedImage img = buildImage(state);
		export(img, state.getGillespie());
	}

	private void export(BufferedImage img, double gillespie) {
		String tStr = String.format(format, gillespie);
		
		File f = new File(path + tStr);
		try {
			ImageIO.write(img, "png", f);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getNominalWidth() {
		return width;
	}
	
	public int getNominalHeight() {
		return height;
	}
	
	private void mkDir(String pathStr) {
		File path = new File(pathStr);
		if (!path.exists()) {
			try {
				path.mkdir();
			} catch (Exception ex) {
				System.out.println("Could not create directory" + pathStr);
				throw new RuntimeException(ex);
			}			
		}
	}
}
