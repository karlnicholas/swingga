package swingga;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	public final static int MAX_ENERGY = 10000;
	public final static int HUNGRY_LEVEL = 1000;
	public final static int FULL_LEVEL = 9000;
	public final static int GATHER_EAT_FOOD_ENERGY = 300;
	public final static int HUNTER_EAT_FOOD_ENERGY = 1200;
//	public final static int CRITTER_MAX_BITE_ENERGY = 10000;
	public final static int HUNTER_FIGHT_ENERGY = 500;
	public final static int HUNTER_SUCCESS_MIN_CHANCE = 5;
	public final static int MAX_REP_LEVEL = 80;
	public final static int MIN_REP_LEVEL = 20;
	public final static int MAX_X = 1000;
	public final static int MAX_Y = 1000;	
	public static enum BEHAVIOR_MODES {FOOD, GATHER, HUNTER, FULL, NORMAL, HUNGRY}
	public static final int MAX_MOVEMENT = 10;
	public boolean run = true;
	private MyPanel myPanel;
	private int counter;
	
	private final ExecutorService pool;
	private final Collection<? extends Callable<Void>> tasks;
	
	public SimulationThread(MyPanel myPanel) {
		this.myPanel = myPanel;
		pool = Executors.newFixedThreadPool(3);
		//Collection<? extends Callable<?>> tasks = Stream.of(
		Callable<Void> t1 = () -> {				
			screenItems.gatheringCritters = stepCritters(
				screenItems.gatheringCritters,  
				this::handleGatherMove
			);
			return null;
		};
		Callable<Void> t2 =  () -> { 				
			screenItems.hunterCritters = stepCritters(
				screenItems.hunterCritters,  
				this::handleHunterMove
			);
			return null;
		};
		Callable<Void> t3 =  () -> { 				
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		};
			
		tasks = Stream.of(t1, t2, t3 ).collect(Collectors.toList());
		screenItems = new ScreenItems();
		screenItems.gatheringCritters = new ArrayList<>();
		screenItems.hunterCritters = new ArrayList<>();
		screenItems.foodStuffs = new ArrayList<>();
		for ( int i = 0; i < 50; ++i ) {
			screenItems.gatheringCritters.add(new Critter(
				rand.nextInt(MAX_X), rand.nextInt(MAX_X), 
				new CritterTuringMovement(), 
				rand.nextInt(FULL_LEVEL - HUNGRY_LEVEL)+HUNGRY_LEVEL, 
				rand.nextInt(MAX_REP_LEVEL - MIN_REP_LEVEL)+MIN_REP_LEVEL, 
				rand.nextInt(MAX_ENERGY), 
				rand.nextInt(10)+1
			));
		}
		for ( int i = 0; i < 50; ++i ) {
			screenItems.hunterCritters.add(new Critter(
					rand.nextInt(MAX_X), rand.nextInt(MAX_X), 
				new CritterTuringMovement(), 
				rand.nextInt(FULL_LEVEL - HUNGRY_LEVEL)+HUNGRY_LEVEL, 
				rand.nextInt(MAX_REP_LEVEL - MIN_REP_LEVEL)+MIN_REP_LEVEL, 
				rand.nextInt(MAX_ENERGY), 
				rand.nextInt(10)+1
			));
		}
	}
	/**
	 * Main loop of critter thread. Does one "step" in critter lifecycle and calls repaint screen.
	 */
	@Override
	public void run() {
		while(run) {
			counter++;
			try {
				List<Future<Void>> fs = pool.invokeAll(tasks);
				fs.get(0).get();
				fs.get(1).get();
				synchronized( screenItems ) {
					// check for gather collisions
					for( Critter h: screenItems.hunterCritters ) {
						for( Critter g: screenItems.gatheringCritters ) {
							if ( h.living == false || g.living == false ) continue;
				    		if ( h.r.intersects(g.r) ) {
		//		    			collisionHunterOnGather(c, c2, ()->rand.nextInt(Math.max(3,Math.max( c2.energy - c.energy/3, 0)/1000+1)) == 0);
				    			collisionHunterOnGather(h, g);
	//			    			break;
				    		}
				    	}
					}
					removeEatenFood();
	/*				
	//				if ( counter % 100 == 0 ) {
					if ( rand.nextInt(50) == 0 ) {
						dropFood();
					}
	*/				
					for ( int i = 0; i < 20; ++i) {
						dropOneFood();
					}
					fs.get(2).get();
					myPanel.repaint();
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

		}
	}
	private List<Critter> stepCritters(List<Critter> critters, Predicate<Critter> moveHandler) {
		synchronized( screenItems ) {
			// move everything
			Critter c = critters.get(0);
			critters = critters.parallelStream()
			.filter(moveHandler)
			.flatMap(
					cr->cr.energy > cr.repEnergy ? 
							Stream.of( cr, reproduceAndMutateCritter(cr)):
							Stream.of( cr ))
			.collect(Collectors.collectingAndThen(
				      Collectors.toList(),
				      list -> {
				          Collections.shuffle(list);
				          return list.stream();
				      })
					)
			.limit(MAX_CRITTERS)
			.collect(Collectors.toList());
			// just in case everything dies
			// take another generation.
			if ( critters.size() == 0 ) {
				c = reproduceAndMutateCritter(c);
				c.energy = 500;
				critters.add(c);
			}
		}
		return critters;
	}

	private boolean handleHunterMove(Critter c) {
		int oldE = c.energy;
		Offset offset = c.getMovement().getOffset();
		c.move(offset);
		assessMovementCost(c, offset);

		/*		
		// check for gather collisions
		for( Critter c2: screenItems.gatheringCritters ) {
			if ( c2.living == false ) continue;
    		if ( c.r.intersects(c2.r) ) {
//    			collisionHunterOnGather(c, c2, ()->rand.nextInt(Math.max(3,Math.max( c2.energy - c.energy/3, 0)/1000+1)) == 0);
    			collisionHunterOnGather(c, c2, ()->rand.nextInt(7) == 0);
    			break;
    		}
    	}
		// check for hunter collisions
		for( Critter c2: screenItems.hunterCritters ) {
			if ( c2.living == false ) continue;
    		if ( c == c2) continue;
    		if ( c.r.intersects(c2.r) ) {
    			collisionHunterOnHunter(c, c2, ()->rand.nextInt(Math.max(5,(MAX_ENERGY-(c.energy-c2.energy))/1000+1)) == 0);
    			break;
    		}
    	}
*/    	
		int newE = c.energy;
		checkEnergyChange(c, oldE, newE);
		return c.energy > 0 && c.living;
	}

	private boolean handleGatherMove(Critter c) {
		int oldE = c.energy;
		Offset offset = c.getMovement().getOffset();
		c.move(offset);
		assessMovementCost(c, offset);
/*
		// check for collisions
		for( Critter c2: screenItems.gatheringCritters ) {
			if ( c2.living == false ) continue;
    		if ( c == c2) continue;
    		if ( c.r.intersects(c2.r) ) {
    			collisionGatherOnGather(c, c2);
    			break;
    		}
    	}
		// check for collisions
		// check for gather collisions
		for( Critter c2: screenItems.hunterCritters ) {
			if ( c2.living == false ) continue;
    		if ( c.r.intersects(c2.r) ) {
//    			collisionHunterOnGather(c, c2, ()->rand.nextInt(Math.max(3,Math.max( c2.energy - c.energy/3, 0)/1000+1)) == 0);
    			collisionHunterOnGather(c2, c, ()->rand.nextInt(7) == 0);
    			break;
    		}
    	}
*/    	
		checkFoodFound(c);
		int newE = c.energy;
		checkEnergyChange(c, oldE, newE);
		return c.energy > 0 && c.living;
	}
	
	private void checkEnergyChange(Critter c, int oldE, int newE) {
		if ( Util.crossedBelow(oldE, newE, HUNGRY_LEVEL) ) {
			c.getMovement().getCollision(BEHAVIOR_MODES.HUNGRY);
		} else if ( Util.crossedAbove(oldE, newE, HUNGRY_LEVEL) ) { 
			c.getMovement().getCollision(BEHAVIOR_MODES.NORMAL);
		} else if ( Util.crossedBelow(oldE, newE, FULL_LEVEL) ) {
			c.getMovement().getCollision(BEHAVIOR_MODES.NORMAL);
		} else if ( Util.crossedAbove(oldE, newE, HUNGRY_LEVEL) ) { 
			c.getMovement().getCollision(BEHAVIOR_MODES.FULL);
		}
	}
	
	private void assessMovementCost(Critter c, Offset offset) {
		int amount =(int) Math.hypot(Math.abs(offset.mx), Math.abs(offset.my)) + 1;
		c.energy -= amount;
	}
	
	private void collisionGatherOnGather(Critter c, Critter c2) {
//			Offset jOffset1 = c2.getMovement().getCollision(BEHAVIOR_MODES.GATHER);
//			c2.move(jOffset1);
			c.getMovement().getCollision(BEHAVIOR_MODES.GATHER);
//			if ( c.r.intersects(c2.r)) {
//				jOffset1.mx = jOffset1.my = jOffset2.mx = jOffset2.my = MAX_MOVEMENT;
//			}
//			assessMovementCost(c2, jOffset1);
/*
			offset.mx = 0 - offset.mx; 
			offset.my = 0 - offset.my; 
			c.move(offset);
			offset.mx = 0; 
			offset.my = 0;
*/			 
	}
	private void collisionHunterOnGather(Critter c, Critter c2) {
//		int oneIn = c2.energy/1000+1;
//		int oneIn = Math.max(c.energy, c2.energy)/1000+1;
		if ( c2.living ) {
			if ( c.energy < FULL_LEVEL && c2.energy < c.biteEnergy * c.huntChance && rand.nextInt(c.huntChance) == 0 ) {
	//			c.energy += c2.energy;
				c.energy += HUNTER_EAT_FOOD_ENERGY - c.biteEnergy/c.huntChance;
				c2.living = false;
			} else {
//				c.energy -= ( Math.min( c.energy, c.biteEnergy) + Math.min( c2.energy, c2.biteEnergy));
				c2.getMovement().getCollision(BEHAVIOR_MODES.HUNTER);
				int c2b = Math.min( c2.biteEnergy, c2.energy)/c.huntChance;
				c.energy -= c2b;
				c2.energy -= c2b;
			}
			c.getMovement().getCollision(BEHAVIOR_MODES.GATHER);
			if ( c.energy <= 0 ) c.living = false;
			if ( c2.energy <= 0 ) c2.living = false;
		}
	}
	private void collisionHunterOnHunter(Critter c, Critter c2, ChanceFunction chanceFunction) {
		if ( c2.living ) {
			if ( c.energy < FULL_LEVEL && chanceFunction.evaluateChance() ) {		
				c.energy = Util.within(0, MAX_ENERGY, c.energy + HUNTER_EAT_FOOD_ENERGY/2);
				c2.living = false;
			} else if ( c.energy > c2.energy ){			
				c2.getMovement().getCollision(BEHAVIOR_MODES.HUNTER);
			} else if ( c2.energy > c.energy ){			
				c.getMovement().getCollision(BEHAVIOR_MODES.HUNTER);
			}
			c2.energy -= HUNTER_FIGHT_ENERGY;
			c.energy -= HUNTER_FIGHT_ENERGY;
		}
	}
	private void checkFoodFound(Critter c) {
		if ( c.energy >= FULL_LEVEL )
			return;
		// check for food found, add to 
		Iterator<Food> fit = screenItems.foodStuffs.iterator();
		while ( fit.hasNext() ) {
			Food f = fit.next();
    		if ( c.r.intersects(f.r) ) {
    			if( f.eaten.compareAndSet(false, true) ) { 
    				c.energy = Util.within(0, MAX_ENERGY, c.energy + GATHER_EAT_FOOD_ENERGY);
	    			c.getMovement().getCollision(BEHAVIOR_MODES.FOOD);
    			}
    			break;
    		} 
    	}
	}
	
	private void removeEatenFood() {
		Iterator<Food> fit = screenItems.foodStuffs.iterator();
		while ( fit.hasNext() ) {
			Food f = fit.next();
			if ( f.eaten.get() ) {
				fit.remove();
			}
		}
	}
	private void dropOneFood() {
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
	private void dropFood() {
		Iterator<Food> fit = screenItems.foodStuffs.iterator();
		while ( fit.hasNext() ) {
			Food f = fit.next();
			f.energy -= 5;
			if ( f.energy <= 0 ) {
				fit.remove();
			}
		}
		for ( int i = 0; i < 50; ++i) {
			dropOneFood();
		}
/*		
		int fSize = screenItems.foodStuffs.size();
		for ( int i = 100 - fSize; i > 0; --i) {
			screenItems.foodStuffs.add(new Food(rand));
		}
*/		
	}
	private Critter reproduceAndMutateCritter(Critter c) {
		// genetic reproduction callback code
		int re1 = (rand.nextInt( 10 ) == 0 ?  Util.within(HUNGRY_LEVEL, FULL_LEVEL, c.repEnergy+Util.randPlusMinus(10)) : c.repEnergy);  
		int op1 = (rand.nextInt( 10 ) == 0 ?  Util.within(MIN_REP_LEVEL, MAX_REP_LEVEL, c.offspringPercent+Util.randPlusMinus(2)) : c.offspringPercent);  
		int re2 = (rand.nextInt( 10 ) == 0 ?  rand.nextInt(FULL_LEVEL - HUNGRY_LEVEL)+HUNGRY_LEVEL : c.repEnergy);
		int op2 = (rand.nextInt( 10 ) == 0 ?  rand.nextInt(MAX_REP_LEVEL - MIN_REP_LEVEL)+MIN_REP_LEVEL : c.offspringPercent);
		int b1 = (rand.nextInt( 10 ) == 0 ?  c.biteEnergy + Util.randPlusMinus(100) : c.biteEnergy);
		int b2 = (rand.nextInt( 10 ) == 0 ?  rand.nextInt(MAX_ENERGY) : c.biteEnergy); 
		int hc1 = (rand.nextInt( 10 ) == 0 ?  Util.within(1, 100, Util.randPlusMinus(1)+c.huntChance) : c.huntChance);
		int hc2 = (rand.nextInt( 10 ) == 0 ?  rand.nextInt(10)+1 : c.huntChance);
		Critter cn = new Critter(
				Util.within(0, 1000, Util.randPlusMinus(10)+c.r.x),  
				Util.within(0, 1000, Util.randPlusMinus(10)+c.r.y),  
				c.getMovement().cloneAndMutate(), 
				(rand.nextBoolean() ? re1 : re2),  
				(rand.nextBoolean() ? op1 : op2), 
				(rand.nextBoolean() ? b1 : b2),
				(rand.nextBoolean() ? hc1 : hc2)
				);
		// 
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

