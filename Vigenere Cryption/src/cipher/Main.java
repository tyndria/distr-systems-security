package cipher;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cipher.cryptoanalysis.FrequencyAnalyzer;
import cipher.cryptoanalysis.Kasiski;
import cipher.cryptoanalysis.ObjectFrequency;

public class Main {

	public static void main(String[] args) {
		Scanner scanner;
		StringBuilder stringBuilder = new StringBuilder();
		List<ObjectFrequency> letterFrequencies = new ArrayList<ObjectFrequency>();
		try {
			scanner = new Scanner(new File("input.txt"));
			while(scanner.hasNext()) {
				stringBuilder.append(scanner.next() + " ");
			}
			
			scanner = new Scanner(new File("frequencies.txt"));
			while(scanner.hasNext()) {
				String letter = scanner.next();
				double frequency = scanner.nextDouble();
				letterFrequencies.add(new ObjectFrequency(letter, frequency));
				if (scanner.hasNextLine()) {
					scanner.nextLine();
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
		
		String text = stringBuilder.toString().toLowerCase();
		
		VigenereCipher cipher = new VigenereCipher();
		String keyWord = "mouse";
		
		String encryptedText = cipher.encrypt(text, keyWord);
		String decryptedText = cipher.decrypt(encryptedText, keyWord);
		System.out.println(text);
		System.out.println(encryptedText);
	    
		int keywordLength = Kasiski.getKeywordLength(encryptedText);
        System.out.println(keywordLength);
        
        FrequencyAnalyzer analyzer = new FrequencyAnalyzer(keywordLength, letterFrequencies);
        String decryptedText2 = analyzer.decrypt(encryptedText);
        System.out.println(decryptedText2);
	}

}
