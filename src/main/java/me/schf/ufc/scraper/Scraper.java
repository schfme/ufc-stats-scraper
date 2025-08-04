package me.schf.ufc.scraper;

import java.io.IOException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import me.schf.ufc.scraper.data.Event;

/**
 * Scraper class responsible for scraping UFC event data from the official statistics website.
 * <p>
 * It supports scraping events within a specified inclusive date range and controls the
 * delay between HTTP requests to avoid rate limiting by the server.
 * </p>
 * <p>
 * The scraper can perform both eager scraping (returning a list of events) and lazy streaming
 * of events, allowing flexible memory and performance usage.
 * </p>
 * 
 * <p><b>Note:</b> The {@code startDate} and {@code endDate} refer to the event dates and
 * are both inclusive.</p>
 */
public class Scraper {

    private final Duration linkAccessDelay;
    private final LocalDate startDate;
    private final LocalDate endDate;

    private static final String BASE_URL = "http://www.ufcstats.com/statistics/events/completed?page=all";

    private Scraper(Duration linkAccessDelay, LocalDate startDate, LocalDate endDate) {
        this.linkAccessDelay = linkAccessDelay != null ? linkAccessDelay : Duration.ofSeconds(2);
        this.startDate = startDate != null ? startDate : LocalDate.MIN;
        this.endDate = endDate != null ? endDate : LocalDate.MAX;
    }

    /**
     * Builder for creating {@link Scraper} instances with customizable configuration.
     */
    public static class Builder {
        private Duration linkAccessDelay;
        private LocalDate startDate;
        private LocalDate endDate;

        /**
         * Sets the delay between URL accesses during scraping.
         * <p>
         * This helps prevent rate limiting by the target site. If the delay is too short,
         * the server (e.g., ufcstats.com) may return HTTP 429 (Too Many Requests).
         *
         * @param delay duration to wait between each URL access
         * @return this builder instance
         */
        public Builder linkAccessDelay(Duration delay) {
            this.linkAccessDelay = delay;
            return this;
        }

        /**
         * Sets the inclusive start of the event date range to scrape.
         *
         * @param start the earliest event date (inclusive)
         * @return this builder instance
         */
        public Builder startDate(LocalDate start) {
            this.startDate = start;
            return this;
        }

        /**
         * Sets the inclusive end of the event date range to scrape.
         *
         * @param end the latest event date (inclusive)
         * @return this builder instance
         */
        public Builder endDate(LocalDate end) {
            this.endDate = end;
            return this;
        }

        /**
         * Builds and returns a configured {@link Scraper} instance for the given event date range.
         *
         * @return a new Scraper instance
         */
        public Scraper build() {
            return new Scraper(linkAccessDelay, startDate, endDate);
        }
    }
    
    /**
     * Performs an eager scrape of events within the configured date range.
     * <p>
     * This method downloads and parses all matching events immediately,
     * returning a fully materialized {@link List} of {@link Event} objects.
     * Use this when you want all results available at once.
     *
     * @return a List of scraped {@link Event} objects for the given date range (inclusive)
     * @throws IOException if a network or parsing error occurs
     */
    public List<Event> doScrape() throws IOException {
        Document allEventsPage = Jsoup.connect(BASE_URL).get();

        EventScraper eventScraper = new EventScraper(startDate, endDate, linkAccessDelay);
        return eventScraper.scrapeEvents(allEventsPage);
    }

    /**
     * Performs a lazy scrape of events within the configured date range.
     * <p>
     * This method returns a {@link Stream} of {@link Event} objects that
     * are parsed and produced one-by-one as the stream is consumed.
     * This approach is memory-efficient and suitable for processing
     * large event sets or streaming results.
     *
     * @return a Stream of scraped {@link Event} objects for the given date range (inclusive)
     * @throws IOException if a network or parsing error occurs
     */
    public Stream<Event> streamScrapedEvents() throws IOException {
        Document allEventsPage = Jsoup.connect(BASE_URL).get();
        return new EventScraper(startDate, endDate, linkAccessDelay).scrapeEventsStream(allEventsPage);
    }

}
