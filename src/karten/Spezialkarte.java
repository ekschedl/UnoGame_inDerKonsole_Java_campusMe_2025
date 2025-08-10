package karten;



public class Spezialkarte extends Karte {
    private SpezialTyp spezialAktion; // +4 oder Farbwahl

    // Konstruktor: Spezialkarten sind immer SCHWARZ (neutral)
    public Spezialkarte(SpezialTyp spezialAktion) {
        super(Farbe.SCHWARZ);
        this.spezialAktion = spezialAktion;
    }

    // Spieler wählt eine Farbe – diese wird in der Karte gespeichert,
    public void farbeWaehlen(Farbe farbe) {
        this.farbe = farbe;
    }

    // Wenn Karte wieder in den Stapel kommt (zb. beim Mischen), wird Farbe zurückgesetzt
    public void farbeZuruecksetzen() {
        this.farbe = Farbe.SCHWARZ;
    }

    // gibt Darstellung der Karte zurück:
    // wenn noch keine Farbe gewählt → zeige neutrale Aktion (zb. „+4“ in weiß),
    //  wenn Farbe gewählt → zeige gewählte Farbe (zb. „ROT“)
    @Override
    public String anzeigen() {
        if (farbe == Farbe.SCHWARZ) {
            return Farbe.SCHWARZ.farbig(spezialAktion.getDisplayName());
        }
        else {
            return farbe.farbig(farbe.getDisplayName());
        }
    }

    public SpezialTyp getSpezialAktion() {
        return spezialAktion;
    }

    // Spezialkarten dürfen *immer* gelegt werden (zB. Farbwahl +4).
    // Aber im Spiel muss geprüft werden, ob Spieler wirklich keine andere Karte legen könnte-schummeln!
    @Override
    public boolean istLegbarAuf(Karte andere) {
        return true;
    }
}

