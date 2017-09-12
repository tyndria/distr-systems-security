package cipher;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Scanner scanner;
		StringBuilder stringBuilder = new StringBuilder();
		try {
			scanner = new Scanner(new File("input.txt"));
			while(scanner.hasNext()) {
				stringBuilder.append(scanner.next() + " ");
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
		
		String text = stringBuilder.toString();
		
		VigenereCipher cipher = new VigenereCipher();
		String keyWord = "mouse";
		
		String encryptedText = cipher.encrypt(text, keyWord);
		String decryptedText = cipher.decrypt(encryptedText, keyWord);
	    
		int keywordLenght = Kasiski.getKeywordLength(encryptedText);
        System.out.println(keywordLenght);
	}

}
