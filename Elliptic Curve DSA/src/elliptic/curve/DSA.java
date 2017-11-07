package elliptic.curve;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.codec.digest.DigestUtils;

public class DSA {
	private EllipticGroup group;
	KeyGenerator generator;
	private int q;
	private Point G;
	
	public DSA(EllipticGroup g, int M, Point G, int q) {
		this.group = g;
		this.q = q;
		this.G = G;
		this.generator = new KeyGenerator(G, M, g);
		generator.generateKeys();
	}
	
	public Entry<Integer, Integer> getSignature(String m) {
		int privateKey = generator.getPrivateKey();
		
		int h = DigestUtils.getSha1Digest().hashCode();
		int r = 0, k = 0, s = 0;
		while (r == 0 || s == 0) {
			k = ThreadLocalRandom.current().nextInt(1, q - 1);
			Point kG = group.smartMult(G, k);
			r = Math.floorMod((int)kG.getX(), q);
			
			s = (int)Math.pow(k, -1) * (h + r * privateKey);
			s = Math.floorMod(s, q);
		}
		return new AbstractMap.SimpleEntry(r, s);
	}

	public boolean certifySignature(String m, Entry<Integer, Integer> signature) {
		int h = DigestUtils.getSha1Digest().hashCode();
		int r = signature.getKey();
		int s = signature.getValue();
		if (r >= 1 && r <= (q - 1) && s >= 1 && s <= (q - 1)) {
			int w = Math.floorMod((int)Math.pow(s, -1), q);
			int u1 = Math.floorMod(h * w, q);
			int u2 = Math.floorMod(r * w, q);
			Point publicKey = generator.getPublicKey();
			Point p1 = group.smartMult(G, u1);
			Point p2 = group.smartMult(publicKey, u2);
			Point p = group.add(p1, p2);
			int rX = Math.floorMod((int)p.getX(), q);
			return rX == r;
		}
		return false;
	}
}
