package swingga;

import java.util.Random;

public class Util {
	private static final Random rand = new Random();
	
	public static int within(int min, int max, int value ) {
		return Math.max(Math.min(value, max), min);
	}
	public static int randPlusMinus(int value) {
		return value - rand.nextInt(value*2+1);
	}
	public static boolean crossedBelow(int oldE, int newE, int level) {
		return newE < level && oldE > level;
	}
	public static boolean crossedAbove(int oldE, int newE, int level) {
		return newE > level && oldE < level;
	}
}
