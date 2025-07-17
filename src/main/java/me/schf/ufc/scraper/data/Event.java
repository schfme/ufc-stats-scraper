package me.schf.ufc.scraper.data;

import java.time.LocalDate;
import java.util.List;

public record Event(
		String eventName, 
		LocalDate eventDate,
		List<FightResult> fightResults
	) {}
