package cipher.cryptoanalysis;

public class ObjectFrequency {
	private String object;
	private double frequency;
	private long occuranceNumber;
	private long wholeLength;
	
	public ObjectFrequency() {}
	
	public ObjectFrequency(String object, long occuranceNumber, long textLength) {
		this.setObject(object);
		this.occuranceNumber = occuranceNumber;
		this.wholeLength = textLength;
		this.countFrequency();
	}
	
	public ObjectFrequency(String object, double frequency) {
		this.setObject(object);
		this.setFrequency(frequency);
	}

	public double getFrequency() {
		return frequency;
	}
	
	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public void countFrequency() {
		this.frequency = (double)occuranceNumber / wholeLength;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public long getOccuranceNumber() {
		return occuranceNumber;
	}
	
	public void increaseOccuranceNumber() {
		this.occuranceNumber ++;
		this.countFrequency();
	}
	
	public String toString() {
		return object + ": " + frequency;
	}
}
