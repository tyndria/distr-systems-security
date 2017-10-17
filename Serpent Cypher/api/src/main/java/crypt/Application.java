package main.java.crypt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		String text = "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. \" +\r\n" + 
				"		\"The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, \" +\r\n" + 
				"		\"as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors \" +\r\n" + 
				"		\"now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. \" +\r\n" + 
				"		\"Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).";
		int key = 9;
		byte[] binaryText = text.getBytes();
		int[] texts = new int[4];
		
		
		for (int i = 0; i < 4; i ++) {
			texts[i] = (int)binaryText[i];
		}
		
		System.out.println(texts[3]);
		System.out.println(cryptRound(texts, key)[3]);
		System.out.println(decryptRound(texts, key)[3]);
		SpringApplication.run(Application.class, args);
	}
	
	static int[] cryptRound(int[] b, int key) {
		for (int i = 0; i < 4; i ++) {
			b[i] = b[i] ^ key;
		}
		b[0] = Integer.rotateLeft(b[0], 13);
		b[2] = Integer.rotateLeft(b[2], 3);
		b[1] = b[0] ^ b[1] ^ b[2];
		b[3] = b[3] ^ b[2] ^ (b[0] << 3);
		b[1] = Integer.rotateLeft(b[1], 1);
		b[3] = Integer.rotateLeft(b[3], 7);
		b[0] = b[0] ^ b[1] ^ b[3];
		b[2] = b[2] ^ b[3] ^ (b[1] << 7);
		b[0] = Integer.rotateLeft(b[0], 5);
		b[2] = Integer.rotateLeft(b[2], 22);
		return b;
	}
	
	static int[] decryptRound(int[] b, int key) {
		b[2] = Integer.rotateRight(b[2], 22);
		b[0] = Integer.rotateRight(b[0], 5);
		b[2] = b[2] ^ b[3] ^ (b[1] << 7);
		b[0] = b[0] ^ b[1] ^ b[3];
		b[3] = Integer.rotateRight(b[3], 7);
		b[1] = Integer.rotateRight(b[1], 1);
		b[3] = b[3] ^ b[2] ^ (b[0] << 3);
		b[1] = b[0] ^ b[1] ^ b[2];
		b[2] = Integer.rotateRight(b[2], 3);
		b[0] = Integer.rotateRight(b[0], 13);
		for (int i = 0; i < 4; i ++) {
			b[i] = b[i] ^ key;
		}
		return b;
	}
	
	

}
