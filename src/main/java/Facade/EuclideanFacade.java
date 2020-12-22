package Facade;

import java.awt.Point;

public class EuclideanFacade {
	public static double distance(Point one, Point two) {
		if (one == null) {
			throw new IllegalArgumentException("point one cannot be null");
		}
		
		if (two == null) {
			throw new IllegalArgumentException("point two cannot be null");
		}
		
		return SquareRoot.of(Addition.of(Power.of(Subtraction.of(one.x, two.x), 2)
				, Power.of(Subtraction.of(one.y, two.y), 2)));
	}
	
}

class SquareRoot {
	public static double of(double one) {
		return Math.sqrt(one);
	}
}

class Power {
	public static double of(double one, double two) {
		return Math.pow(one, two);
	}
}

class Addition {
	public static double of(double one, double two) {
		return one + two;
	}
}

class Subtraction {
	public static double of(double one, double two) {
		return one - two;
	}
}