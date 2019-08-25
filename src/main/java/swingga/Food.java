package swingga;

import java.awt.Rectangle;
import java.util.Random;

public class Food {
	private static final double stdevs = 4.0;
	int energy;
	public Rectangle r;
	public Food(Random rand) {
		double ng = rand.nextGaussian() + stdevs;
		if ( ng > (stdevs*2.0) ) ng = (stdevs*2.0);
		if ( ng < 0.0 ) ng = 0.0;
		int x = (int)(ng * (1000/(stdevs*2.0))); 
		ng = rand.nextGaussian() + stdevs;	
		if ( ng > (stdevs*2.0) ) ng = (stdevs*2.0);
		if ( ng < 0.0 ) ng = 0.0;
		int y = (int)(ng * (1000/(stdevs*2.0))); 
//		System.out.println("" + x + ":" + y);
		r = new Rectangle(x, y, SimulationThread.cSize,  SimulationThread.cSize); 
		this.energy = 1000;
	}
}
