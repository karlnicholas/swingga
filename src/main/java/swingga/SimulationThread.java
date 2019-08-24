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
					c.move(c.getMovement().getMovement(c));
					if ( !c.getMovement().checkEnergy() ) {
						cit.remove();
						continue;
					}
					checkFoodFound(c);
					// check for collisions, remove from list
			    	for ( Critter c2: screenItems.critters ) {
			    		if ( c == c2) continue;
			    		if ( c.intersects(c2.rectangle) ) {
			    			handleCollision(c, c2);
			    			break;
			    		}
			    	}
				}
		    	if ( screenItems.critters.size() == 0 ) {
		    		c.getMovement().setEnergy(100);
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
	
	Offset getRandomJump() {
		randomJumpOffset.mx = (20 - rand.nextInt(41));
		randomJumpOffset.my = (20 - rand.nextInt(41));
		return randomJumpOffset;
	}
	
	private void handleCollision(Critter c, Critter c2) {
		int e1 = c.getMovement().getEnergy();
		int e2 = c2.getMovement().getEnergy();
		if ( e1 > e2 ) {
//		    				cit.remove();
			c2.move(getRandomJump());
			c2.getMovement().setEnergy( c2.getMovement().getEnergy() - 40);
		} else if ( e2 > e1 ) {
			c.move(getRandomJump());
			c.getMovement().setEnergy( c.getMovement().getEnergy() - 40);
		}
	}
	private void checkFoodFound(Critter c) {
		// check for food found, add to 
		Iterator<Food> fit = screenItems.foodStuffs.iterator();
		while ( fit.hasNext() ) {
			Food f = fit.next();
    		if ( c.intersects(f.rectangle) ) {
    			int newE = c.getMovement().getEnergy() + 1000;
    			if (newE > 10000) newE = 10000;
    			c.getMovement().setEnergy(newE);
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
		Collections.sort(screenItems.critters, (c1, c2)-> {return c2.getMovement().getEnergy() - c1.getMovement().getEnergy();} );
		int cSize = screenItems.critters.size();
		for ( int i = 100 - cSize; i > 0; --i) {
			Critter cr = screenItems.critters.get(rand.nextInt(Math.min(10, cSize)));
			if ( cr.getMovement().getEnergy() > 200 ) {
	//			Critter cn = new Critter(cr.x,  cr.y, cr.getMovement().cloneAndMutate());
				Critter cn = new Critter(
						Math.max(0, Math.min(1000, (10-rand.nextInt(21))+cr.x)), 
						Math.max(0, Math.min(1000, (10-rand.nextInt(21))+cr.y)), 
						cr.getMovement().cloneAndMutate());
	//			Critter cn = new Critter(rand.nextInt(1000), rand.nextInt(1000),  cr.getMovement().cloneAndMutate());
				cn.getMovement().setEnergy(cr.getMovement().getEnergy() / 2);
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
			screenItems.critters.forEach(c->g2d.fillOval(c.x, c.y, cSize, cSize));	    		
			g2d.setColor(Color.GREEN);
			screenItems.foodStuffs.forEach(f->g2d.fillOval(f.x, f.y, cSize, cSize));
		}
	}
}

