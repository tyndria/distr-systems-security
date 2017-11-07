package elliptic.curve;

import java.util.concurrent.ThreadLocalRandom;

public class KeyGenerator {
	private Point G;
	private int M;
	private EllipticGroup group;
	
	private int privateKey;
	Point publicKey;
	private Point commonPrivateKey;
	
	public KeyGenerator(Point G, int M, EllipticGroup g) {
		this.G = G;
		this.M = M;
		this.group = g;
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
	}
	
	private void generatePublicKey() {
		this.publicKey = group.smartMult(G, privateKey);
	}
	
	public void generateCommonPrivateKey(Point partnerPublicKey) {
		this.commonPrivateKey = group.smartMult(partnerPublicKey, privateKey);
	}
}
