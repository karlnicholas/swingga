package swingga;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
	public SimulationThread(MyPanel myPanel) {
		this.myPanel = myPanel;
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
					c.move();
					if ( c.getMovement().getEnergy() < 0 ) {
						cit.remove();
						continue;
					}
					// check for food found, add to 
		    		Rectangle r1 = new Rectangle(c.x, c.y, cSize, cSize);
					Iterator<Food> fit = screenItems.foodStuffs.iterator();
		    		while ( fit.hasNext() ) {
		    			Food f = fit.next();
			    		Rectangle r2 = new Rectangle(f.x, f.y, cSize, cSize);
			    		if ( r1.intersects(r2) ) {
			    			int newE = c.getMovement().getEnergy() + 1000;
			    			if (newE > 10000) newE = 10000;
			    			c.getMovement().setEnergy(newE);
			    			fit.remove();
			    			break;
			    		}
			    	}

					// check for collisions, remove from list
		    		r1 = new Rectangle(c.x, c.y, cSize, cSize);
			    	for ( Critter c2: screenItems.critters ) {
			    		if ( c == c2) continue;
			    		Rectangle r2 = new Rectangle(c2.x, c2.y, cSize, cSize);
			    		if ( r1.intersects(r2) ) {
			    			int e1 = c.getMovement().getEnergy();
			    			int e2 = c2.getMovement().getEnergy();
			    			if ( e1 > e2 ) {
	//		    				cit.remove();
			    				c2.x = c2.x + (20 - rand.nextInt(41));
			    				c2.y = c2.y + (20 - rand.nextInt(41));
			    				c2.checkBounds();
			    				c2.getMovement().setEnergy( c2.getMovement().getEnergy() - 40);
			    			} else if ( e2 > e1 ) {
			    				c.x = c.x + (20 - rand.nextInt(41));
			    				c.y = c.y + (20 - rand.nextInt(41));
			    				c.checkBounds();
			    				c.getMovement().setEnergy( c.getMovement().getEnergy() - 40);
			    			}
			    			break;
			    		}
			    	}
			    	
				}
		    	if ( screenItems.critters.size() == 0 ) {
		    		c.getMovement().setEnergy(100);
		    		screenItems.critters.add(c);
		    	}
				myPanel.repaint();
				if ( counter % 10 == 0 ) {
	//				printAverages();
					reproduceAndMutate();
				}
				if ( counter % 10 == 0 ) {
					dropFood();
				}
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

	private void printAverages() {
		int cSize = screenItems.critters.size();
		int xRandMax = 0; 
		int yRandMax = 0;
		int xRandOff = 0;
		int yRandOff = 0;
		int xRandLimit = 0;
		int yRandLimit = 0;
		for ( int i = 0 ; i < cSize; ++i) {
			Critter cr = screenItems.critters.get(i);
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
	public void drawScreenItems(Graphics2D g2d) {
		synchronized( screenItems ) {
			g2d.setColor(Color.RED);
			screenItems.critters.forEach(c->g2d.fillOval(c.x, c.y, cSize, cSize));	    		
			g2d.setColor(Color.GREEN);
			screenItems.foodStuffs.forEach(f->g2d.fillOval(f.x, f.y, cSize, cSize));
		}
	}
}

