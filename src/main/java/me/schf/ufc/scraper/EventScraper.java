package me.schf.ufc.scraper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.schf.ufc.scraper.data.Event;

public class EventScraper {

    private static final DateTimeFormatter EVENT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
    
    private static final Logger LOGGER = Logger.getLogger(EventScraper.class.getName());

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Duration linkAccessDelay;

    public EventScraper(LocalDate startDate, LocalDate endDate, Duration linkAccessDelay) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.linkAccessDelay = linkAccessDelay;
    }

    public Stream<Event> scrapeEventsStream(Document allEventsPage) {
        return allEventsPage.select("tr.b-statistics__table-row").stream()
            .filter(row -> row.select("th").isEmpty())
            .map(this::scrapeEventRow)
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    public List<Event> scrapeEvents(Document allEventsPage) {
        var events = new ArrayList<Event>();

        var rows = allEventsPage.select("tr.b-statistics__table-row");
        for (Element row : rows) {
            if (!row.select("th").isEmpty()) {
                continue;
            }

            Optional<Event> maybeEvent = scrapeEventRow(row);
            maybeEvent.ifPresent(events::add);
        }

        return events;
    }

    private Optional<Event> scrapeEventRow(Element row) {
        try {
            // skip empty rows
            if (row.select("td").stream().allMatch(td -> td.text().trim().isEmpty())) {
                return Optional.empty();
            }

            var eventDate = extractDate(row);
            if (!isWithinDateRange(eventDate)) {
                return Optional.empty();
            }

            var linkEl = row.selectFirst("a[href*=/event-details/]");
            if (linkEl == null) {
                return Optional.empty();
            }

            var eventName = linkEl.text().trim();
            var eventDetailLink = linkEl.attr("href");

            Thread.sleep(linkAccessDelay.toMillis());

            var eventDetailPage = Jsoup.connect(eventDetailLink).get();
            var fightResultScraper = new FightResultScraper();
            var fightResults = fightResultScraper.parseEventFights(eventDetailPage);

            Event event = new Event.Builder()
                    .eventName(eventName)
                    .eventDate(eventDate)
                    .fightResults(fightResults)
                    .build();

            return Optional.of(event);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Thread interrupted while scraping event row", e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get event row.", e);
            return Optional.empty();
        }
    }

    private LocalDate extractDate(Element row) {
        var textDate = row.selectFirst("span.b-statistics__date").text();
        return LocalDate.parse(textDate, EVENT_DATE_FORMAT);
    }

    private boolean isWithinDateRange(LocalDate eventDate) {
        return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
    }
}
