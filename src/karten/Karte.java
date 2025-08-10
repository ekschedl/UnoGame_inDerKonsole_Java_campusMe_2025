package karten;

public abstract class Karte {
    protected Farbe farbe;

    public Karte(Farbe farbe) {
        this.farbe = farbe;
    }

    public Farbe getFarbe() {
        return farbe;
    }

    public abstract String anzeigen();

    //Standardregel :
    // Eine karten.Karte ist spielbar, wenn die karten.Farbe gleich ist.
    //Diese Logik gilt für viele Kartenarten (zB. Zahl, Spezial)
	//Spezialisierte Karten (wie Zahlenkarten oder +4) können diese Methode mit @Override anpassen
    public boolean istLegbarAuf(Karte andere){
        return this.farbe == andere.getFarbe();

    }

    //für Debuggen und schnelle Tests, 06.06 sche
    @Override
    public String toString() {
        return anzeigen();
    }
}
