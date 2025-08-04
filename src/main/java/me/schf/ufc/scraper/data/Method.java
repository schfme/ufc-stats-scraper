package me.schf.ufc.scraper.data;

public enum Method {

    OTHER("Other"),
    KO_TKO("KO/TKO"),
    SPLIT_DECISION("S-DEC"),
    OVERTURNED("Overturned"),
    DISQUALIFICATION("DQ"),
    SUBMISSION("SUB"),
    UNANIMOUS_DECISION("U-DEC"),
    MAJORITY_DECISION("M-DEC"),
    NO_CONTEST("CNC");

    private final String text;

    Method(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static Method fromText(String text) {
        for (Method method : values()) {
            if (method.text.equalsIgnoreCase(text)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown outcome: " + text);
    }
}