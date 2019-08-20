package swingga;

import java.util.Random;

/**
 * Massive Silliness. 
 *
 */

public class CritterRandomMovement implements CritterMovement {
	private static final Random rand = new Random();
	public int xRandMax, yRandMax, xRandOff, yRandOff, xRandLimit, yRandLimit;
	// memory of last motion amount
	private int mx, my;

	CritterRandomMovement( int xRandMax, int yRandMax, int xRandOff, int yRandOff, int xRandLimit, int yRandLimit) {
		this.xRandMax = xRandMax;
		this.yRandMax = yRandMax;
		this.xRandOff = xRandOff;
		this.yRandOff = yRandOff;
		this.xRandLimit = xRandLimit;
		this.yRandLimit = yRandLimit;
	}

	@Override
	public void moveCritter(Critter c) {
		mx = Math.min(xRandLimit, Math.max(-xRandLimit, mx + (xRandOff - rand.nextInt(xRandMax))));
		my = Math.min(yRandLimit, Math.max(-yRandLimit, my + (yRandOff - rand.nextInt(yRandMax))));
		c.x = Math.min(990, Math.max(10, c.x + mx));
		c.y = Math.min(990, Math.max(10, c.y + my));
	}
	@Override
	public CritterMovement cloneAndMutate() {
		CritterRandomMovement movement = new CritterRandomMovement(xRandMax, yRandMax, xRandOff, yRandOff, xRandLimit, yRandLimit);
		switch(rand.nextInt(6)) {
		case 0:
			movement.xRandMax = Math.max(1, (2 - rand.nextInt(5) + movement.xRandMax));
			break;
		case 1:
			movement.yRandMax = Math.max(1, (2 - rand.nextInt(5) + movement.yRandMax));
			break;
		case 2:
			movement.xRandOff = Math.max(1, (2 - rand.nextInt(5) + movement.xRandOff));
			break;
		case 3:
			movement.yRandOff = Math.max(1, (2 - rand.nextInt(5) + movement.yRandOff));
			break;
		case 4:
			movement.xRandLimit = Math.max(1, (2 - rand.nextInt(5) + movement.xRandLimit));
			break;
		case 5:
			movement.yRandLimit = Math.max(1, (2 - rand.nextInt(5) + movement.yRandLimit));
			break;
		}
		return movement;
	}

	@Override
	public int getEnergy() {
		return 0;
	}

	@Override
	public void setEnergy(int energy) {
		// TODO Auto-generated method stub
		
	}
}
