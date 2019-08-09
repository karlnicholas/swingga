package swingga;

import java.util.Random;

public class Critter {
	private Random rand = new Random();
	public int xRandMax, yRandMax, xRandOff, yRandOff, xRandLimit, yRandLimit;
	public int x, y;
	public int mx, my;
	public Critter(int x, int y, int xRandMax, int yRandMax, int xRandOff, int yRandOff, int xRandLimit, int yRandLimit) {
		this.x = x;
		this.y = y;
		this.xRandMax = xRandMax;
		this.yRandMax = yRandMax;
		this.xRandOff = xRandOff;
		this.yRandOff = yRandOff;
		this.xRandLimit = xRandLimit;
		this.yRandLimit = yRandLimit;
	}
	public void move() {
		mx = Math.min(xRandLimit, Math.max(-xRandLimit, mx + (xRandOff - rand.nextInt(xRandMax))));
		my = Math.min(yRandLimit, Math.max(-yRandLimit, my + (yRandOff - rand.nextInt(yRandMax))));
		x = Math.min(990, Math.max(10, x + mx));
		y = Math.min(990, Math.max(10, y + my));
	}
	@Override
	public String toString() {
		return super.toString() + "[" + x +"," + y + "," + mx +"," + my + "]";
	}
}
