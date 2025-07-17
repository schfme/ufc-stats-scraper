package me.schf.ufc.scraper.data;

public class FighterStats {
	private String name;
	private int knockdowns;
	private int significantStrikes;
	private int takedowns;
	private int submissionAttempts;
	private boolean isWinner;

	private FighterStats(Builder builder) {
		this.name = builder.name;
		this.knockdowns = builder.knockdowns;
		this.significantStrikes = builder.significantStrikes;
		this.takedowns = builder.takedowns;
		this.submissionAttempts = builder.submissionAttempts;
		this.isWinner = builder.isWinner;
	}

	public String getName() {
		return name;
	}

	public int getKnockdowns() {
		return knockdowns;
	}

	public int getSignificantStrikes() {
		return significantStrikes;
	}

	public int getTakedowns() {
		return takedowns;
	}

	public int getSubmissionAttempts() {
		return submissionAttempts;
	}

	public boolean isWinner() {
		return isWinner;
	}

	public static class Builder {
		private String name;
		private int knockdowns;
		private int significantStrikes;
		private int takedowns;
		private int submissionAttempts;
		private boolean isWinner;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder knockdowns(int knockdowns) {
			this.knockdowns = knockdowns;
			return this;
		}

		public Builder significantStrikes(int significantStrikes) {
			this.significantStrikes = significantStrikes;
			return this;
		}

		public Builder takedowns(int takedowns) {
			this.takedowns = takedowns;
			return this;
		}

		public Builder submissionAttempts(int submissionAttempts) {
			this.submissionAttempts = submissionAttempts;
			return this;
		}

		public Builder isWinner(boolean isWinner) {
			this.isWinner = isWinner;
			return this;
		}

		public FighterStats build() {
			return new FighterStats(this);
		}
	}
}
