package spiel;

public class Einstellungen {
    // Einige Grundeinstellungen für den Spielablauf. Die Werte entsprechen den offiziellen Uno-Regeln.
    // Können wir zeitweise ändern, um das Testen zu vereinfachen: z.B. ANZAHL_KARTEN_PRO_SPIELER auf 3 reduzieren, damit wir
    // kürzere Spielrunden habe
    public static final int ANZAHL_SPIELER = 4;
    public static final int ANZAHL_KARTEN_PRO_SPIELER = 7;
    public static final int SIEG_PUNKTE = 500;

    // Wartepause des Bots nach seinem Zug. Wenn ein Spiel nur mit Bots gestartet wird, können wir den Spielablauf deutlich
    // beschleunigen, indem wir den Wert z.B. auf 10 setzen.
    public static final int BOT_PAUSE = 1000;
}
