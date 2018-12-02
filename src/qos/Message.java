package qos;

public class Message {
	public static final char X = 'X';
	public static final char Y = 'Y';
	
	private char type;
	private String value;
	
	public Message (char type, String value) {
		this.type = type;
		this.value = value;
	}
	
	public char getType() {
		return this.type;
	}

	public String getValue() {
		return this.value;
	}
	
	public static Message X(String value) {
		Message x1 = new Message(X, value);
		return x1;
	}

	public static Message Y(String value) {
		Message y1 = new Message(Y, value);
		return y1;
	}
	
	/*
	 * Dump Message object to string.
	 * */
	public String toString() {
		String out = String.format("<<%s::%s>>", this.type, this.value);
		return out;
	}

	/*
	 * Parse string to Message object.
	 * */
	public static Message fromString(String input) {
		char t = input.substring(2, 3).toCharArray()[0]; // Get either X or Y
		String v = input.substring(5, input.length() -2); // Extract content		
		return new Message(t, v);
	}
}
