package swingga;


public class Critter {
	// current location
	public int x, y;
	// memory of last location
	public int mx, my;
	// movement algorithm
	private CritterMovement movement;
	public Critter(int x, int y, CritterMovement movement) {
		this.x = x;
		this.y = y;
		this.mx = x;
		this.my = y;
		this.movement = movement;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[" + x +"," + y + "," + mx +"," + my + "]";
	}

	public void move() {
		movement.moveCritter(this);
	}

	public CritterMovement getMovement() {
		return movement;
	}

}
