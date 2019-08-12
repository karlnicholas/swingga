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
	private static final int motionMax = 10;
	// memory of last motion amount
	private int mx, my;
	private int turingLocation;

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
		mx += motions.get(turingLocation).getxAdjust();
		my += motions.get(turingLocation).getyAdjust();
		c.x = Math.min(990, Math.max(10, c.x + mx));
		c.y = Math.min(990, Math.max(10, c.y + my));
		turingLocation = motions.get(turingLocation).getGotoLocation();
	}
	
	@Override
	public CritterMovement cloneAndMutate() {
		CritterTuringMovement movement = new CritterTuringMovement(this);
		TuringMotion motion = movement.motions.get( rand.nextInt(motions.size()) );
		switch(rand.nextInt(3)) {
		case 0:
			motion.setxAdjust(motion.getxAdjust() + (2 - rand.nextInt(5)));
			break;
		case 1:
			motion.setyAdjust(motion.getyAdjust() + (2 - rand.nextInt(5)));
			break;
		case 2:
			motion.setGotoLocation(motion.getGotoLocation() + (2 - rand.nextInt(5)));
			motion.setGotoLocation( Math.max(0, Math.min(motions.size()-1, motion.getGotoLocation()) ) );
			break;
		}
		return movement;
	}
}
