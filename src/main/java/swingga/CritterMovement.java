package swingga;

/**
 * Interface for specific implementations of movements
 *
 */
public interface CritterMovement {

	public Offset getMovement(Critter c);
	public int getEnergy();
	public void setEnergy(int energy);
	public boolean checkEnergy();

	public CritterMovement cloneAndMutate();

}
