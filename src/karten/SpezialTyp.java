package karten;

public enum SpezialTyp { //val, 09.06
    PLUS_VIER("+4 FARBWAHL"),
    FARBWAHL("FARBWAHL");


    private final String displayName;

    SpezialTyp(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}





