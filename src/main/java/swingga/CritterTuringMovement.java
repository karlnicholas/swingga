package swingga;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Massive Silliness. 
 *
 */

public class CritterTuringMovement implements CritterMovement {
	private static final Random rand = new Random();
	private List<TuringMotion> motions = new ArrayList<>();
	private static final int motionMax = 500;
	// memory of last motion amount
	private int mx, my;
	private int turingLocation;
	private int energy = 1000; 

	public CritterTuringMovement() {
		int mCount = rand.nextInt(motionMax-1) + 1;
		for(int i=mCount; i>0; --i) {
			motions.add( new TuringMotion(rand, mCount));
		}
	}

	public CritterTuringMovement(CritterTuringMovement critterTuringMovement) {
		for(int i=0; i < critterTuringMovement.motions.size(); ++i) {
			motions.add( new TuringMotion(critterTuringMovement.motions.get(i)));
		}
	}

	@Override
	public void moveCritter(Critter c) {
		int xm = motions.get(turingLocation).getxAdjust();
		mx = Math.max(-10, Math.min(10, xm ));  
		int ym = motions.get(turingLocation).getyAdjust();
		my = Math.max(-10, Math.min(10, ym ));  
		
		energy -= Math.max( Math.abs(mx)*2 + Math.abs(my)*2, 1);
//		energy -= Math.min((Math.abs(mx) + Math.abs(my)), 4);
		c.x = c.x + mx;
		c.y = c.y + my;
		c.checkBounds();
/*		
		c.x = Math.max(0, Math.min(1000, c.x + mx));
		c.y = Math.max(0, Math.min(1000, c.y + my));
*/		
		turingLocation = motions.get(turingLocation).getGotoLocation();
	}
	
	
	@Override
	public CritterMovement cloneAndMutate() {
		CritterTuringMovement movement = new CritterTuringMovement(this);
		int mLoc = rand.nextInt(motions.size());
		TuringMotion motion = movement.motions.get( mLoc );
		switch(rand.nextInt(4)) {
		case 0:
			motion.setxAdjust(8 - rand.nextInt(17));
			break;
		case 1:
			motion.setyAdjust(8 - rand.nextInt(17));
			break;
		case 2:
			motion.setGotoLocation(motion.getGotoLocation() + (2 - rand.nextInt(5)));
			motion.setGotoLocation( Math.max(0, Math.min(motions.size()-1, motion.getGotoLocation()) ) );
			break;
		case 3:
			break;
		}
		return movement;
	}

	@Override
	public int getEnergy() {
		return energy;
	}

	@Override
	public void setEnergy(int energy) {
		this.energy = energy;
	}
}
