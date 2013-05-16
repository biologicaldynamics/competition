package structures.cell.ic;

import structures.cell.AbstractCell;
import structures.cell.Cheater;
import structures.cell.Producer;
import control.parameters.Parameters;

/**
 * A disc of producers, with radius specified by icArgument,
 * in a field of cheaters.
 *  
 * @author dbborens@princeton.edu
 *
 */
public class ProducerDisc extends InitialCondition {

	private int x0;
	private int y0;
	public ProducerDisc(Parameters p) {
		super(p);
		
		x0 = p.W() / 2;
		y0 = p.W() / 2;
		
		placeBackground(p);
		
		placeDisc(p);
	}

	private void placeBackground(Parameters p) {
		for (int x = 0; x < p.W(); x++) {
			for (int y = 0; y < p.W(); y++) {
				AbstractCell c;
				c = new Cheater(p, x, y);
				assign(x, y, c);
			}
			
		}
	}

	private void placeDisc(Parameters p) {
		for (int r = 0; r < p.getIcArgument(); r++) {
			for (int dx = 0; dx <= r; dx++) {
				int dy = r - dx;
				
				placeFour(p, dx, dy);
				
			}
		}
	}
	
	private void placeFour(Parameters p, int dx, int dy) {
		makeProducer(p, x0 + dx, y0 + dy);
		makeProducer(p, x0 + dx, y0 - dy);
		makeProducer(p, x0 - dx, y0 + dy);
		makeProducer(p, x0 - dx, y0 - dy);
	}

	private void makeProducer(Parameters p, int x, int y) {
		Producer c = new Producer(p, x, y);
		assign(x, y, c);
	}
}
