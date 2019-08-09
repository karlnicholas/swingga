package swingga;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import swingga.SwingGa.MyPanel;

/**
 * 
 * Main Simulation for running simulation and killing and reproducing and mutation critters. 
 *
 */
public class SimulationThread implements Runnable {
	private static final Random rand = new Random();
	public final static int cSize = 10;
	private List<Critter> critters; 
	public boolean run = true;
	private int counter = 0;
	private MyPanel myPanel; 
	public SimulationThread(MyPanel myPanel) {
		this.myPanel = myPanel;
		critters = new ArrayList<>();
		for ( int i = 0; i < 100; ++i ) {
			critters.add(new Critter(
					cSize + (int)(Math.random() * 900), cSize + (int)(Math.random() * 900), 
					new CritterRandomMovement(3, 3, 1, 1, 4, 4) 
					));
		}
		
	}
	@Override
	public void run() {
		while(run) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			counter++;
			// move everything
			critters.forEach(Critter::move);
			// check for collisions, remove from list
			checkCollisions();

			myPanel.repaint();
			if ( counter % 20 == 0 ) {
				printAverages();
				reproduceAndMutate(critters);
			}
		}
	}
	
	private void reproduceAndMutate(List<Critter> cs) {
		// genetic reproduction callback code.
		int cSize = cs.size();
		for ( int i = 100 - cSize; i > 0; --i) {
			Critter cr = cs.get(rand.nextInt(cSize));
//			Critter cn = new Critter(cr.x,  cr.y, cr.getMovement().cloneAndMutate());
			Critter cn = new Critter(rand.nextInt(1000), rand.nextInt(1000),  cr.getMovement().cloneAndMutate());
			cs.add(cn);
		}
	}

	private void checkCollisions() {
		Iterator<Critter> cit = critters.iterator();
    	while ( cit.hasNext() ) {
    		Critter c1 = cit.next();
    		Rectangle r1 = new Rectangle(c1.x, c1.y, cSize, cSize);
	    	for ( Critter c2: critters ) {
	    		if ( c1 == c2) continue;
	    		Rectangle r2 = new Rectangle(c2.x, c2.y, cSize, cSize);
	    		if ( r1.intersects(r2) ) {
	    			cit.remove();
	    			break;
	    		}
	    	}
    	}
	}  

	private void printAverages() {
		int cSize = critters.size();
		int xRandMax = 0; 
		int yRandMax = 0;
		int xRandOff = 0;
		int yRandOff = 0;
		int xRandLimit = 0;
		int yRandLimit = 0;
		for ( int i = 0 ; i < cSize; ++i) {
			Critter cr = critters.get(i);
			CritterRandomMovement m = (CritterRandomMovement) cr.getMovement(); 
			xRandMax += m.xRandMax; 
			yRandMax += m.yRandMax;
			xRandOff += m.xRandOff;
			yRandOff += m.yRandOff;
			xRandLimit += m.xRandLimit;
			yRandLimit += m.yRandLimit;
		}
		
		System.out.println(
		xRandMax / cSize +":" +  
		yRandMax / cSize +":" + 
		xRandOff / cSize +":" + 
		yRandOff / cSize +":" + 
		xRandLimit / cSize +":" + 
		yRandLimit / cSize 
		);
	}
	public List<Critter> getCritters() {
		return critters;
	}
	public void drawCritters(Graphics2D g2d) {
		critters.forEach(c->g2d.fillOval(c.x, c.y, cSize, cSize));	    		
	}
}

