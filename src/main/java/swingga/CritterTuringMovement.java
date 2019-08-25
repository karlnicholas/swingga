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
	private Offset offset;
	private int turingLocation;

	public CritterTuringMovement() {
		offset = new Offset();
		int mCount = rand.nextInt(motionMax-1) + 1;
		for(int i=mCount; i>0; --i) {
			motions.add( new TuringMotion(rand, mCount));
		}
	}

	public CritterTuringMovement(CritterTuringMovement critterTuringMovement) {
		offset = new Offset();
		for(int i=0; i < critterTuringMovement.motions.size(); ++i) {
			motions.add( new TuringMotion(critterTuringMovement.motions.get(i)));
		}
	}

	@Override
	public Offset getMovement(Critter c) {
		int xm = motions.get(turingLocation).getxAdjust();
		offset.mx = Math.max(-10, Math.min(10, xm ));  
		int ym = motions.get(turingLocation).getyAdjust();
		offset.my = Math.max(-10, Math.min(10, ym ));  
		
//		energy -= Math.min((Math.abs(mx) + Math.abs(my)), 4);
/*		
		c.x = Math.max(0, Math.min(1000, c.x + mx));
		c.y = Math.max(0, Math.min(1000, c.y + my));
*/		
		turingLocation = motions.get(turingLocation).getGotoLocation();
		return offset; 
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

}
