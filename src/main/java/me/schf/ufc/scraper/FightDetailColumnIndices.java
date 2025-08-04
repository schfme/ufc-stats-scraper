package me.schf.ufc.scraper;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FightDetailColumnIndices {

    public enum Column {
        WL("W/L"),
        FIGHTER("Fighter"),
        KD("Kd"),
        STR("Str"),
        TD("Td"),
        SUB("Sub"),
        WEIGHT_CLASS("Weight class"),
        METHOD("Method"),
        ROUND("Round"),
        TIME("Time");

        private final String name;

        Column(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Column fromName(String name) {
            for (var col : values()) {
                if (col.name.equals(name)) {
                    return col;
                }
            }
            throw new IllegalArgumentException("Unknown column name: " + name);
        }
    }

    private static final Set<String> REQUIRED_COLUMNS = Set.of(
        "W/L", "Fighter", "Kd", "Str", "Td", "Sub", "Weight class", "Method", "Round", "Time"
    );

    private final Map<Column, Integer> indices = new EnumMap<>(Column.class);

    public FightDetailColumnIndices(List<String> columnNames) {
        for (int i = 0; i < columnNames.size(); i++) {
            String name = columnNames.get(i);
            try {
                Column col = Column.fromName(name);
                indices.put(col, i);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Unknown column: " + name, e);
            }
        }

        // check required columns
        List<String> missing = REQUIRED_COLUMNS.stream()
                .filter(r -> !columnNames.contains(r))
                .toList();
        
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required columns: " + missing);
        }
    }

    public int getIndex(Column column) {
        return indices.get(column);
    }

    public int getWlIndex() { return getIndex(Column.WL); }
    public int getFighterIndex() { return getIndex(Column.FIGHTER); }
    public int getKdIndex() { return getIndex(Column.KD); }
    public int getStrIndex() { return getIndex(Column.STR); }
    public int getTdIndex() { return getIndex(Column.TD); }
    public int getSubIndex() { return getIndex(Column.SUB); }
    public int getWeightClassIndex() { return getIndex(Column.WEIGHT_CLASS); }
    public int getMethodIndex() { return getIndex(Column.METHOD); }
    public int getRoundIndex() { return getIndex(Column.ROUND); }
    public int getTimeIndex() { return getIndex(Column.TIME); }
}
