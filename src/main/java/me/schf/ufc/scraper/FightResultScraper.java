package me.schf.ufc.scraper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.schf.ufc.scraper.data.FightResult;
import me.schf.ufc.scraper.data.FighterStats;
import me.schf.ufc.scraper.data.Method;
import me.schf.ufc.scraper.data.WeightClass;


public class FightResultScraper {

    public List<FightResult> parseEventFights(Document eventDetailPage) {
        var fightResultTable = eventDetailPage.selectFirst("table.b-fight-details__table");
        if (fightResultTable == null) {
            return List.of();
        }

        var columnIndices = new FightDetailColumnIndices(
            fightResultTable.select("thead tr th")
                .stream()
                .map(th -> th.text().trim())
                .toList()
        );

        return fightResultTable.select("tbody.b-fight-details__table-body tr")
            .stream()
            .map(row -> parseFightRow(row, columnIndices))
            .toList();
    }

    private FightResult parseFightRow(Element row, FightDetailColumnIndices indices) {
        var cols = row.select("td");

        // fighters column - has <p> elements for each fighter name
        var fighterParagraphs = cols.get(indices.getFighterIndex())
            .select("p.b-fight-details__table-text");

        // winner flags column - has <p> elements with the green flag anchors if winner
        var winnerFlagParagraphs = cols.get(indices.getWlIndex())
        	    .select("p.b-fight-details__table-text");

        var kdVals = extractInts(cols.get(indices.getKdIndex()));
        var strVals = extractInts(cols.get(indices.getStrIndex()));
        var tdVals = extractInts(cols.get(indices.getTdIndex()));
        var subVals = extractInts(cols.get(indices.getSubIndex()));

        var fighterStatsList = new ArrayList<FighterStats>();

        for (int i = 0; i < fighterParagraphs.size(); i++) {
            var fighterPara = fighterParagraphs.get(i);
            var fighterLink = fighterPara.selectFirst("a.b-link_style_black");
            if (fighterLink == null) continue;

            String fighterName = fighterLink.text().trim();

            // corresponding winner flag <p> element for this fighter
            boolean isWinner = false;
            if (i < winnerFlagParagraphs.size()) {
                var winnerPara = winnerFlagParagraphs.get(i);
                isWinner = winnerPara.selectFirst("a.b-flag_style_green") != null;
            }
            
            var fighterStats = new FighterStats.Builder()
                .name(fighterName)
                .knockdowns(kdVals.get(i))
                .significantStrikes(strVals.get(i))
                .takedowns(tdVals.get(i))
                .submissionAttempts(subVals.get(i))
                .isWinner(isWinner)
                .build();

            fighterStatsList.add(fighterStats);
        }

        var weightClass = WeightClass.fromText(getTextSafe(cols.get(indices.getWeightClassIndex())));
        var method = Method.fromText(getTextSafe(cols.get(indices.getMethodIndex())));
        var round = getTextSafe(cols.get(indices.getRoundIndex()));
        var finalRoundEndTime = parseFightTime(getTextSafe(cols.get(indices.getTimeIndex())));
        var weightClassCol = cols.get(indices.getWeightClassIndex());
        // checking if the row has the little belt picture :)
        boolean isTitleFight = !weightClassCol.select("img[src*=belt.png]").isEmpty();

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

	private static String getTextSafe(Element td) {
		return Optional.ofNullable(td.selectFirst("p.b-fight-details__table-text")).map(Element::text).map(String::trim)
				.orElse("");
	}

	private static Duration parseFightTime(String timeStr) {
		if (timeStr == null || timeStr.isBlank()) {
			return Duration.ZERO;
		}
		var parts = timeStr.split(":");
		var minutes = Integer.parseInt(parts[0]);
		var seconds = Integer.parseInt(parts[1]);
		return Duration.ofMinutes(minutes).plusSeconds(seconds);
	}


}
