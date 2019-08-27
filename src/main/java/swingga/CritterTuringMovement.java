package swingga;

import java.util.ArrayList;
import java.util.Arrays;
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
	private int[] collisionLocs;
	// memory of last motion amount
	private Offset offset;
	private int turingLocation;

	public CritterTuringMovement() {
		offset = new Offset();
		int mCount = rand.nextInt(motionMax-1) + 1;
		for(int i=0; i<mCount; ++i) {
			motions.add( new TuringMotion(rand, mCount));
		}
		collisionLocs = new int[SimulationThread.COLLISION_TYPE.values().length];
		for ( int i = 0; i < SimulationThread.COLLISION_TYPE.values().length; ++i ) {
			collisionLocs[i] = rand.nextInt(mCount);
		}
	}

	/**
	 * Make a deep copy of existing CritterMovement
	 * @param critterTuringMovement
	 */
	public CritterTuringMovement(CritterTuringMovement critterTuringMovement) {
		offset = new Offset();
		for(int i=0; i < critterTuringMovement.motions.size(); ++i) {
			motions.add( new TuringMotion(critterTuringMovement.motions.get(i)));
		}
		collisionLocs = Arrays.copyOf(critterTuringMovement.collisionLocs, critterTuringMovement.collisionLocs.length);
	}

	@Override
	public Offset getOffset() {
		int xm = motions.get(turingLocation).getxAdjust();
		offset.mx = Math.max(-SimulationThread.MAX_MOVEMENT, Math.min(SimulationThread.MAX_MOVEMENT, xm ));  
		int ym = motions.get(turingLocation).getyAdjust();
		offset.my = Math.max(-SimulationThread.MAX_MOVEMENT, Math.min(SimulationThread.MAX_MOVEMENT, ym ));
		
		turingLocation = motions.get(turingLocation).getGotoLocation();
		return offset; 
	}
	
	@Override
	public Offset getCollision(SimulationThread.COLLISION_TYPE colType) {
		turingLocation = collisionLocs[colType.ordinal()]; 
		return getOffset(); 
	}
	
	@Override
	public CritterMovement cloneAndMutate() {
		CritterTuringMovement movement = new CritterTuringMovement(this);
		int mLoc = rand.nextInt(motions.size());
		TuringMotion motion = movement.motions.get( mLoc );
		switch(rand.nextInt(20)) {
		case 0:
			motion.setxAdjust(Math.max(-SimulationThread.MAX_MOVEMENT, Math.min(SimulationThread.MAX_MOVEMENT, motion.getxAdjust() + (1-rand.nextInt(3)))));
			break;
		case 1:
			motion.setyAdjust(Math.max(-SimulationThread.MAX_MOVEMENT, Math.min(SimulationThread.MAX_MOVEMENT, motion.getyAdjust() + (1-rand.nextInt(3)))));
			break;
		case 2:
			motion.setGotoLocation( Math.max(0, Math.min(movement.motions.size(), motion.getGotoLocation() + (1-rand.nextInt(3)))) );
			break;
		case 3:
			int mCount = rand.nextInt(motionMax-1) + 1;
			if ( movement.motions.size() > mCount ) {
				// make smaller
				for ( int i=movement.motions.size(); i > mCount; --i) {
					movement.motions.remove(i-1);
				}
				for ( int i=0; i < movement.motions.size(); ++i) {
					if ( movement.motions.get(i).getGotoLocation() >= mCount )
						movement.motions.get(i).setGotoLocation(rand.nextInt(mCount));
				}				
				for ( int i = 0; i < SimulationThread.COLLISION_TYPE.values().length; ++i ) {
					if ( movement.collisionLocs[i] >= mCount )
						movement.collisionLocs[i] = rand.nextInt(mCount);
				}
			} else {
				for ( int i=movement.motions.size(); i < mCount; ++i) {
					movement.motions.add( new TuringMotion(rand, mCount));
				}
			}
			break;
		case 4:
			motion.setxAdjust(SimulationThread.MAX_MOVEMENT - rand.nextInt(SimulationThread.MAX_MOVEMENT*2+1));
			break;
		case 5:
			motion.setyAdjust(SimulationThread.MAX_MOVEMENT - rand.nextInt(SimulationThread.MAX_MOVEMENT*2+1));
			break;
		case 6:
			motion.setGotoLocation( rand.nextInt(movement.motions.size()-1) );
			break;
		default:
			break;
		}
		return movement;
	}

}
