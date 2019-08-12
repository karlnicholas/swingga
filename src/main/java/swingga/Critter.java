package swingga;


public class Critter {
	// current location
	public int x, y;
	// movement algorithm
	private CritterMovement movement;
	public Critter(int x, int y, CritterMovement movement) {
		this.x = x;
		this.y = y;
		this.movement = movement;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[" + x +"," + y + "]";
	}

	public void move() {
		movement.moveCritter(this);
	}

	public CritterMovement getMovement() {
		return movement;
	}

}
