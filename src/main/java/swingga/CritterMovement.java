package swingga;

import swingga.SimulationThread.COLLISION_TYPE;

/**
 * Interface for specific implementations of movements
 *
 */
public interface CritterMovement {

	public CritterMovement cloneAndMutate();
	public Offset getCollision(COLLISION_TYPE colType);
	public Offset getOffset();

}
