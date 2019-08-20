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
	public void checkBounds() {
		if ( x < 0 ) x = 1000 + x;
		if ( x > 1000 ) x = x - 1000;
		if ( y < 0 ) y = 1000 + y;
		if ( y > 1000 ) y = y - 1000;
		
	}

}
