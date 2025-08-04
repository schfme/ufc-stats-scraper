package me.schf.ufc.scraper.data;

import java.time.Duration;
import java.util.List;

public class FightResult {

	private List<FighterStats> fighterStatsList;
	private WeightClass weightClass;
	private Method method;
	private Round round;
	private Duration finalRoundEndTime;
	private boolean isTitleFight;

	private FightResult(Builder builder) {
		this.fighterStatsList = builder.fighterStatsList;
		this.weightClass = builder.weightClass;
		this.method = builder.method;
		this.round = builder.round;
		this.finalRoundEndTime = builder.finalRoundEndTime;
		this.isTitleFight = builder.isTitleFight;
	}

	public List<FighterStats> getFighterStatsList() {
		return fighterStatsList;
	}

	public WeightClass getWeightClass() {
		return weightClass;
	}

	public Method getMethod() {
		return method;
	}

	public Round getRound() {
		return round;
	}

	public Duration getFinalRoundEndTime() {
		return finalRoundEndTime;
	}

	public boolean isTitleFight() {
		return isTitleFight;
	}

	public static class Builder {
		private List<FighterStats> fighterStatsList;
		private WeightClass weightClass;
		private Method method;
		private Round round;
		private Duration finalRoundEndTime;
		private boolean isTitleFight;

		public Builder fighterStatsList(List<FighterStats> fighterStatsList) {
			this.fighterStatsList = fighterStatsList;
			return this;
		}

		public Builder weightClass(WeightClass weightClass) {
			this.weightClass = weightClass;
			return this;
		}

		public Builder method(Method method) {
			this.method = method;
			return this;
		}

		public Builder round(Round round) {
			this.round = round;
			return this;
		}

		public Builder finalRoundEndTime(Duration finalRoundEndTime) {
			this.finalRoundEndTime = finalRoundEndTime;
			return this;
		}

		public Builder isTitleFight(boolean isTitleFight) {
			this.isTitleFight = isTitleFight;
			return this;
		}

		public FightResult build() {
			return new FightResult(this);
		}
	}

	@Override
	public String toString() {
		return "FightResult [fighterStatsList=" + fighterStatsList + ", weightClass=" + weightClass + ", method="
				+ method + ", round=" + round + ", finalRoundEndTime=" + finalRoundEndTime + ", isTitleFight="
				+ isTitleFight + "]";
	}

}
