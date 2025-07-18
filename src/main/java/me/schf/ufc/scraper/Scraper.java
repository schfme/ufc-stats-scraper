package me.schf.ufc.scraper;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import me.schf.ufc.scraper.data.Event;

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

    public static class Builder {
        private Duration linkAccessDelay;
        private LocalDate startDate;
        private LocalDate endDate;

        public Builder linkAccessDelay(Duration delay) {
            this.linkAccessDelay = delay;
            return this;
        }

        public Builder startDate(LocalDate start) {
            this.startDate = start;
            return this;
        }

        public Builder endDate(LocalDate end) {
            this.endDate = end;
            return this;
        }

        public Scraper build() {
            return new Scraper(linkAccessDelay, startDate, endDate);
        }
    }

    public List<Event> doScrape() throws IOException, InterruptedException {
        Document allEventsPage = Jsoup.connect(BASE_URL).get();

        EventScraper eventScraper = new EventScraper(startDate, endDate, linkAccessDelay);
        return eventScraper.scrapeEvents(allEventsPage);
    }
    
    public Stream<Event> streamScrapedEvents() throws IOException {
        Document allEventsPage = Jsoup.connect(BASE_URL).get();
        return new EventScraper(startDate, endDate, linkAccessDelay).scrapeEventsStream(allEventsPage);
    }


}
