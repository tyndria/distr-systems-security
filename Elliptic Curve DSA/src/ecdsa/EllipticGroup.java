package ecdsa;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import libs.ModularArith;

public class EllipticGroup {
	private int M;
	private int a = -1;
	private int b = -1;
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
		int x = 0;
		while (x < M) {
			int c = (int) Math.pow(x, 3) + this.a * x + this.b;
			BigInteger bigSqrtModRoot = ModularArith.sqrtP(BigInteger.valueOf(c), BigInteger.valueOf((long)M));
			if (bigSqrtModRoot != null) {
				Integer sqrtModRoot = bigSqrtModRoot.intValue();
				points.add(new Point(x, sqrtModRoot.intValue()));
			}
			x ++;
		}
	}

	public List<Point> getPoints() {
		return points;
	}
	
	private boolean checkEquationParams(double a, double b) {
		int c =  (int) (4 * Math.pow(a, 3) + 27 * Math.pow(b, 2));
		return Math.floorMod(c, M) != 0; 
	}
	
	// Do not work yet :(
	public Point add(Point a, Point b) {
		int m = -1;

		if (a.equals(b)) {
			m = (int) ((3 * Math.pow(a.x, 2) + this.a) / (2 * a.y));
		} else {
			m = (int) ((a.y - b.y) / (a.x - b.x));
		}
		
		m = Math.floorMod(m, M);
		
		int xRes = (int) (Math.pow(m, 2) - a.x - b.x);
		int yRes = (int) (a.y + m * (xRes - a.x));
		
		return new Point(Math.floorMod(xRes, M), Math.floorMod(yRes, M));
	}
	
	public Point smartMult(Point a, int n) {
		String bits = new StringBuilder(Integer.toBinaryString(n)).reverse().toString();
		Point result = null;
		Point current = a;
		
		for (int i = 0; i < bits.length(); i ++) {
			if (bits.charAt(i) == '1') {
				result = result == null ? current : this.add(result, current);
			}
			current = this.add(current, current);
		}
		
		return result;
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
	
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
	public boolean equals(Point a) {
		return a.x == this.x && a.y == this.y;
	}
}
