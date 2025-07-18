package me.schf.ufc.scraper;

import java.time.Duration;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ScraperStreamDemo {

	public static void main(String[] args) throws Exception {
	    Scraper scraper = new Scraper.Builder()
	            .startDate(LocalDate.now().minusMonths(1))
	            .linkAccessDelay(Duration.ofSeconds(1))
	            .build();

	    ObjectMapper mapper = new ObjectMapper();
	    mapper.registerModule(new JavaTimeModule());
	    mapper.enable(SerializationFeature.INDENT_OUTPUT);

	    scraper.streamScrapedEvents().forEach(event -> {
	        try {
	            String json = mapper.writeValueAsString(event);
	            System.out.println(json);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    });
	}
}
