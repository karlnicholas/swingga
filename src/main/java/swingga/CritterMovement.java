package swingga;

import swingga.SimulationThread.MOVE_FUNCTION;

/**
 * Interface for specific implementations of movements
 *
 */
public interface CritterMovement {

	public CritterMovement cloneAndMutate();
	public Offset getCollision(MOVE_FUNCTION colType);
	public Offset getOffset();

}
