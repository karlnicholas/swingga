package swingga;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import swingga.SwingGa.MyPanel;

/**
 * 
 * Main Simulation for running simulation and killing and reproducing and mutation screenItems.critters. 
 *
 */
public class SimulationThread implements Runnable {
	private ScreenItems screenItems;
	private static final Random rand = new Random();
	public final static int cSize = 10;
	public boolean run = true;
	private int counter = 0;
	private MyPanel myPanel;
	private Offset randomJumpOffset;
	public SimulationThread(MyPanel myPanel) {
		this.myPanel = myPanel;
		randomJumpOffset = new Offset();
		screenItems = new ScreenItems();
		screenItems.critters = new ArrayList<>();
		screenItems.foodStuffs = new ArrayList<>();
		for ( int i = 0; i < 100; ++i ) {
			screenItems.critters.add(new Critter(
				cSize + (int)(Math.random() * 900), cSize + (int)(Math.random() * 900), 
				new CritterTuringMovement() 
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
			synchronized( screenItems ) {
	
				// move everything
				Iterator<Critter> cit = screenItems.critters.iterator();
	    		Critter c = null;
		    	while ( cit.hasNext() ) {
		    		c = cit.next();
		    		if ( handleMoveAndCheckDeath(c) ) { 
		    			cit.remove();
		    			continue;
		    		}
					checkFoodFound(c);
				}
		    	// just in case everthing dies
		    	if ( screenItems.critters.size() == 0 ) {
		    		c.energy = 100;
		    		screenItems.critters.add(c);
		    	}
				if ( counter % 10 == 0 ) {
	//				printAverages();
					reproduceAndMutate();
				}
				if ( counter % 10 == 0 ) {
					dropFood();
				}
				myPanel.repaint();
			}
		}
	}
	
	private boolean handleMoveAndCheckDeath(Critter c) {
		Offset offset = c.getMovement().getMovement(c);
		c.move(offset);

		// check for collisions
    	for ( Critter c2: screenItems.critters ) {
    		if ( c == c2) continue;
    		if ( c.r.intersects(c2.r) ) {
    			handleCollision(c, c2, offset);
    			break;
    		}
    	}
		assessMovementCost(c, offset);
		if ( c.energy <= 0  ) {
			return true;
		}
		return false;
	}
	
	
	private void assessMovementCost(Critter c, Offset offset) {
		c.energy -= Math.max( Math.abs(offset.mx) + Math.abs(offset.my), (counter%3==0?1:0));
	}
	
	private Offset getRandomJump() {
		randomJumpOffset.mx = (10 - rand.nextInt(21));
		randomJumpOffset.my = (10 - rand.nextInt(21));
		return randomJumpOffset;
	}
	
	private void handleCollision(Critter c, Critter c2, Offset offset) {
		if ( c.energy > c2.energy ) {
			c2.move(getRandomJump());
			c2.energy -= 40;
		} else if ( c2.energy > c.energy ) {
			offset.mx = 0 - offset.mx; 
			offset.my = 0 - offset.my; 
			c.move(offset);
			offset.mx = 0; 
			offset.my = 0; 
		}
	}
	private void checkFoodFound(Critter c) {
		// check for food found, add to 
		Iterator<Food> fit = screenItems.foodStuffs.iterator();
		while ( fit.hasNext() ) {
			Food f = fit.next();
    		if ( c.r.intersects(f.r) ) {
    			int newE = c.energy + 1000;
    			if (newE > 10000) newE = 10000;
    			c.energy = newE;
    			fit.remove();
    			break;
    		}
    	}
		
	}
	
	private void dropFood() {
		Iterator<Food> fit = screenItems.foodStuffs.iterator();
		while ( fit.hasNext() ) {
			Food f = fit.next();
			f.energy -= 50;
			if ( f.energy <= 0 ) {
				fit.remove();
			}
		}
		int fSize = screenItems.foodStuffs.size();
		for ( int i = 100 - fSize; i > 0; --i) {
			screenItems.foodStuffs.add(new Food(rand));
		}
	}
	private void reproduceAndMutate() {
		// genetic reproduction callback code
		Collections.sort(screenItems.critters, (c1, c2)-> {return c2.energy - c1.energy;} );
		int cSize = screenItems.critters.size();
		for ( int i = 100 - cSize; i > 0; --i) {
			Critter cr = screenItems.critters.get(rand.nextInt(Math.min(10, cSize)));
			if ( cr.energy > 200 ) {
	//			Critter cn = new Critter(cr.x,  cr.y, cr.getMovement().cloneAndMutate());
				Critter cn = new Critter(
						Math.max(0, Math.min(1000, (10-rand.nextInt(21))+cr.r.x)), 
						Math.max(0, Math.min(1000, (10-rand.nextInt(21))+cr.r.y)), 
						cr.getMovement().cloneAndMutate());
	//			Critter cn = new Critter(rand.nextInt(1000), rand.nextInt(1000),  cr.getMovement().cloneAndMutate());
				cn.energy = cr.energy / 2;
//				cr.getMovement().setEnergy(cr.getMovement().getEnergy() - cr.getMovement().getEnergy() / 4);
				screenItems.critters.add(cn);
			}
		}
/*		
		Collections.sort(screenItems.critters, (c1, c2)-> {return c2.getMovement().getEnergy() - c1.getMovement().getEnergy();} );
		cSize = screenItems.critters.size();
		for ( int i = 10 - cSize; i > 0; --i) {
			Critter cr = screenItems.critters.get(rand.nextInt(Math.min(10, cSize)));
	//			Critter cn = new Critter(cr.x,  cr.y, cr.getMovement().cloneAndMutate());
				Critter cn = new Critter(
						Math.max(0, Math.min(1000, (10-rand.nextInt(21))+cr.x)), 
						Math.max(0, Math.min(1000, (10-rand.nextInt(21))+cr.y)), 
						cr.getMovement());
	//			Critter cn = new Critter(rand.nextInt(1000), rand.nextInt(1000),  cr.getMovement().cloneAndMutate());
				cn.getMovement().setEnergy(cr.getMovement().getEnergy() / 4);
				cr.getMovement().setEnergy(cr.getMovement().getEnergy() - cr.getMovement().getEnergy() / 4);
//				cn.getMovement().setEnergy(cr.getMovement().getEnergy());
				screenItems.critters.add(cn);
		}
*/		
	}

	public void drawScreenItems(Graphics2D g2d) {
		synchronized( screenItems ) {
			g2d.setColor(Color.RED);
			screenItems.critters.forEach(c->g2d.fillOval(c.r.x, c.r.y, cSize, cSize));	    		
			g2d.setColor(Color.GREEN);
			screenItems.foodStuffs.forEach(f->g2d.fillOval(f.r.x, f.r.y, cSize, cSize));
		}
	}
}

