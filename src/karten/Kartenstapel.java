package karten;
/*
 * Klasse verwaltet Ziehstapel und Ablagestapel
 * - erzeugt alle Karten (Zahlen, Aktionskarten, Spezialkarten)
 * - mischt die Karten
 * -  Methoden zum Ziehen und Ablegen von Karten
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Kartenstapel {
    private List<Karte> ziehstapel = new ArrayList<>();
    private List<Karte> ablagestapel = new ArrayList<>();
    private boolean kartenErstellt = false;                   // Schutzvariable, damit erstelleAlleKarten() nur einmal aufgerufen wird

    public Kartenstapel() {
        erstelleAlleKarten();
        mischen();
    }

    // prüft, ob Karten schon erstellt wurden – wenn ja, kein erneutes Erzeugen
    private void erstelleAlleKarten() {
        if (kartenErstellt) {
            System.out.println("❗ Karten wurden bereits erstellt – erneutes Erstellen verhindert.");
            return;
        }

        // Schleife für jede Farbe (außer SCHWARZ)
        for (Farbe farbe : Farbe.values()) {
            if (farbe != Farbe.SCHWARZ) {
                // jede Farbe bekommt eine 0
                ziehstapel.add(new Zahlenkarte(farbe, 0));
                // jede Zahl von 1–9 wird 2× pro Farbe hinzugefügt
                for (int i = 1; i <= 9; i++) {
                    ziehstapel.add(new Zahlenkarte(farbe, i));
                    ziehstapel.add(new Zahlenkarte(farbe, i));
                }

                // je 2× Aktionskarten pro Farbe
                for (int i = 0; i < 2; i++) {
                    ziehstapel.add(new Aktionskarte(farbe, AktionsTyp.PLUS_ZWEI));
                    ziehstapel.add(new Aktionskarte(farbe, AktionsTyp.AUSSETZEN));
                    ziehstapel.add(new Aktionskarte(farbe, AktionsTyp.RICHTUNG));
                }
            }
        }

        // 4x +4 und 4x Farbwahl (immer SCHWARZ, muss nicht extra übergeben werden)
        for (int i = 0; i < 4; i++) {
            ziehstapel.add(new Spezialkarte(SpezialTyp.PLUS_VIER));
            ziehstapel.add(new Spezialkarte(SpezialTyp.FARBWAHL));
        }

        kartenErstellt = true; // verhindert erneutes Erstellen
    }

    // mischt den Ziehstapel
    public void mischen() {
        Collections.shuffle(ziehstapel);
    }

    // zieht die oberste Karte (letztes Element in der ArrayList)
    public Karte zieheKarte() {
        if (ziehstapel.isEmpty()) {
            mischenVonAblagestapel(); // wenn leer, Ablagestapel mischen
        }

        return ziehstapel.remove(ziehstapel.size() - 1); // letzte Karte zurückgeben und entfernen
    }

    // legt eine Karte auf den Ablagestapel
    public void ablegen(Karte karte) {
        // Spezialkarten merken sich Farbwahl – diese wird zurückgesetzt,
        // sobald eine neue Karte abgelegt wird (nur für alte Ablage!)
        if (zeigeObereAblage() instanceof Spezialkarte sk) {
            sk.farbeZuruecksetzen();
        }

        ablagestapel.add(karte); // neue Karte oben auf Ablagestapel
    }

    // 07.07 sche – mischt den Ablagestapel in den Ziehstapel, falls nötig
    private void mischenVonAblagestapel() {
        // Wenn zu wenige Karten zum Mischen da sind (zb. nur die oberste), Spielabbruch
        if (ablagestapel.size() <= 1) {
            System.out.println("❌ Nicht genug Karten zum Mischen – das Spiel wird beendet. Bitte das Programm neu starten.");
            return;
        }

        // oberste Karte behalten (muss oben auf Ablagestapel bleiben)
        int letzterIndex = ablagestapel.size() - 1;
        Karte obersteKarte = ablagestapel.get(letzterIndex);

        // alle anderen Karten in den Ziehstapel übertragen
        for (int i = 0; i < letzterIndex; i++) {
            ziehstapel.add(ablagestapel.get(i));
        }

        // Ablagestapel leeren und oberste Karte wieder hinzufügen
        ablagestapel.clear();
        ablagestapel.add(obersteKarte);

        // neu mischen
        Collections.shuffle(ziehstapel);

        System.out.println("\007🔄 Ziehstapel neu gemischt! (" + ziehstapel.size() + " Karten)");
    }

    // Gibt das letzte Element der Liste zurück, also das, was zuletzt per ablagestapel.add(karte) oben draufgelegt wurde.
    public Karte zeigeObereAblage() {
        if (!ablagestapel.isEmpty()) {
            return ablagestapel.get(ablagestapel.size() - 1);
        }
        return null;
    }

    // Testausgabe aller Karten im Ziehstapel
    public void ziehstapelAnzeigen() {
        System.out.println("Alle Karten im Ziehstapel:");
        for (Karte karte : ziehstapel) {
            System.out.println(karte);
        }
    }

    // Testausgabe aller Karten im Ablagestapel
    public void ablagestapelAnzeigen() {
        System.out.println("Alle Karten im Ablagestapel:");
        for (Karte karte : ablagestapel) {
            System.out.println(karte);
        }
    }

    // Getter für direkten Zugriff
    public List<Karte> getZiehstapel() {
        return ziehstapel;
    }

    public List<Karte> getAblagestapel() {
        return ablagestapel;
    }
}