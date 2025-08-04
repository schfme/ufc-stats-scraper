package me.schf.ufc.scraper;

import java.time.Duration;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ScraperDemo {

    public static void main(String[] args) throws Exception {
    	
        LocalDate targetDate = LocalDate.of(2025, 6, 7);

        Scraper scraper = new Scraper.Builder()
                .startDate(targetDate) 					// Both start and end date are inclusive.
                .endDate(targetDate)
                .linkAccessDelay(Duration.ofSeconds(1)) // Delay between URL accesses to avoid rate-limiting.
                                                        // A value too low may trigger a HTTP 429 (Too Many Requests) from ufcstats.
                                                        // Be respectful to the source and avoid hammering the site.
                .build();

        // Example 1: doScrape() eagerly scrapes all events into memory and returns them as a list
        scraper.doScrape().stream().forEach(ScraperDemo::prettyPrintAsJson);

        // Example 2: streamScrapedEvents() lazily streams scraped events one-by-one as they are parsed
        scraper.streamScrapedEvents().forEach(ScraperDemo::prettyPrintAsJson);
    }

    private static void prettyPrintAsJson(Object event) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String json = mapper.writeValueAsString(event);
            System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
