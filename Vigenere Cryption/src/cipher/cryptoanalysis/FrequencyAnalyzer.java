package cipher.cryptoanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import cipher.Constants;

public class FrequencyAnalyzer implements Constants{
	private int keywordLength;
	private List<ObjectFrequency> letterFrequencies;
	
	public FrequencyAnalyzer(int keywordLength, List<ObjectFrequency> letterFrequencies) {
		this.keywordLength = keywordLength;
		this.letterFrequencies = letterFrequencies;
		Collections.sort(letterFrequencies, (a, b) -> a.getFrequency() - b.getFrequency() > 0 ? -1 : 1);
	}
	
	public String decrypt(String text) {
		StringBuilder stringBuilder = new StringBuilder();
		// List of decrypted letters maps for each piece of text encrypted with Caesar cipher
		List<Map<Character, Character>> alphabets = new ArrayList();
		for (int i = 0; i < keywordLength; i ++) {
			stringBuilder = new StringBuilder();
			for (int j = i; j < text.length(); j += (keywordLength)) {
				char letter = text.charAt(j);
				stringBuilder.append(letter);
			}
			
			String textPiece = stringBuilder.toString().toLowerCase();
			// Process each piece of text encrypted with Caesar cipher
			List<ObjectFrequency> textLettersFrequencies = this.countLettersFrequency(textPiece);
			int possibleShift = this.analyzeLetterShift(this.findPossibleInitalLetters(textLettersFrequencies));
			alphabets.add(this.decryptLetters(possibleShift, textPiece));
		}
		
		return this.joinDecryptedTexts(alphabets, text);
	}
	
	private Map<Character, Character> decryptLetters(int shift, String text) {
		Map<Character, Character> decryptedLetters = new HashMap();
		for (int i = 0; i < text.length(); i ++) {
			char letter = text.charAt(i);
			if (ArrayUtils.indexOf(SPECIAL_SYMBOLS, letter) == -1) {
				if (!decryptedLetters.containsKey(letter)) {
					int initialLetterCode = (int)letter - shift;
					initialLetterCode = initialLetterCode < LOWER_ALPHABET_START_CODE ? initialLetterCode += ALPHABET_LENGTH : initialLetterCode;
					decryptedLetters.put(letter, (char)initialLetterCode);
				}
			}
		}
		return decryptedLetters;
	}
	
	private String joinDecryptedTexts(List<Map<Character, Character>> alphabets, String text) {
		StringBuilder decryptedText = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i ++) {
			char letter = text.charAt(i);
			if (ArrayUtils.indexOf(SPECIAL_SYMBOLS, letter) == -1) {
				int alphabetIndex = this.getAlphabetIndex(i);
				boolean isUpperCaseLetter = (int)letter < LOWER_ALPHABET_START_CODE; // check it letter is uppercase or not
				char decryptedLetter = alphabets.get(alphabetIndex).get(Character.toLowerCase(letter));
				decryptedLetter = isUpperCaseLetter ? Character.toUpperCase(decryptedLetter) : decryptedLetter;
				decryptedText.append(decryptedLetter);
			} else {
				decryptedText.append(letter);
			}
		}
		
		return decryptedText.toString();
	}
	
	private int getAlphabetIndex(int letterNumber) {
		return letterNumber < keywordLength ? letterNumber : letterNumber % keywordLength;
	}
	
	private List<ObjectFrequency> countLettersFrequency(String text) {
		Map<Character, ObjectFrequency> frequencies = new HashMap<Character, ObjectFrequency>();
		for (int i = 0; i < text.length(); i ++) {
			char letter = text.charAt(i);
			if (ArrayUtils.indexOf(SPECIAL_SYMBOLS, letter) == -1) {
				ObjectFrequency frequency;
				if (frequencies.containsKey(letter)) {
					frequency = frequencies.get(letter);
					frequency.increaseOccuranceNumber();
				} else {
					frequency = new ObjectFrequency(Character.toString(letter), 1, text.length());
				}
				frequencies.put(letter, frequency);
			}
		}
		List<ObjectFrequency> frequenciesList = new ArrayList<ObjectFrequency>(frequencies.values());
		Collections.sort(frequenciesList, (a, b) -> a.getFrequency() - b.getFrequency() > 0 ? -1 : 1);
		return frequenciesList;
	}
	
	private Map<Character,  List<Character>> findPossibleInitalLetters(List<ObjectFrequency> textLetterFrequencies) {
		Map<Character, List<Character>> letterMap = new HashMap<Character,  List<Character>>();
		for (int i = 0; i < textLetterFrequencies.size(); i ++) {
			List<Character> mostUsedLetters = new ArrayList<Character>();
			
			double textLetterFrequency = textLetterFrequencies.get(i).getFrequency();
			Collections.sort(letterFrequencies, (a, b) -> {
				double difference = Math.abs(textLetterFrequency - ((ObjectFrequency)a).getFrequency()) - Math.abs(textLetterFrequency - ((ObjectFrequency)b).getFrequency());
				return difference > 0 ? 1 : -1;
			});
			// Take the first 3 letters, which frequencies are the most close to the current letter form the piece of text
			mostUsedLetters = letterFrequencies.subList(0, 3).stream().map(item -> item.getObject().charAt(0)).collect(Collectors.toList());
			
			letterMap.put(textLetterFrequencies.get(i).getObject().charAt(0), mostUsedLetters);
		}
		return letterMap;
	}
	
	private int analyzeLetterShift(Map<Character,  List<Character>> map) {
		Map<Integer, ObjectFrequency> mostUsedShift = new HashMap();
		map.keySet().forEach((encryptedLetter) -> {
			map.get(encryptedLetter).forEach((possibleInitalLetter) -> {
				int shift = (int)encryptedLetter - possibleInitalLetter;
				shift = shift < 0 ? shift + ALPHABET_LENGTH: shift;
				
				if (mostUsedShift.containsKey(shift)) {
					ObjectFrequency existedShift = mostUsedShift.get(shift);
					existedShift.increaseOccuranceNumber();
					mostUsedShift.put(shift, existedShift);
				} else  {
					ObjectFrequency newShift = new ObjectFrequency(Integer.toString(shift), 1, map.keySet().size() * 3);
					mostUsedShift.put(shift, newShift);
				}
			});
		});
		List<ObjectFrequency> shiftsFrequency = mostUsedShift.values().stream().collect(Collectors.toList());
		Collections.sort(shiftsFrequency, (a, b) -> (int)b.getOccuranceNumber() - (int)a.getOccuranceNumber());
		
		return Integer.parseInt(shiftsFrequency.get(0).getObject());
	}
	
}
