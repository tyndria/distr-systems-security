package ecdsa;

public class Main {

	public static void main(String[] args) {
		int M = 71;
		EllipticGroup eGroup = new EllipticGroup(M);
		eGroup.generatedGroupElements();
		System.out.println(eGroup.getPoints());
		Point p1 = new Point(0, 1);
		Point p2 = new Point(1, 12);
		Point p3 = new Point(7, 29);
		
		Point G = new Point(2, 3);
		
		KeyGenerator A = new KeyGenerator(G, M, eGroup);
		KeyGenerator B = new KeyGenerator(G, M, eGroup);
		
		A.generatePrivateKey();
		A.generatePublicKey();
		Point PA = A.getPublicKey();
		
		B.generatePrivateKey();
		B.generatePublicKey();
		Point PB = A.getPublicKey();
		
		A.generateCommonPrivateKey(PB);
		B.generateCommonPrivateKey(PA);
	}

}
