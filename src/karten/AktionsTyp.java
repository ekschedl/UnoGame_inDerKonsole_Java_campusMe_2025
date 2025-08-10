package karten;

public enum AktionsTyp {
    PLUS_ZWEI("+2"),
    AUSSETZEN("AUSSETZEN"),
    RICHTUNG("RICHTUNGSWECHSEL");

   private final String displayName;

    AktionsTyp(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
