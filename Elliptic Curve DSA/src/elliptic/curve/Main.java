package elliptic.curve;

import java.util.Map.Entry;

public class Main {

	public static void main(String[] args) {
		int M = 71;
		EllipticGroup eGroup = new EllipticGroup(M);
		eGroup.generatedGroupElements();
		System.out.println(eGroup.getPoints());
		Point p1 = new Point(2, 3);
		Point p2 = new Point(0, 1);
		Point p3 = new Point(7, 29);
		System.out.println(eGroup.add(p1, p2));
		
		Point G = new Point(2, 3);
		
		KeyGenerator A = new KeyGenerator(G, M, eGroup);
		KeyGenerator B = new KeyGenerator(G, M, eGroup);
		
		A.generateKeys();
		Point PA = A.getPublicKey();
		
		B.generateKeys();
		Point PB = A.getPublicKey();
		
		A.generateCommonPrivateKey(PB);
		B.generateCommonPrivateKey(PA);
		
		// Not working yet :(
		int q = 5;
		String helloWorld = "hello, world!";
		DSA dsa = new DSA(eGroup, M, G, q);
		Entry<Integer, Integer> signature = dsa.getSignature(helloWorld);
		System.out.println(dsa.getSignature(helloWorld) + " " + dsa.certifySignature(helloWorld, signature));
	}

}
