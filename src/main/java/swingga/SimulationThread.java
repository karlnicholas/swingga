package swingga;

import java.awt.Color;
import java.awt.Graphics2D;
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
	public ScreenItems screenItems;
	private static final Random rand = new Random();
	public final static int cSize = 10;
	public final static int huntChance = 10;
	public final static int MAX_CRITTERS = 500;
	public static enum MOVE_FUNCTION {FOOD, GATHER, HUNTER, FULL, HUNGRY}
	public static final int MAX_MOVEMENT = 10;
	public boolean run = true;
	private int counter = 0;
	private MyPanel myPanel;
	public SimulationThread(MyPanel myPanel) {
		this.myPanel = myPanel;
		screenItems = new ScreenItems();
		screenItems.gatheringCritters = new ArrayList<>();
		screenItems.hunterCritters = new ArrayList<>();
		screenItems.foodStuffs = new ArrayList<>();
		for ( int i = 0; i < 50; ++i ) {
			screenItems.gatheringCritters.add(new Critter(
				cSize + (int)(Math.random() * 900), cSize + (int)(Math.random() * 900), 
				new CritterTuringMovement(), 
				rand.nextInt(7950)+2000, 
				rand.nextInt(60)+20
			));
		}
		for ( int i = 0; i < 50; ++i ) {
			screenItems.hunterCritters.add(new Critter(
				cSize + (int)(Math.random() * 900), cSize + (int)(Math.random() * 900), 
				new CritterTuringMovement(), 
				rand.nextInt(7950)+2000, 
				rand.nextInt(60)+20
			));
		}
	}
	/**
	 * Main loop of critter thread. Does one "step" in critter lifecycle and calls repaint screen.
	 */
	@Override
	public void run() {
		while(run) {
			try {
				Thread.sleep(75);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			counter++;
			synchronized( screenItems ) {
	
				
				stepGatheringCritters();

				stepHunterCritters();

				if ( counter % 10 == 0 ) {
					dropFood();
				}
				myPanel.repaint();
			}
		}
	}
	private void stepHunterCritters() {
		// move everything
		Iterator<Critter> cit = screenItems.hunterCritters.iterator();
		Critter c = null;
		while ( cit.hasNext() ) {
			c = cit.next();
			if ( handleHunterCritterMoveAndCheckDeath(c) ) { 
				c.living = false;
				continue;
			}
		}
		// check still living
		cit = screenItems.hunterCritters.iterator();
		c = null;
		while ( cit.hasNext() ) {
			c = cit.next();
			if ( c.living == false)
				cit.remove();
		}
		// check if reproducing
		List<Critter> babies = new ArrayList<>();
		for ( Critter cr: screenItems.hunterCritters) {
			if ( cr.energy > cr.repEnergy ) {
				babies.add( reproduceAndMutateHunterCritter(cr) );
			}
		}
		Collections.shuffle(babies);
		int maxBabies = MAX_CRITTERS - screenItems.hunterCritters.size();
		cit = babies.iterator();
		while ( cit.hasNext() ) {
			cit.next();
			if ( maxBabies-- > 0 ) { 
				continue;
			}
			cit.remove();
		}
		screenItems.hunterCritters.addAll(babies);
		// just in case everything dies
		// take another generation.
		if ( screenItems.hunterCritters.size() == 0 ) {
			c = reproduceAndMutateHunterCritter(c);
			c.energy = 500;
			screenItems.hunterCritters.add(c);
		}
	}
	private void stepGatheringCritters() {
		// move gathering critters
		Iterator<Critter> cit = screenItems.gatheringCritters.iterator();
		Critter c = null;
		while ( cit.hasNext() ) {
			c = cit.next();
			if ( handleGatheringCritterMoveAndCheckDeath(c) ) { 
				c.living = false;
				continue;
			}
			checkFoodFound(c);
		}
		// check still living
		cit = screenItems.gatheringCritters.iterator();
		c = null;
		while ( cit.hasNext() ) {
			c = cit.next();
			if ( c.living == false)
				cit.remove();
		}
		// check if reproducing
		List<Critter> babies = new ArrayList<>();
		for ( Critter cr: screenItems.gatheringCritters) {
			if ( cr.energy > cr.repEnergy ) {
				babies.add( reproduceAndMutateGatheringCritter(cr) );
			}
		}
		Collections.shuffle(babies);
		int maxBabies = MAX_CRITTERS - screenItems.gatheringCritters.size();
		cit = babies.iterator();
		while ( cit.hasNext() ) {
			cit.next();
			if ( maxBabies-- > 0 ) { 
				continue;
			}
			cit.remove();
		}
		screenItems.gatheringCritters.addAll(babies);
		// just in case everything dies
		// take another generation.
		if ( screenItems.gatheringCritters.size() == 0 ) {
			c = reproduceAndMutateGatheringCritter(c);
			c.energy = 500;
			screenItems.gatheringCritters.add(c);
		}
	}
	
	private boolean handleGatheringCritterMoveAndCheckDeath(Critter c) {
		Offset offset = c.getMovement().getOffset();
		if ( c.energy > 1000 ) {
			c.hungry = false;
		} else if ( c.energy < 1000 && c.hungry == false ) {
			c.hungry = true;
			offset = c.getMovement().getCollision(MOVE_FUNCTION.HUNGRY);
		}
		c.move(offset);
		assessMovementCost(c, offset);

		// check for collisions
		for( Critter c2: screenItems.gatheringCritters ) {
			if ( c2.living == false ) continue;
    		if ( c == c2) continue;
    		if ( c.r.intersects(c2.r) ) {
    			handleFoodCritterCollision(c, c2);
    			break;
    		}
    	}
		if ( c.energy <= 0  ) {
			return true;
		}
		return false;
	}
	
	private boolean handleHunterCritterMoveAndCheckDeath(Critter c) {
		Offset offset = c.getMovement().getOffset();
		if ( c.energy > 1000 ) {
			c.hungry = false;
		} else if ( c.energy < 1000 && c.hungry == false ) {
			c.hungry = true;
			offset = c.getMovement().getCollision(MOVE_FUNCTION.HUNGRY);
		}
		c.move(offset);
		assessMovementCost(c, offset);

		// check for collisions
		for( Critter c2: screenItems.gatheringCritters ) {
			if ( c2.living == false ) continue;
    		if ( c == c2) continue;
    		if ( c.r.intersects(c2.r) ) {
    			handleHunterCritterFoodCollision(c, c2, offset);
    			break;
    		}
    	}
		// check for hunter collisions
		for( Critter c2: screenItems.hunterCritters ) {
			if ( c2.living == false ) continue;
    		if ( c == c2) continue;
    		if ( c.r.intersects(c2.r) ) {
    			handleHunterCritterHunterCollision(c, c2, offset);
    			break;
    		}
    	}
		if ( c.energy <= 0  ) {
			return true;
		}
		return false;
	}
	
	private void assessMovementCost(Critter c, Offset offset) {
		c.energy -= Math.max( Math.hypot(Math.abs(offset.mx), Math.abs(offset.my)), 1);
	}
	
	private void handleFoodCritterCollision(Critter c, Critter c2) {
			Offset jOffset1 = c2.getMovement().getCollision(MOVE_FUNCTION.GATHER);
			c2.move(jOffset1);
			Offset jOffset2 = c.getMovement().getCollision(MOVE_FUNCTION.GATHER);
			c.move(jOffset2);
			if ( c.r.intersects(c2.r)) {
				jOffset1.mx = jOffset1.my = jOffset2.mx = jOffset2.my = MAX_MOVEMENT;
			}
			assessMovementCost(c2, jOffset1);
			assessMovementCost(c, jOffset2);
/*
			offset.mx = 0 - offset.mx; 
			offset.my = 0 - offset.my; 
			c.move(offset);
			offset.mx = 0; 
			offset.my = 0;
*/			 
	}
	private void handleHunterCritterFoodCollision(Critter c, Critter c2, Offset offset) {
		int oneIn = (10000-(c.energy-c2.energy))/1000+1;
		if ( rand.nextInt(Math.max(3,oneIn)) == 0 ) {		
			c.energy += c2.energy;
			if ( c.energy > 10000) {
				c.energy = 10000;
				Offset jOffset = c.getMovement().getCollision(MOVE_FUNCTION.FULL);
				c.move(jOffset);
				assessMovementCost(c, jOffset);
			}
			c2.living = false;
		} else {
			Offset jOffset = c.getMovement().getCollision(MOVE_FUNCTION.GATHER);
			c.move(jOffset);
			assessMovementCost(c, jOffset);
			jOffset = c2.getMovement().getCollision(MOVE_FUNCTION.HUNTER);
			c2.move(jOffset);
			assessMovementCost(c2, jOffset);
		}
	}
	private void handleHunterCritterHunterCollision(Critter c, Critter c2, Offset offset) {
		int oneIn = (10000-(c.energy-c2.energy))/1000+1;
		if ( rand.nextInt(Math.max(1,oneIn)) == 0 ) {		
			c.energy += c2.energy;
			if ( c.energy > 10000) {
				c.energy = 10000;
				Offset jOffset = c.getMovement().getCollision(MOVE_FUNCTION.FULL);
				c.move(jOffset);
				assessMovementCost(c, jOffset);
			}
			c2.living = false;
		} else {			
			Offset jOffset = c2.getMovement().getCollision(MOVE_FUNCTION.HUNTER);
			c2.move(jOffset);
			assessMovementCost(c2, jOffset);
		}
	}
	private void checkFoodFound(Critter c) {
		// check for food found, add to 
		Iterator<Food> fit = screenItems.foodStuffs.iterator();
		while ( fit.hasNext() ) {
			Food f = fit.next();
    		if ( c.r.intersects(f.r) ) {
    			if ( rand.nextInt(Math.max(1, c.energy/1000+1)) == 0 ) {
	    			int newE = c.energy + 1000;
	    			if (newE > 10000) {
	    				newE = 10000;
	    				Offset jOffset = c.getMovement().getCollision(MOVE_FUNCTION.FULL);
	    				c.move(jOffset);
	    				assessMovementCost(c, jOffset);
	    			}
	    			c.energy = newE;
	    			fit.remove();
	    			Offset jOffset = c.getMovement().getCollision(MOVE_FUNCTION.FOOD);
	    			c.move(jOffset);
	    			assessMovementCost(c, jOffset);
    			}
    			break;
    		} 
    	}
	}
	
	private void dropFood() {
		Iterator<Food> fit = screenItems.foodStuffs.iterator();
		while ( fit.hasNext() ) {
			Food f = fit.next();
			f.energy -= 5;
			if ( f.energy <= 0 ) {
				fit.remove();
			}
		}
		for ( int i = 0; i < 30; ++i) {
			Food f = new Food(rand);
			boolean no = true;
			for ( Food fe: screenItems.foodStuffs ) {
				if ( fe.r.intersects(f.r)) {
					no = false;
					break;
				}
			}
			if ( no ) 
				screenItems.foodStuffs.add(f);
		}
/*		
		int fSize = screenItems.foodStuffs.size();
		for ( int i = 100 - fSize; i > 0; --i) {
			screenItems.foodStuffs.add(new Food(rand));
		}
*/		
	}
	private Critter reproduceAndMutateGatheringCritter(Critter c) {
		// genetic reproduction callback code
		int re1 = (rand.nextInt( 10 ) == 0 ?  Math.max(2000, Math.min(9950, (10-rand.nextInt(21))+c.repEnergy)) : c.repEnergy);  
		int op1 = (rand.nextInt( 10 ) == 0 ?  Math.max(20, Math.min(80, (1-rand.nextInt(3))+c.offspringPercent)) : c.offspringPercent);  
		int re2 = rand.nextInt(7950)+2000; 
		int op2 = rand.nextInt(60)+20;
		Critter cn = new Critter(
				Math.max(0, Math.min(1000, (10-rand.nextInt(21))+c.r.x)), 
				Math.max(0, Math.min(1000, (10-rand.nextInt(21))+c.r.y)), 
				c.getMovement().cloneAndMutate(), 
				(rand.nextBoolean() ? re1 : re2),  
				(rand.nextBoolean() ? op1 : op2)  
				);
		// 
		int eGiven = Math.round(((float)c.energy) / (100.0f / ((float)c.offspringPercent)));
		cn.energy = eGiven;
		c.energy =  c.energy - eGiven;
		return cn;
	}
	private Critter reproduceAndMutateHunterCritter(Critter c) {
		// genetic reproduction callback code
		int re1 = (rand.nextInt( 10 ) == 0 ?  Math.max(2000, Math.min(9950, (10-rand.nextInt(21))+c.repEnergy)) : c.repEnergy);  
		int op1 = (rand.nextInt( 10 ) == 0 ?  Math.max(20, Math.min(80, (1-rand.nextInt(3))+c.offspringPercent)) : c.offspringPercent);  
		int re2 = rand.nextInt(7950)+2000; 
		int op2 = rand.nextInt(60)+20;
		Critter cn = new Critter(
				Math.max(0, Math.min(1000, (10-rand.nextInt(21))+c.r.x)), 
				Math.max(0, Math.min(1000, (10-rand.nextInt(21))+c.r.y)), 
				c.getMovement().cloneAndMutate(), 
				(rand.nextBoolean() ? re1 : re2),  
				(rand.nextBoolean() ? op1 : op2)  
			);
		int eGiven = Math.round(((float)c.energy) / (100.0f / ((float)c.offspringPercent)));
		cn.energy = eGiven;
		c.energy =  c.energy - eGiven;
		return cn;
	}

	public void drawScreenItems(Graphics2D g2d) {
		synchronized( screenItems ) {
			g2d.setColor(Color.GREEN);
			screenItems.foodStuffs.forEach(f->g2d.fillOval(f.r.x, f.r.y, cSize, cSize));
			g2d.setColor(Color.BLUE);
			screenItems.gatheringCritters.forEach(c->g2d.fillOval(c.r.x, c.r.y, cSize, cSize));	    		
			g2d.setColor(Color.RED);
			screenItems.hunterCritters.forEach(c->g2d.fillOval(c.r.x, c.r.y, cSize, cSize));	    		
		}
	}
}

