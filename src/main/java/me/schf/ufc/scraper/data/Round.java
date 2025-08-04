package me.schf.ufc.scraper.data;

public enum Round {
	ONE("1"), 
	TWO("2"), 
	THREE("3"), 
	FOUR("4"), 
	FIVE("5");

	private final String text;

	Round(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
    public static Round fromText(String text) {
        for (Round round : values()) {
            if (round.text.equalsIgnoreCase(text)) {
                return round;
            }
        }
        throw new IllegalArgumentException("Unknown round: " + text);
    }
}
