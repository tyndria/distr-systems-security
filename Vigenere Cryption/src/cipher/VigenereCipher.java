package cipher;

public class VigenereCipher implements SymbolCode{
	
	public String encrypt(String text, String keyWord) {
		StringBuilder encryptedText = new StringBuilder();
		int keyWordLength = keyWord.length();
		for (int i = 0; i < text.length(); i ++) {
			if (checkSpecSymbol(encryptedText, text.charAt(i)) == null) {
				char keyWordChar = keyWord.charAt(i < keyWordLength ? i : i % keyWordLength);
				char textChar = text.charAt(i);
				int ALPHABET_START_CODE = getAlphabetStartCode(textChar);
				int ALPHABET_END_CODE = getAlphabetEndCode(textChar);
				int textCharShift = textChar - ALPHABET_START_CODE;
				int encryptedCharCode = textCharShift + keyWordChar;
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
				char keyWordChar = keyWord.charAt(i < keyWordLength ? i : i % keyWordLength);
				char textChar = text.charAt(i);
				int ALPHABET_START_CODE = getAlphabetStartCode(textChar);
				int textCharShift = textChar - keyWordChar;
				int decryptedCharCode = ALPHABET_START_CODE + textCharShift;
				decryptedCharCode = decryptedCharCode < ALPHABET_START_CODE ? 
						(decryptedCharCode + ALPHABET_LENGTH) : decryptedCharCode;
				decryptedText.append((char)(decryptedCharCode));
			}
		}
		return decryptedText.toString();
	}
	
	private static StringBuilder checkSpecSymbol(StringBuilder text, char a) {
		if (a == SPACE) {
			return text.append(" ");
		} else if (a == DOT) {
			return text.append(".");
		} else if (a == COMMA) {
			return text.append(",");
		}
		return null;
	}
	
	private static int getAlphabetStartCode(char charCode) {
		return (int)charCode < LOWER_ALPHABET_START_CODE ? UPPER_ALPHABET_START_CODE : LOWER_ALPHABET_START_CODE;
	}
	
	private static int getAlphabetEndCode(char charCode) {
		return (int)charCode < LOWER_ALPHABET_START_CODE ? UPPER_ALPHABET_END_CODE : LOWER_ALPHABET_END_CODE;
	}
}
