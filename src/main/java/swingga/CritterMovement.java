package swingga;

import swingga.SimulationThread.BEHAVIOR_MODES;

/**
 * Interface for specific implementations of movements
 *
 */
public interface CritterMovement {

	public CritterMovement cloneAndMutate();
	public Offset getCollision(BEHAVIOR_MODES colType);
	public Offset getOffset();

}
