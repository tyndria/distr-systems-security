package elliptic.curve;

import java.util.Map.Entry;

public class Main {

	public static void main(String[] args) {
		int M = 211;
		int a = 0;
		int b = -4;
		int q = 241;
		Point G = new Point(2, 2);
		EllipticGroup eGroup = new EllipticGroup(M, a, b, G);
		
		KeyGenerator A = new KeyGenerator(G, M, eGroup, q);
		KeyGenerator B = new KeyGenerator(G, M, eGroup, q);
		
		A.generateKeys();
		Point PA = A.getPublicKey();
		//System.out.println(PA);
		
		B.generateKeys();
		Point PB = A.getPublicKey();
		
		A.generateCommonPrivateKey(PB);
		B.generateCommonPrivateKey(PA);
		
		// Not working yet :(
		String helloWorld = "hello, world!";
		DSA dsa = new DSA(eGroup, M, G, q);
		Entry<Integer, Integer> signature = dsa.getSignature(helloWorld);
		System.out.println(dsa.getSignature(helloWorld) + " " + dsa.certifySignature(helloWorld, signature));
	}

}
