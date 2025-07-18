package me.schf.ufc.scraper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jsoup.nodes.Element;

public class Utils {

    public static List<Integer> extractInts(Element col) {
        var values = new ArrayList<Integer>();
        for (var p : col.select("p.b-fight-details__table-text")) {
            try {
                values.add(Integer.parseInt(p.text().trim()));
            } catch (NumberFormatException e) {
                values.add(null);
            }
        }
        return values;
    }

    public static String getTextSafe(Element td) {
        return Optional.ofNullable(td.selectFirst("p.b-fight-details__table-text"))
                .map(Element::text)
                .map(String::trim)
                .orElse("");
    }

    public static Duration parseFightTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return Duration.ZERO;
        }
        var parts = timeStr.split(":");
        var minutes = Integer.parseInt(parts[0]);
        var seconds = Integer.parseInt(parts[1]);
        return Duration.ofMinutes(minutes).plusSeconds(seconds);
    }
}
