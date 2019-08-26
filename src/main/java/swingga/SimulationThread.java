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
	private ScreenItems screenItems;
	private static final Random rand = new Random();
	public final static int cSize = 10;
	public final static int huntChance = 10;
	public final static int MAX_CRITTERS = 500;
	public boolean run = true;
	private int counter = 0;
	private MyPanel myPanel;
	private Offset randomJumpOffset;
	public SimulationThread(MyPanel myPanel) {
		this.myPanel = myPanel;
		randomJumpOffset = new Offset();
		screenItems = new ScreenItems();
		screenItems.gatheringCritters = new ArrayList<>();
		screenItems.hunterCritters = new ArrayList<>();
		screenItems.foodStuffs = new ArrayList<>();
		for ( int i = 0; i < 50; ++i ) {
			screenItems.gatheringCritters.add(new Critter(
				cSize + (int)(Math.random() * 900), cSize + (int)(Math.random() * 900), 
				new CritterTuringMovement(), 
				rand.nextInt(7999)+2000, 
				rand.nextInt(79)+20
			));
		}
		for ( int i = 0; i < 50; ++i ) {
			screenItems.hunterCritters.add(new Critter(
				cSize + (int)(Math.random() * 900), cSize + (int)(Math.random() * 900), 
				new CritterTuringMovement(), 
				rand.nextInt(7999)+2000, 
				rand.nextInt(79)+20
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
				Thread.sleep(50);
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
		if ( screenItems.hunterCritters.size() == 0 ) {
			c.energy = 100;
			c.living = true;
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
		if ( screenItems.gatheringCritters.size() == 0 ) {
			c.energy = 100;
			c.living = true;
			screenItems.gatheringCritters.add(c);
		}
	}
	
	private boolean handleGatheringCritterMoveAndCheckDeath(Critter c) {
		Offset offset = c.getMovement().getMovement(c);
		c.move(offset);

		// check for collisions
		for( Critter c2: screenItems.gatheringCritters ) {
			if ( c2.living == false ) continue;
    		if ( c == c2) continue;
    		if ( c.r.intersects(c2.r) ) {
    			handleFoodCritterCollision(c, c2, offset);
    			break;
    		}
    	}
		assessMovementCost(c, offset);
		if ( c.energy <= 0  ) {
			return true;
		}
		return false;
	}
	
	private boolean handleHunterCritterMoveAndCheckDeath(Critter c) {
		Offset offset = c.getMovement().getMovement(c);
		c.move(offset);

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
	
	private void handleFoodCritterCollision(Critter c, Critter c2, Offset offset) {
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
	private void handleHunterCritterFoodCollision(Critter c, Critter c2, Offset offset) {
		if ( rand.nextInt(huntChance) == 0 ) {
			c.energy += c2.energy;
			if ( c.energy > 10000)
				c.energy = 10000;
			c2.living = false;
		} else {
			c2.move(getRandomJump());
			c2.energy -= 40;
		}
	}
	private void handleHunterCritterHunterCollision(Critter c, Critter c2, Offset offset) {
		if ( c.energy > c2.energy && rand.nextInt(huntChance) == 0 ) {
			c.energy += c2.energy;
			if ( c.energy > 10000)
				c.energy = 10000;
			c2.living = false;
		} else {
			c2.move(getRandomJump());
			c2.energy -= 40;
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
	private Critter reproduceAndMutateGatheringCritter(Critter c) {
		// genetic reproduction callback code
		Critter cn = new Critter(
				Math.max(0, Math.min(1000, (10-rand.nextInt(21))+c.r.x)), 
				Math.max(0, Math.min(1000, (10-rand.nextInt(21))+c.r.y)), 
				c.getMovement().cloneAndMutate(), 
				(rand.nextInt( 10 ) == 0 ?  Math.max(2000, Math.min(8000, (10-rand.nextInt(21))+c.repEnergy)) : c.repEnergy),  
				(rand.nextInt( 10 ) == 0 ?  Math.max(20, Math.min(80, (1-rand.nextInt(3))+c.offspringPercent)) : c.offspringPercent)  
				);
		// 
		cn.energy = c.energy / (100 / c.offspringPercent);
		c.energy =  c.energy - (c.energy / (100 / c.offspringPercent));
		return cn;
	}
	private Critter reproduceAndMutateHunterCritter(Critter c) {
		// genetic reproduction callback code
		Critter cn = new Critter(
				Math.max(0, Math.min(1000, (10-rand.nextInt(21))+c.r.x)), 
				Math.max(0, Math.min(1000, (10-rand.nextInt(21))+c.r.y)), 
				c.getMovement().cloneAndMutate(), 
				(rand.nextInt( 10 ) == 0 ?  Math.max(2000, Math.min(8000, (10-rand.nextInt(21))+c.repEnergy)) : c.repEnergy),  
				(rand.nextInt( 10 ) == 0 ?  Math.max(20, Math.min(80, (1-rand.nextInt(3))+c.offspringPercent)) : c.offspringPercent)  
			);
		cn.energy = c.energy / (100 / c.offspringPercent);
		c.energy =  c.energy - (c.energy / (100 / c.offspringPercent));
		return cn;
	}

	public void drawScreenItems(Graphics2D g2d) {
		synchronized( screenItems ) {
			g2d.setColor(Color.BLUE);
			screenItems.gatheringCritters.forEach(c->g2d.fillOval(c.r.x, c.r.y, cSize, cSize));	    		
			g2d.setColor(Color.RED);
			screenItems.hunterCritters.forEach(c->g2d.fillOval(c.r.x, c.r.y, cSize, cSize));	    		
			g2d.setColor(Color.GREEN);
			screenItems.foodStuffs.forEach(f->g2d.fillOval(f.r.x, f.r.y, cSize, cSize));
		}
	}
}

