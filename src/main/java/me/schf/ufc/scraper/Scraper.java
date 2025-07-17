package me.schf.ufc.scraper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import me.schf.ufc.scraper.data.Event;
import me.schf.ufc.scraper.data.FightResult;
import me.schf.ufc.scraper.data.FighterStats;

public class Scraper {

	private static final String BASE_URL = "http://www.ufcstats.com/statistics/events/completed?page=all";
	private static final DateTimeFormatter EVENT_DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy",
			Locale.ENGLISH);

	/*
	 * Default values.
	 */
	private Duration linkAccessDelay = Duration.ofSeconds(1);
	private LocalDate startDate = LocalDate.MIN;
	private LocalDate endDate = LocalDate.MAX;

	private Scraper(Duration linkAccessDelay, LocalDate startDate, LocalDate endDate) {
		super();
		if (linkAccessDelay != null) this.linkAccessDelay = linkAccessDelay;
		if (startDate != null) this.startDate = startDate;
		if (startDate != null) this.startDate = startDate;
	}

	public static class Builder {
		private Duration linkAccessDelay;
		private LocalDate startDate;
		private LocalDate endDate;

		public Builder linkAccessDelay(Duration linkAccessDelay) {
			this.linkAccessDelay = linkAccessDelay;
			return this;
		}

		public Builder startDate(LocalDate startDate) {
			this.startDate = startDate;
			return this;
		}

		public Builder endDate(LocalDate endDate) {
			this.endDate = endDate;
			return this;
		}

		public Scraper build() {
			return new Scraper(linkAccessDelay, startDate, endDate);
		}

	}

	public List<Event> doScrape() throws IOException, InterruptedException {

		List<Event> events = new ArrayList<>();
		Document allEventsPage = Jsoup.connect(BASE_URL).get();

		List<Element> eventTableRows = allEventsPage.select("tr.b-statistics__table-row");
		eventTableRows.forEach(System.out::println);
		for (Element eventTableRow : eventTableRows) {
//			LocalDate eventDate = extractDate(eventTableRow);
			LocalDate eventDate = LocalDate.now().minusDays(1);
			if (isAfterStartDate(eventDate) && isBeforeEndDate(eventDate)) {
				System.out.println(eventTableRow);
				Element eventDetail = eventTableRow.selectFirst("a[href*=/event-details/]");
				String eventName = eventDetail.text().trim();
				String eventDetailLink = eventDetail.attr("href");

				Thread.sleep(linkAccessDelay.toMillis());

				Document eventDetailPage = Jsoup.connect(eventDetailLink).get();
				List<FightResult> fightResults = parseEventFights(eventDetailPage);
				events.add(new Event(eventName, eventDate, fightResults));
			}

		}

		return events;
	}

	private static List<FightResult> parseEventFights(Document eventDetailPage) {
		Element fightResultTable = eventDetailPage.selectFirst("table.b-fight-details__table");

		FightDetailColumnIndices fightDetailColumnIndices = fightResultTable.select("thead tr th")
				.stream()
				.map(th -> th.text().trim())
				.collect(Collectors.collectingAndThen(Collectors.toList(), FightDetailColumnIndices::new));

		return fightResultTable.select("tbody.b-fight-details__table-body tr")
				.stream()
				.map(row -> parseFightRow(row, fightDetailColumnIndices))
				.toList();
	}

	private static FightResult parseFightRow(Element row, FightDetailColumnIndices fightDetailColumnIndices) {
		Elements columns = row.select("td");

		var fighters = columns.get(fightDetailColumnIndices.getFighterIndex())
				.select("p.b-fight-details__table-text a");

		assertThat(fighters.size()).isEqualTo(2).withFailMessage("erm awkward");

		List<Integer> kdVals = extractInts(columns.get(fightDetailColumnIndices.getKdIndex()));
		List<Integer> strVals = extractInts(columns.get(fightDetailColumnIndices.getStrIndex()));
		List<Integer> tdVals = extractInts(columns.get(fightDetailColumnIndices.getTdIndex()));
		List<Integer> subVals = extractInts(columns.get(fightDetailColumnIndices.getSubIndex()));

		List<FighterStats> fighterStatsList = new ArrayList<>();
		for (int i = 0; i < fighters.size(); i++) {
			FighterStats fighterStats = new FighterStats.Builder()
					.name(fighters.get(i).text().trim())
					.knockdowns(kdVals.get(i))
					.significantStrikes(strVals.get(i))
					.takedowns(tdVals.get(i))
					.submissionAttempts(subVals.get(i))
					.isWinner(false) // TODO
					.build();
			fighterStatsList.add(fighterStats);
		}
		
		String weightClass = getTextSafe(columns.get(fightDetailColumnIndices.getWeightClassIndex()));
		String method = getTextSafe(columns.get(fightDetailColumnIndices.getMethodIndex()));
		String round = getTextSafe(columns.get(fightDetailColumnIndices.getRoundIndex()));
		Duration finalRoundEndTime = parseFightTime(getTextSafe(columns.get(fightDetailColumnIndices.getTimeIndex())));

		boolean isTitleFight = row.select(".b-fight-details__fight-title").text().toLowerCase().contains("title");

		return new FightResult.Builder()
				.fighterStatsList(fighterStatsList)
				.weightClass(weightClass)
				.method(method)
				.round(round)
				.finalRoundEndTime(finalRoundEndTime)
				.isTitleFight(isTitleFight)
				.build();
	}
	
    private static List<Integer> extractInts(Element col) {
        List<Integer> values = new ArrayList<>();
        Elements ps = col.select("p.b-fight-details__table-text");
        for (Element p : ps) {
            try {
                values.add(Integer.parseInt(p.text().trim()));
            } catch (NumberFormatException e) {
                values.add(null);
            }
        }
        return values;
    }

	public static class FightDetailColumnIndices {
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

	    private FightDetailColumnIndices(List<String> columnNames) {
	        Set<String> found = new HashSet<>();

			for (int i = 0; i < columnNames.size(); i++) {
				String name = columnNames.get(i);
				switch (name) {
				case "W/L":
					this.wlIndex = i;
					found.add(name);
					break;
				case "Fighter":
					this.fighterIndex = i;
					found.add(name);
					break;
				case "Kd":
					this.kdIndex = i;
					found.add(name);
					break;
				case "Str":
					this.strIndex = i;
					found.add(name);
					break;
				case "Td":
					this.tdIndex = i;
					found.add(name);
					break;
				case "Sub":
					this.subIndex = i;
					found.add(name);
					break;
				case "Weight class":
					this.weightClassIndex = i;
					found.add(name);
					break;
				case "Method":
					this.methodIndex = i;
					found.add(name);
					break;
				case "Round":
					this.roundIndex = i;
					found.add(name);
					break;
				case "Time":
					this.timeIndex = i;
					found.add(name);
					break;
				}
			}

	        List<String> required = List.of(
	            "W/L", "Fighter", "Kd", "Str", "Td", "Sub", 
	            "Weight class", "Method", "Round", "Time"
	        );

	        List<String> missing = required.stream()
	            .filter(f -> !found.contains(f))
	            .toList();

	        assertThat(missing)
	            .as("Missing required columns")
	            .isEmpty();
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


	private static String getTextSafe(Element td) {
		return Optional.ofNullable(td.selectFirst("p.b-fight-details__table-text"))
				.map(Element::text)
				.map(String::trim)
	            .orElse("");
	}

	private static Duration parseFightTime(String timeStr) {
		String[] parts = timeStr.split(":");
		int minutes = Integer.parseInt(parts[0]);
		int seconds = Integer.parseInt(parts[1]);
		return Duration.ofMinutes(minutes).plusSeconds(seconds);
	}

	private boolean isAfterStartDate(LocalDate eventDate) {
		return eventDate.isAfter(startDate);
	}

	private boolean isBeforeEndDate(LocalDate eventDate) {
		return eventDate.isBefore(endDate);
	}

	private LocalDate extractDate(Element element) {
		String textDate = element.selectFirst("span.b-statistics__date").text();
		LocalDate eventDate = LocalDate.parse(textDate, EVENT_DATE_FORMAT);
		return eventDate;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Scraper scraper = new Scraper.Builder().build();
		Set<String> weightClasses = new HashSet<>();
		Set<String> outcomes = new HashSet<>();
		for (Event event : scraper.doScrape()) {
			for (FightResult fightResult : event.fightResults()) {
				weightClasses.add(fightResult.getWeightClass());
				outcomes.add(fightResult.getMethod());

			}
		}
		System.out.println("----OUTCOMES----");
		outcomes.forEach(System.out::println);
		System.out.println("----WEIGHT CLASSES----");
		weightClasses.forEach(System.out::println);
	}
}
