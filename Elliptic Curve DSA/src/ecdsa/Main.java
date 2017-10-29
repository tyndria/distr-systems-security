package ecdsa;

public class Main {

	public static void main(String[] args) {
		int M = 71;
		EllipticGroup eGroup = new EllipticGroup(M);
		eGroup.generatedGroupElements();
		System.out.println(eGroup.getPoints());
		Point p1 = new Point(6, 2);
		Point p2 = new Point(2, 3);
		System.out.println(eGroup.add(p1, p2));
	}

}
