package me.schf.ufc.scraper.data;

import java.time.LocalDate;
import java.util.List;

public class Event {
	private final String eventName;
	private final LocalDate eventDate;
	private final List<FightResult> fightResults;

	private Event(Builder builder) {
		this.eventName = builder.eventName;
		this.eventDate = builder.eventDate;
		this.fightResults = builder.fightResults;
	}

	public String getEventName() {
		return eventName;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public List<FightResult> getFightResults() {
		return fightResults;
	}

	public static class Builder {
		private String eventName;
		private LocalDate eventDate;
		private List<FightResult> fightResults;

		public Builder eventName(String eventName) {
			this.eventName = eventName;
			return this;
		}

		public Builder eventDate(LocalDate eventDate) {
			this.eventDate = eventDate;
			return this;
		}

		public Builder fightResults(List<FightResult> fightResults) {
			this.fightResults = fightResults;
			return this;
		}

		public Event build() {
			return new Event(this);
		}
	}

	@Override
	public String toString() {
		return "Event [eventName=" + eventName + ", eventDate=" + eventDate + ", fightResults=" + fightResults + "]";
	}

}
