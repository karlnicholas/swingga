package swingga;

import java.awt.Rectangle;

public class Critter {
	// current location
	public int x, y;
	// movement algorithm
	private CritterMovement movement;
	public Rectangle rectangle; 

	public Critter(int x, int y, CritterMovement movement) {
		this.x = x;
		this.y = y;
		this.movement = movement;
		rectangle = new Rectangle(x, y, SimulationThread.cSize, SimulationThread.cSize);
	}
	
	@Override
	public String toString() {
		return super.toString() + "[" + x +"," + y + "]";
	}

	public void move(Offset offset) {
		x = x + offset.mx;
		y = y + offset.my;
		checkBounds();
		rectangle.setLocation(x, y);
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

	public boolean intersects(Rectangle r2) {
		return rectangle.intersects(r2);
	}

}
