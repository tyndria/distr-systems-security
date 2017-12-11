package elliptic.curve;

import java.util.concurrent.ThreadLocalRandom;

public class KeyGenerator {
	private Point G;
	private int M;
	private EllipticGroup group;
	private int q;
	
	private int privateKey;
	Point publicKey;
	private Point commonPrivateKey;
	
	public KeyGenerator(Point G, int M, EllipticGroup g, int q) {
		this.G = G;
		this.M = M;
		this.group = g;
		this.q = q;
	}
	
	public Point getPublicKey() {
		return publicKey;
	}
	
	public int getPrivateKey() {
		return privateKey;
	}
	
	public void generateKeys() {
		generatePrivateKey();
		generatePublicKey();
	}
	
	private void generatePrivateKey() {
		this.privateKey = ThreadLocalRandom.current().nextInt(1, M);
		//this.privateKey = 121; // example from the book
	}
	
	private void generatePublicKey() {
		this.publicKey = group.smartMult(G, privateKey);
	}
	
	public void generateCommonPrivateKey(Point partnerPublicKey) {
		this.commonPrivateKey = group.smartMult(partnerPublicKey, privateKey);
	}
}
