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
	private int[] behaviorModeLocs;
	// memory of last motion amount
	private Offset offset;
	private int turingLocation;

	public CritterTuringMovement() {
		offset = new Offset();
		int mCount = rand.nextInt(motionMax-1) + 1;
		for(int i=0; i<mCount; ++i) {
			motions.add( new TuringMotion(rand, mCount));
		}
		behaviorModeLocs = new int[SimulationThread.BEHAVIOR_MODES.values().length];
		for ( int i = 0; i < SimulationThread.BEHAVIOR_MODES.values().length; ++i ) {
			behaviorModeLocs[i] = rand.nextInt(mCount);
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
		behaviorModeLocs = Arrays.copyOf(critterTuringMovement.behaviorModeLocs, critterTuringMovement.behaviorModeLocs.length);
	}

	@Override
	public Offset getOffset() {
		int xm = motions.get(turingLocation).getxAdjust();
		offset.mx = xm;  
		int ym = motions.get(turingLocation).getyAdjust();
		offset.my = ym;
		
		turingLocation = motions.get(turingLocation).getGotoLocation();
		return offset; 
	}
	
	@Override
	public Offset getCollision(SimulationThread.BEHAVIOR_MODES colType) {
		turingLocation = behaviorModeLocs[colType.ordinal()]; 
		return getOffset(); 
	}
	
	@Override
	public CritterMovement cloneAndMutate() {
		CritterTuringMovement movement = new CritterTuringMovement(this);
		int mLoc = rand.nextInt(motions.size());
		TuringMotion motion = movement.motions.get( mLoc );
		switch(rand.nextInt(20)) {
		case 0:
			motion.setxAdjust(Util.within(-SimulationThread.MAX_MOVEMENT, SimulationThread.MAX_MOVEMENT, motion.getxAdjust() + Util.randPlusMinus(1)));
			break;
		case 1:
			motion.setyAdjust(Util.within(-SimulationThread.MAX_MOVEMENT, SimulationThread.MAX_MOVEMENT, motion.getyAdjust() + Util.randPlusMinus(1)));
			break;
		case 2:
			motion.setGotoLocation(Util.within(0, movement.motions.size()-1, motion.getxAdjust() + Util.randPlusMinus(1)));
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
				for ( int i = 0; i < SimulationThread.BEHAVIOR_MODES.values().length; ++i ) {
					if ( movement.behaviorModeLocs[i] >= mCount )
						movement.behaviorModeLocs[i] = rand.nextInt(mCount);
				}
			} else {
				for ( int i=movement.motions.size(); i < mCount; ++i) {
					movement.motions.add( new TuringMotion(rand, mCount));
				}
			}
			break;
		case 4:
			motion.setxAdjust(Util.randPlusMinus(SimulationThread.MAX_MOVEMENT));
			break;
		case 5:
			motion.setyAdjust(Util.randPlusMinus(SimulationThread.MAX_MOVEMENT));
			break;
		case 6:
			motion.setGotoLocation( rand.nextInt(movement.motions.size()) );
			break;
		default:
			break;
		}
		return movement;
	}

}
