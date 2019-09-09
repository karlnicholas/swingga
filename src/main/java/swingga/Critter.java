package swingga;

import java.awt.Rectangle;

public class Critter implements Comparable<Critter> {
	// movement algorithm
	private CritterMovement movement;
	public Rectangle r; 
	public int energy = 1000;
	public boolean living;
	public int repEnergy;
	public int offspringPercent;
	public boolean hungry;
	public int biteEnergy;
	public int huntChance;

	public Critter(int x, int y, CritterMovement movement, int repEnergy, int offspringPercent, int biteEnergy, int huntChance) {
		this.movement = movement;
		r = new Rectangle(x, y, SimulationThread.cSize, SimulationThread.cSize);
		living = true;
		this.repEnergy = repEnergy;
		this.offspringPercent = offspringPercent;
		this.biteEnergy = biteEnergy;
		this.huntChance = huntChance;
		hungry = false;
	}
	
	@Override
	public String toString() {
		return super.toString() + "[" + r.x +"," + r.y + "]";
	}

	public void move(Offset offset) {
		r.x = r.x + offset.mx;
		r.y = r.y + offset.my;
		checkBounds();
	}

	public CritterMovement getMovement() {
		return movement;
	}
	public void checkBounds() {
		if ( r.x < 0 ) r.x = 1000 + r.x;
		if ( r.x > 1000 ) r.x = r.x - 1000;
		if ( r.y < 0 ) r.y = 1000 + r.y;
		if ( r.y > 1000 ) r.y = r.y - 1000;		
	}
	public int getEnergy() {
		return energy;
	}
	public int getRepEnergy() {
		return repEnergy;
	}
	public boolean getLiving() {
		return living;
	}
	public int getBiteEnergy() {
		return biteEnergy;
	}
	public int getHuntChance() {
		return huntChance;
	}

	@Override
	public int compareTo(Critter o) {
		return energy - o.energy;
	}
}
