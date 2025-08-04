package me.schf.ufc.scraper.data;

public enum WeightClass {

    CATCH_WEIGHT("Catch Weight"),
    LIGHT_HEAVYWEIGHT("Light Heavyweight"),
    WOMENS_BANTAMWEIGHT("Women's Bantamweight"),
    FLYWEIGHT("Flyweight"),
    WOMENS_STRAWWEIGHT("Women's Strawweight"),
    SUPER_HEAVYWEIGHT("Super Heavyweight"),
    WOMENS_FLYWEIGHT("Women's Flyweight"),
    WOMENS_FEATHERWEIGHT("Women's Featherweight"),
    HEAVYWEIGHT("Heavyweight"),
    LIGHTWEIGHT("Lightweight"),
    FEATHERWEIGHT("Featherweight"),
    WELTERWEIGHT("Welterweight"),
    MIDDLEWEIGHT("Middleweight"),
    BANTAMWEIGHT("Bantamweight"),
    OPEN_WEIGHT("Open Weight");

    private final String text;

    WeightClass(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static WeightClass fromText(String text) {
        for (WeightClass wc : values()) {
            if (wc.text.equalsIgnoreCase(text)) {
                return wc;
            }
        }
        throw new IllegalArgumentException("Unknown weight class: " + text);
    }
}