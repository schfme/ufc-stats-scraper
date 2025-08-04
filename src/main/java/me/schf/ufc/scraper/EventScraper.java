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

    private static class Selectors {
        static final String ROW = "tr.b-statistics__table-row";
        static final String HEADER_CELL = "th";
        static final String DATE_SPAN = "span.b-statistics__date";
        static final String EVENT_LINK = "a[href*=/event-details/]";
        static final String TABLE_CELL = "td";
    }

    public EventScraper(LocalDate startDate, LocalDate endDate, Duration linkAccessDelay) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.linkAccessDelay = linkAccessDelay;
    }

    public Stream<Event> scrapeEventsStream(Document allEventsPage) {
        return allEventsPage.select(Selectors.ROW).stream()
                .map(EventRowParser::new)
                .filter(parser -> !parser.isHeader() && !parser.isEmpty())
                .filter(parser -> isWithinDateRange(parser.getEventDate()))
                .map(this::scrapeEvent)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public List<Event> scrapeEvents(Document allEventsPage) {
        List<Event> events = new ArrayList<>();
        for (Element row : allEventsPage.select(Selectors.ROW)) {
            EventRowParser parser = new EventRowParser(row);

            if (parser.isHeader() || parser.isEmpty() || !isWithinDateRange(parser.getEventDate())) {
                continue;
            }

            scrapeEvent(parser).ifPresent(events::add);
        }
        return events;
    }


    private Optional<Event> scrapeEvent(EventRowParser parser) {
        try {
            Optional<Element> linkElOpt = parser.getEventLink();
            if (linkElOpt.isEmpty()) {
                return Optional.empty();
            }

            Element linkEl = linkElOpt.get();
            String eventName = linkEl.text().trim();
            String eventDetailLink = linkEl.attr("href");

            Thread.sleep(linkAccessDelay.toMillis());

            Document eventDetailPage = Jsoup.connect(eventDetailLink).get();
            FightResultScraper fightResultScraper = new FightResultScraper();
            var fightResults = fightResultScraper.parseEventFights(eventDetailPage);

            Event event = new Event.Builder()
                    .eventName(eventName)
                    .eventDate(parser.getEventDate())
                    .fightResults(fightResults)
                    .build();

            return Optional.of(event);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Thread interrupted while scraping event row", e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to scrape event", e);
            return Optional.empty();
        }
    }

    private boolean isWithinDateRange(LocalDate eventDate) {
        return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
    }

    private static class EventRowParser {
        private final Element row;

        EventRowParser(Element row) {
            this.row = row;
        }

        boolean isHeader() {
            return !row.select(Selectors.HEADER_CELL).isEmpty();
        }

        boolean isEmpty() {
            return row.select(Selectors.TABLE_CELL).stream()
                    .allMatch(td -> td.text().trim().isEmpty());
        }

        LocalDate getEventDate() {
            return getEventDate(EVENT_DATE_FORMAT);
        }

        LocalDate getEventDate(DateTimeFormatter formatter) {
            String dateText = row.selectFirst(Selectors.DATE_SPAN).text();
            return LocalDate.parse(dateText, formatter);
        }

        Optional<Element> getEventLink() {
            return Optional.ofNullable(row.selectFirst(Selectors.EVENT_LINK));
        }
    }
}
