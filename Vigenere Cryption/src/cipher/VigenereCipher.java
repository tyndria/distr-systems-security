package cipher;
import org.apache.commons.lang3.ArrayUtils;

public class VigenereCipher implements Constants{
	
	public String encrypt(String text, String keyWord) {
		StringBuilder encryptedText = new StringBuilder();
		int keyWordLength = keyWord.length();
		for (int i = 0; i < text.length(); i ++) {
			if (checkSpecSymbol(encryptedText, text.charAt(i)) == null) {
				char textChar = text.charAt(i);
				int ALPHABET_START_CODE = getAlphabetStartCode(textChar);
				int ALPHABET_END_CODE = getAlphabetEndCode(textChar);
				int textCharShift = textChar - ALPHABET_START_CODE;
				char keywordChar = keyWord.charAt(i < keyWordLength ? i : i % keyWordLength);
				keywordChar = isUpperAlphabet(textChar) ? Character.toUpperCase(keywordChar) : keywordChar;
				int encryptedCharCode = textCharShift + keywordChar;
				encryptedCharCode = encryptedCharCode > ALPHABET_END_CODE ? 
						(encryptedCharCode - ALPHABET_LENGTH) : encryptedCharCode;
				encryptedText.append((char)(encryptedCharCode));
			}
		}
		return encryptedText.toString();
	}
	
	public String decrypt(String text, String keyWord) {
		StringBuilder decryptedText = new StringBuilder();
		int keyWordLength = keyWord.length();
		for (int i = 0; i < text.length(); i ++) {
			if (checkSpecSymbol(decryptedText, text.charAt(i)) == null) {
				char textChar = text.charAt(i);
				int ALPHABET_START_CODE = getAlphabetStartCode(textChar);
				char keywordChar = keyWord.charAt(i < keyWordLength ? i : i % keyWordLength);
				keywordChar = isUpperAlphabet(textChar) ? Character.toUpperCase(keywordChar) : keywordChar;
				int textCharShift = textChar - keywordChar;
				int decryptedCharCode = ALPHABET_START_CODE + textCharShift;
				decryptedCharCode = decryptedCharCode < ALPHABET_START_CODE ? 
						(decryptedCharCode + ALPHABET_LENGTH) : decryptedCharCode;
				decryptedText.append((char)(decryptedCharCode));
			}
		}
		return decryptedText.toString();
	}
	
	private StringBuilder checkSpecSymbol(StringBuilder text, char a) {
		if(ArrayUtils.indexOf(SPECIAL_SYMBOLS, a) != -1) {
			return text.append(a);
		}
		return null;
	}
	
	private int getAlphabetStartCode(char charCode) {
		return (int)charCode < LOWER_ALPHABET_START_CODE ? UPPER_ALPHABET_START_CODE : LOWER_ALPHABET_START_CODE;
	}
	
	private int getAlphabetEndCode(char charCode) {
		return (int)charCode < LOWER_ALPHABET_START_CODE ? UPPER_ALPHABET_END_CODE : LOWER_ALPHABET_END_CODE;
	}
	
	private boolean isUpperAlphabet(char a) {
		return (int)a < LOWER_ALPHABET_START_CODE;
	}
}
