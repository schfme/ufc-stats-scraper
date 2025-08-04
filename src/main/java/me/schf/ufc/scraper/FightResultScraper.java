package me.schf.ufc.scraper;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.schf.ufc.scraper.data.FightResult;
import me.schf.ufc.scraper.data.FighterStats;
import me.schf.ufc.scraper.data.Method;
import me.schf.ufc.scraper.data.Round;
import me.schf.ufc.scraper.data.WeightClass;

public class FightResultScraper {

    private static class Selectors {
        static final String FIGHT_DETAILS_TABLE = "table.b-fight-details__table";
        static final String COLUMN_HEADERS = "thead tr th";
        static final String TABLE_BODY_ROWS = "tbody.b-fight-details__table-body tr";
        static final String TABLE_CELL = "td";
        static final String FIGHTER_PARAGRAPHS = "p.b-fight-details__table-text";
        static final String FIGHTER_LINK = "a.b-link_style_black";
        static final String WINNER_FLAG_PARAGRAPHS = "p.b-fight-details__table-text";
        static final String WINNER_FLAG_ANCHOR = "a.b-flag_style_green";
        static final String BELT_IMAGE = "img[src*=belt.png]";
    }

    public List<FightResult> parseEventFights(Document eventDetailPage) {
        var fightResultTable = eventDetailPage.selectFirst(Selectors.FIGHT_DETAILS_TABLE);
        if (fightResultTable == null) {
            return List.of();
        }

        var columnIndices = new FightDetailColumnIndices(
            fightResultTable.select(Selectors.COLUMN_HEADERS)
                .stream()
                .map(Element::text)
                .map(String::trim)
                .toList()
        );

        return fightResultTable.select(Selectors.TABLE_BODY_ROWS)
            .stream()
            .map(row -> parseFightRow(row, columnIndices))
            .toList();
    }

    private FightResult parseFightRow(Element row, FightDetailColumnIndices indices) {
        var cols = row.select(Selectors.TABLE_CELL);

        var fighterParagraphs = cols.get(indices.getFighterIndex())
            .select(Selectors.FIGHTER_PARAGRAPHS);

        var winnerFlagParagraphs = cols.get(indices.getWlIndex())
            .select(Selectors.WINNER_FLAG_PARAGRAPHS);

        var kdVals = extractInts(cols.get(indices.getKdIndex()));
        var strVals = extractInts(cols.get(indices.getStrIndex()));
        var tdVals = extractInts(cols.get(indices.getTdIndex()));
        var subVals = extractInts(cols.get(indices.getSubIndex()));

        var fighterStatsList = IntStream.range(0, fighterParagraphs.size())
            .mapToObj(i -> {
                var fighterPara = fighterParagraphs.get(i);
                var fighterLink = fighterPara.selectFirst(Selectors.FIGHTER_LINK);
                if (fighterLink == null) return null;

                String fighterName = fighterLink.text().trim();

                boolean isWinner = i < winnerFlagParagraphs.size() &&
                    winnerFlagParagraphs.get(i).selectFirst(Selectors.WINNER_FLAG_ANCHOR) != null;

                return new FighterStats.Builder()
                    .name(fighterName)
                    .knockdowns(kdVals.get(i))
                    .significantStrikes(strVals.get(i))
                    .takedowns(tdVals.get(i))
                    .submissionAttempts(subVals.get(i))
                    .isWinner(isWinner)
                    .build();
            })
            .filter(Objects::nonNull)
            .toList();

        var weightClass = WeightClass.fromText(getTextSafe(cols.get(indices.getWeightClassIndex())));
        var method = Method.fromText(getTextSafe(cols.get(indices.getMethodIndex())));
        var round = Round.fromText(getTextSafe(cols.get(indices.getRoundIndex())));
        var finalRoundEndTime = parseFightTime(getTextSafe(cols.get(indices.getTimeIndex())));

        var weightClassCol = cols.get(indices.getWeightClassIndex());
        boolean isTitleFight = !weightClassCol.select(Selectors.BELT_IMAGE).isEmpty();

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
        return col.select(Selectors.FIGHTER_PARAGRAPHS).stream()
            .map(Element::text)
            .map(String::trim)
            .map(text -> {
                try {
                    return Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    return null;
                }
            })
            .toList();
    }

    private static String getTextSafe(Element td) {
        return Optional.ofNullable(td.selectFirst(Selectors.FIGHTER_PARAGRAPHS))
            .map(Element::text)
            .map(String::trim)
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
