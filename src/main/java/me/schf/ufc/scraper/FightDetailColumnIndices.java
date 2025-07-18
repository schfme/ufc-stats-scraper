package me.schf.ufc.scraper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FightDetailColumnIndices {

	private int wlIndex;
	private int fighterIndex;
	private int kdIndex;
	private int strIndex;
	private int tdIndex;
	private int subIndex;
	private int weightClassIndex;
	private int methodIndex;
	private int roundIndex;
	private int timeIndex;

	public FightDetailColumnIndices(List<String> columnNames) {
		Set<String> found = new HashSet<>();

		for (var i = 0; i < columnNames.size(); i++) {
			var name = columnNames.get(i);
			switch (name) {
			case "W/L" -> {
				wlIndex = i;
				found.add(name);
			}
			case "Fighter" -> {
				fighterIndex = i;
				found.add(name);
			}
			case "Kd" -> {
				kdIndex = i;
				found.add(name);
			}
			case "Str" -> {
				strIndex = i;
				found.add(name);
			}
			case "Td" -> {
				tdIndex = i;
				found.add(name);
			}
			case "Sub" -> {
				subIndex = i;
				found.add(name);
			}
			case "Weight class" -> {
				weightClassIndex = i;
				found.add(name);
			}
			case "Method" -> {
				methodIndex = i;
				found.add(name);
			}
			case "Round" -> {
				roundIndex = i;
				found.add(name);
			}
			case "Time" -> {
				timeIndex = i;
				found.add(name);
			}
			default -> {
				throw new IllegalStateException("Unknown column: %s".formatted(name));
			}
			}
		}

		List<String> required = List.of("W/L", "Fighter", "Kd", "Str", "Td", "Sub", "Weight class", "Method", "Round",
				"Time");
		var missing = required.stream().filter(r -> !found.contains(r)).toList();

		if (!missing.isEmpty()) {
			throw new IllegalStateException("Missing required columns: %s".formatted(missing));
		}
	}

	public int getWlIndex() {
		return wlIndex;
	}

	public int getFighterIndex() {
		return fighterIndex;
	}

	public int getKdIndex() {
		return kdIndex;
	}

	public int getStrIndex() {
		return strIndex;
	}

	public int getTdIndex() {
		return tdIndex;
	}

	public int getSubIndex() {
		return subIndex;
	}

	public int getWeightClassIndex() {
		return weightClassIndex;
	}

	public int getMethodIndex() {
		return methodIndex;
	}

	public int getRoundIndex() {
		return roundIndex;
	}

	public int getTimeIndex() {
		return timeIndex;
	}
}
