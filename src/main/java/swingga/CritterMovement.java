package swingga;

/**
 * Interface for specific implementations of movements
 *
 */
public interface CritterMovement {

	public Offset getMovement(Critter c);
	public CritterMovement cloneAndMutate();

}
