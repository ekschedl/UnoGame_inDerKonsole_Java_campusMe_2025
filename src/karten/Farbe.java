package karten;

public enum Farbe {
        ROT("\u001B[31m", "ROT"),     // Rot
        GELB("\u001B[93m", "GELB"),
        GRUEN("\u001B[32m", "GRÜN"),   // Grün
        BLAU("\u001B[34m", "BLAU"),    // Blau
        SCHWARZ("\u001B[37m", "SCHWARZ"); // Weiß statt Schwarz

        private final String code;
        private final String displayName;

        Farbe(String code, String displayName) {
                this.code = code;
                this.displayName = displayName;
        }

        public String farbig(String text) {
                return code + text + "\u001B[0m"; // Reset-karten.Farbe am Ende
        }


        public String getDisplayName() {
                return displayName;
        }
}
