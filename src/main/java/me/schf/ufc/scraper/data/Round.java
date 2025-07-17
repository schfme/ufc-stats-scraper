package me.schf.ufc.scraper.data;

public enum Round {
	ONE(1), 
	TWO(2), 
	THREE(3), 
	FOUR(4), 
	FIVE(5);

	private int roundNumber;

	private Round(int roundNumber) {
		this.roundNumber = roundNumber;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

}
