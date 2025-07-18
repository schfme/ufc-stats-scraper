package me.schf.ufc.scraper;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.schf.ufc.scraper.data.FightResult;
import me.schf.ufc.scraper.data.FighterStats;

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

        var kdVals = Utils.extractInts(cols.get(indices.getKdIndex()));
        var strVals = Utils.extractInts(cols.get(indices.getStrIndex()));
        var tdVals = Utils.extractInts(cols.get(indices.getTdIndex()));
        var subVals = Utils.extractInts(cols.get(indices.getSubIndex()));

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

        var weightClass = Utils.getTextSafe(cols.get(indices.getWeightClassIndex()));
        var method = Utils.getTextSafe(cols.get(indices.getMethodIndex()));
        var round = Utils.getTextSafe(cols.get(indices.getRoundIndex()));
        var finalRoundEndTime = Utils.parseFightTime(Utils.getTextSafe(cols.get(indices.getTimeIndex())));
        var isTitleFight = row.select(".b-fight-details__fight-title").text().toLowerCase().contains("title");

        return new FightResult.Builder()
            .fighterStatsList(fighterStatsList)
            .weightClass(weightClass)
            .method(method)
            .round(round)
            .finalRoundEndTime(finalRoundEndTime)
            .isTitleFight(isTitleFight)
            .build();
    }


}
