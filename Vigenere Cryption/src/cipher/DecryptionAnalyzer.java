package cipher;

public class DecryptionAnalyzer {
	public DecryptionAnalyzer() {}
	
	public double letterMatchesPercentage (String initalText, String decryptedText) {
		long number = 0;
		for (int i = 0; i < initalText.length(); i ++) {
			if (initalText.charAt(i) == decryptedText.charAt(i)) {
				number ++;
			}
		}
		return (double)number/initalText.length();
	}
}
