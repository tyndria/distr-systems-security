package elliptic.curve;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import libs.ModularArith;

public class EllipticGroup {
	private int M;
	private int a = -1;
	private int b = -1;
	private Point G = new Point();
	private List<Point> points = new ArrayList<Point>();

	public EllipticGroup(int M) {
		this.M = M;
		
		this.defineEquationParams();
	}
	
	public EllipticGroup(int M, int a, int b) {
		this.M = M;
		this.a = a;
		this.b = b;
	}
	
	public EllipticGroup(int M, int a, int b, Point G) {
		this.M = M;
		this.a = a;
		this.b = b;
		this.G = G;
	}
	
	private void defineEquationParams() {
		// The simplest realization
		// Choose natural numbers for ease
		int a = 0, b = 0;
		boolean isDefined = false;
		while (a < M) {
			while (b < M) {
				if (this.checkEquationParams(a, b)) {
					this.a = a;
					this.b = b;
					isDefined = true;
					break;
				}
				b ++;
			}
			if (isDefined) {
				break;
			}
			a ++;
		}
	}
	
	public void generatedGroupElements() {
		int x = 1;
		while (x <= M) {
			int c = (int) Math.pow(x, 3) + this.a * x + this.b;
			int sqrtModRoot = (int)Math.sqrt(c);
			if (mod(sqrtModRoot, M) > 0 ) {
				points.add(new Point(x, mod(sqrtModRoot, M)));
			}
			/*BigInteger bigSqrtModRoot = ModularArith.sqrtP(BigInteger.valueOf(c), BigInteger.valueOf((long)M));
			if (bigSqrtModRoot != null) {
				Integer sqrtModRoot = bigSqrtModRoot.intValue();
				points.add(new Point(x, sqrtModRoot.intValue()));
			}*/
			x ++;
		}
	}

	public List<Point> getPoints() {
		return points;
	}
	
	private boolean checkEquationParams(double a, double b) {
		int c = (int) (4 * Math.pow(a, 3) + 27 * Math.pow(b, 2));
		return mod(c, M) > 0; 
	}
	
	// Do not work yet :(
	public Point add(Point a, Point b) {
		int m = -1;
		int xRes = 0;
		int yRes = 0;

		if (a.x == 0 && a.y == 0) {
			xRes = (int) b.x;
			yRes = (int) b.y;
		} else if (b.x == 0 && b.y == 0) {
			xRes = (int) a.x;
			yRes = (int) a.y;
		} else if (a.x == b.x && a.y == mod((int)-b.y, M)) {
			xRes = 0;
			yRes = 0;
		} else if (a.equals(b)) {
			if (a.y == 0) {
				xRes = 0;
				yRes = 0;
			} else {
				m = (int) (((3 * Math.pow(a.x, 2) + this.a)) / (2 * a.y));
				m = mod(m, M);
				
				xRes = (int) (Math.pow(m, 2) - a.x - b.x);
				yRes = (int) (m * (a.x - xRes) - a.y);
				
				xRes = mod(xRes, M);
				yRes = mod(yRes, M);
			}
		} else {
			m = (int) ((b.y - a.y) / (b.x - a.x));
			m = mod(m, M);
			
			xRes = (int) (Math.pow(m, 2) - a.x - b.x);
			yRes = (int) (m * (a.x - xRes) - a.y);
			
			xRes = mod(xRes, M);
			yRes = mod(yRes, M);
		}
		
		return new Point(xRes, yRes);
	}
	
	public Point smartMult(Point a, int n) {
		String bits = new StringBuilder(Integer.toBinaryString(n)).toString();
		Point current = a;
		
		for (int i = 2; i < bits.length(); i ++) {
			current = this.add(current, current);
			if (bits.charAt(i) == '1') {
				if (a.x == current.x && a.y == current.y) {
					current = this.add(current, current);
				} else {
					current = this.add(a, current);
				}
			}
		}
		
		return current;
	}
	
	private int mod(int x, int y) {
		return Math.floorMod(x, y);
	}
}

class Point {
	double x;
	double y;
	
	// TODO: properly define 'zero' element
	public Point() {
		this.x = 0;
		this.y = 0;
	}
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
	public boolean equals(Point a) {
		return a.x == this.x && a.y == this.y;
	}
}
