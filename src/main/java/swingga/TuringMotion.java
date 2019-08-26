package swingga;

import java.util.Random;

public class TuringMotion {
	private int xAdjust, yAdjust;
	private int gotoLocation;
	public TuringMotion(Random rand, int mCount) {
		setxAdjust(SimulationThread.MAX_MOVEMENT - rand.nextInt(SimulationThread.MAX_MOVEMENT*2+1));
		setyAdjust(SimulationThread.MAX_MOVEMENT - rand.nextInt(SimulationThread.MAX_MOVEMENT*2+1));
		gotoLocation = rand.nextInt(mCount);
	}
	public TuringMotion(TuringMotion turingMotion) {
		this.setxAdjust(turingMotion.xAdjust);
		this.setyAdjust(turingMotion.yAdjust);
		this.gotoLocation = turingMotion.gotoLocation;
	}
	public int getGotoLocation() {
		return gotoLocation;
	}
	public void setGotoLocation(int gotoLocation) {
		this.gotoLocation = gotoLocation;
	}
	public int getxAdjust() {
		return xAdjust;
	}
	public void setxAdjust(int xAdjust) {
		this.xAdjust = xAdjust;
	}
	public int getyAdjust() {
		return yAdjust;
	}
	public void setyAdjust(int yAdjust) {
		this.yAdjust = yAdjust;
	}
}
