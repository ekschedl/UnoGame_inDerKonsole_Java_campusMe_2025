package karten;

/*
Diese Klasse repräsentiert Aktionskarten (z.B. „+2“, „Aussetzen“, „Richtungswechsel“).
Diese Karten dürfen nur gelegt werden,
→ wenn sie dieselbe Farbe haben wie die Karte darunter
ODER
→ wenn sie dieselbe Aktion haben (z.B. zwei „+2“-Karten nacheinander)
*/

public class Aktionskarte extends Karte {
    private AktionsTyp aktion;  // z.B. PLUS_ZWEI, AUSSETZEN, RICHTUNG

    // Konstruktor: Farbe und Aktions-Typ werden gesetzt
    public Aktionskarte(Farbe farbe, AktionsTyp aktion) {
        super(farbe);
        this.aktion = aktion;
    }

    // Getter für die Aktion
    public AktionsTyp getAktion() {
        return aktion;
    }

    // Ausgabe der Karte im farbigen Format: z. B. "ROT +2"
    @Override
    public String anzeigen() {
        return farbe.farbig(farbe.getDisplayName() + " " + aktion.getDisplayName());
    }

    // Regelprüfung: Darf diese Karte auf eine andere gelegt werden?
    @Override
    public boolean istLegbarAuf(Karte andere) {
        // Wenn andere Karte auch eine Aktionskarte ist:
        if (andere instanceof Aktionskarte) {
            Aktionskarte a = (Aktionskarte) andere;
            // Legen erlaubt, wenn Farbe ODER Aktion gleich ist
            return this.farbe.equals(a.farbe) || this.aktion == a.aktion;
        }

        // Wenn andere Karte KEINE Aktionskarte ist:
        // Nur Farbe zählt
        return this.farbe.equals(andere.getFarbe());
    }
}