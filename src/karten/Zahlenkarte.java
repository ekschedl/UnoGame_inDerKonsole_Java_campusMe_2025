package karten;

/*
Diese Klasse repräsentiert normale Zahlenkarten (zB. „ROT 5“, „BLAU 2“).
Zahlenkarten dürfen gelegt werden, wenn:
→ die Farbe gleich ist ODER
→ die Zahl gleich ist
*/
public class Zahlenkarte extends Karte {
    private int zahl;

    public Zahlenkarte(Farbe farbe, int zahl) {
        super(farbe);
        this.zahl = zahl;
    }

    public int getZahl() {
        return zahl;
    }

    // Anzeige im Format: "FARBE ZAHL" (z.bB. "ROT 5"), farbig dargestellt
    @Override
    public String anzeigen() {
        return farbe.farbig(farbe.getDisplayName() + " " + zahl);
    }

    // Regel: Karte darf gelegt werden, wenn...
    @Override
    public boolean istLegbarAuf(Karte andere) {
        // Wenn andere Karte auch eine Zahlenkarte ist:
        if (andere instanceof Zahlenkarte) {
            Zahlenkarte z = (Zahlenkarte) andere;
            // Legen erlaubt, wenn entweder Farbe oder Zahl gleich ist
            return this.farbe.equals(z.farbe) || this.zahl == z.zahl;
        }

        // Wenn andere Karte KEINE Zahlenkarte ist:
        // Nur die Farbe zählt
        return this.farbe.equals(andere.getFarbe());
    }
}