package swingga;

/**
 * Interface for specific implementations of movements
 *
 */
public interface CritterMovement {

	public void moveCritter(Critter c);
	public int getEnergy();
	public void setEnergy(int energy);

	public CritterMovement cloneAndMutate();

}
