package swingga;

import swingga.SimulationThread.MOVE_TYPE;

/**
 * Interface for specific implementations of movements
 *
 */
public interface CritterMovement {

	public CritterMovement cloneAndMutate();
	public Offset getCollision(MOVE_TYPE colType);
	public Offset getOffset();

}
