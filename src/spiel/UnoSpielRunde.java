package spiel;

import karten.*;
import spieler.Spieler;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Scanner;

/**
 * UnoSpielRunde steuert den Ablauf einer einzelnen UNO-Runde mit Mensch und Bot.
 * <p>
 * Aufgaben:
 * - Mischen und Austeilen der Karten
 * - Auswählen und Auswerten der Startkarte
 * - Durchführen der Spielzüge aller Spieler (Bots oder Menschen)
 * - Beenden der Runde bei Sieg und Punktevergabe
 * <p>
 * Hauptmethoden:
 * - starten(): startet eine vollständige Spielrunde (Austeilen, Startkarte, Zugfolge)
 * - spielDurchlauf(): zentrale Spielschleife mit Spielerwechsel und Spielende
 * - botZug(): Bot entscheidet automatisch, welche Karte er legt oder zieht
 * - spielerZug(): zeigt Menü für menschlichen Spieler (legen, ziehen, UNO, prüfen...)
 * - karteSpielen(): legt Karte, wenn erlaubt
 * - kartenEffektAnwenden(): prüft, ob Sonderkarte (z.B. Richtungswechsel, +2) gespielt wurde
 */
public class UnoSpielRunde {
    private final Scanner scanner;
    private final Kartenstapel stapel;
    private final List<Spieler> spielerListe;
    private int aktuellerIndex = 0; // Index des Spielers, der gerade am Zug ist
    private Spieler letzterSpieler; // Zuletzt aktiver Spieler – für UNO-Kontrolle
    private boolean spielrichtungUhrzeigersinn = true; // Richtung im Uhrzeigersinn
    private boolean letzteKarteWarAussetzen = false;  // Zur Verwaltung von Aussetzen-Effekten

    // Konstruktor: erhält die aktuelle Spielerreihenfolge + Scanner
    public UnoSpielRunde(Scanner scanner, List<Spieler> spielerListe) {
        this.scanner = scanner;
        this.spielerListe = spielerListe;
        this.stapel = new Kartenstapel();
    }

    public Kartenstapel getStapel() {
        return stapel;
    }

    /**
     * Diese Methode startet eine vollständige Spielrunde.
     * Ruft: Karten austeilen, Startkarte legen, Startkarte auswerten, Spielverlauf durchführen.
     * Gibt zurück: den Gewinner der Runde (wer als erster alle Karten loswurde).
     */
    public Spieler starten() {
        kartenAusteilen();
        startkarteLegen();
        checkEffektDerStartkarte();
        return spielDurchlauf();
    }

    // Teilt jedem Spieler ANZAHL_KARTEN_PRO_SPIELER Karten vom Stapel aus
    private void kartenAusteilen() {
        for (Spieler spieler : spielerListe) {
            for (int j = 0; j < Einstellungen.ANZAHL_KARTEN_PRO_SPIELER; j++) {
                spieler.karteZiehen(stapel.zieheKarte());
            }
        }
    }

    // Legt die oberste Karte vom Stapel als Startkarte auf den Ablagestapel
    // → Spezialfall: "+4" darf nicht Startkarte sein
    private void startkarteLegen() {
        Karte startkarte = stapel.zieheKarte();
        while (startkarte instanceof Spezialkarte && ((Spezialkarte) startkarte).getSpezialAktion() == SpezialTyp.PLUS_VIER) {
            startkarte = stapel.zieheKarte(); // solange neue ziehen, bis gültige Startkarte
        }
        stapel.ablegen(startkarte);
        System.out.println("\n\u001B[1m\u001B[35m🚩 Startkarte: \u001B[0m" + startkarte.anzeigen());
    }

    // Prüft, ob die Startkarte einen Effekt auslöst (z.B. +2, Richtungswechsel, Farbwunsch)
    public void checkEffektDerStartkarte() {
        Karte startkarte = getAktuelleAblage();
        Spieler aktuellerSpieler = getAktuellerSpieler();
        aktuellerSpieler.setUnoGerufen(false);// UNO-Status zurücksetzen (neue Runde)

        if (startkarte instanceof Aktionskarte aktion) {
            switch (aktion.getAktion()) {
                case AUSSETZEN -> {
                    rundenHeaderAnzeigen();
                    spielerUeberspringen(aktuellerSpieler);
                }
                case RICHTUNG -> {
                    System.out.println("🔁 Startkarte ist RICHTUNGSWECHSEL – Spielrichtung wird umgedreht!");
                    spielrichtungUhrzeigersinn = !spielrichtungUhrzeigersinn;
                    // nach Richtungswechsel direkt zum nächsten Spieler in neuer Richtung springen
                    aktuellerIndex = berechneNaechstenIndex(1); // nächsten Spieler ermitteln
                }
                case PLUS_ZWEI -> {
                    rundenHeaderAnzeigen();
                    strafkartenZiehen(aktuellerSpieler, 2);
                    spielerUeberspringen(aktuellerSpieler);
                }
            }
            // Sonderbehandlung für Farbwahlkarte
        } else if (startkarte instanceof Spezialkarte sk && sk.getSpezialAktion() == SpezialTyp.FARBWAHL) {
            // Regel zu Farbenwahlkarte bei Spielstart:
            // > Wenn eine Farbenwahl Karte zu Beginn des Spiels gezogen wird, entscheidet der Spieler zur Linken
            // > des Gebers, welche Farbe als nächstes gelegt werden soll.
            // Diese Regel ist nicht 1:1 anwendbar, da es im Computerspiel keinen "Geber" gibt.
            // Daher interpretieren wir die Regel so: Die erste Spielerin darf die Farbe auswählen, aber keine legen; die
            // nächste Spielerin darf als erste eine Karte auf die Farbwahlkarte legen.

            Spieler farbeWaehlenderSpieler = getAktuellerSpieler();
            rundenHeaderAnzeigen(farbeWaehlenderSpieler);
            farbeWaehlenderSpieler.handAnzeigen();
            farbeWaehlen(sk, farbeWaehlenderSpieler);

            aktuellerIndex = berechneNaechstenIndex(1); // Nächster Spieler kommt dran
        }

        if (letzteKarteWarAussetzen) {
            aktuellerIndex = berechneNaechstenIndex(1);
            letzteKarteWarAussetzen = false;
        }
    }

    /**
     * Diese Methode steuert den gesamten Ablauf der Runde:
     * Spielerwechsel, Spielzüge, Runde beenden, Punkte vergeben.
     * Gibt zurück: den Spieler, der alle Karten losgeworden ist.
     */
    private Spieler spielDurchlauf() {
        boolean spielLaeuft = true;

        while (spielLaeuft) {
            Spieler aktuellerSpieler = getAktuellerSpieler();

            rundenHeaderAnzeigen();

            if (aktuellerSpieler.istBot()) {
                botZug(); // Bot spielt automatisch
            } else {
                spielerZug(); // Menschlicher Spieler bekommt Menü
            }

            if (aktuellerSpieler.getHand().isEmpty()) {
                rundeGewonnen(aktuellerSpieler); // Punkte berechnen
                return aktuellerSpieler;         // Runde gewonnen
            }

            letzterSpieler = aktuellerSpieler;

            // Spielerwechsel berechnen (1 normal, 2 bei Aussetzen)
            int schritte = letzteKarteWarAussetzen ? 2 : 1;
            aktuellerIndex = berechneNaechstenIndex(schritte); // Spielerwechsel berechnen
            letzteKarteWarAussetzen = false;
        }

        return null;// theoretisch nie erreicht
    }

    // Gibt einfachen Aufruf weiter an die überladene Methode mit Spielerparameter
    private void rundenHeaderAnzeigen() {
        rundenHeaderAnzeigen(getAktuellerSpieler());
    }

    // Zeigt Rundenkopf mit Name und Kartenanzahl an – für Übersicht im Spielverlauf
    private void rundenHeaderAnzeigen(Spieler spieler) {
        System.out.println();
        System.out.println("============== NÄCHSTER SPIELER ==============");
        if (letzterSpieler != null) {
            // Info über vorherigen Spieler und wie viele Karten er noch hat
            System.out.println("Vorheriger Spieler " + letzterSpieler.getName() + " hat " + letzterSpieler.getHand().size() + " Karte(n) in der Hand");
        }
        System.out.println("\u001B[35m\uD83C\uDFB2 Spieler: " + "\u001B[0m" + spieler.getName());
    }

    // Gibt die oberste Karte vom Ablagestapel zurück – z. B. zum Vergleich bei Spielzügen
    private Karte getAktuelleAblage() {
        return stapel.zeigeObereAblage();
    }

    // Liefert den Spieler, der gerade an der Reihe ist
    public Spieler getAktuellerSpieler() {
        return spielerListe.get(aktuellerIndex);
    }

    // Berechnet den nächsten Spieler, der nach dem aktuellen dran ist
    private Spieler getNaechsterSpieler() {
        int index = berechneNaechstenIndex(1);
        return spielerListe.get(index);
    }

    // ================= BOT-ZUG =================
// Führt den Spielzug eines Bots aus – inklusive Ziehen, Legen, UNO-Rufen
    private void botZug() {
        Spieler aktuellerSpieler = getAktuellerSpieler();
        Karte passende = findePassendeKarte(aktuellerSpieler.getHand());

        if (passende == null) {
            // Wenn Bot keine passende Karte hat → eine ziehen
            Karte gezogene = stapel.zieheKarte();

            if (istAblegbar(gezogene)) {
                // Falls die gezogene passt → sofort ablegen
                stapel.ablegen(gezogene);
                System.out.println("hat gezogen und gelegt: " + gezogene.anzeigen());
                kartenEffektAnwenden(gezogene); // zB. +2, Richtungswechsel
            } else {
                // Karte passt nicht → zur Hand hinzufügen
                aktuellerSpieler.karteZiehen(gezogene);
                System.out.println("hat gezogen: " + gezogene.anzeigen());
            }
        } else {
            // Passende Karte auf der Hand → direkt spielen
            stapel.ablegen(passende);
            aktuellerSpieler.karteSpielen(passende);
            System.out.println("hat gespielt: " + passende.anzeigen());
            kartenEffektAnwenden(passende);
        }

        // Bot ruft automatisch UNO, sobald er nur noch 1 Karte hat
        if (aktuellerSpieler.getHand().size() == 1) {
            unoRufen(aktuellerSpieler);
        }

        // Kurze Pause (Thread.sleep), damit Spielzug nicht zu abrupt ist
        try {
            Thread.sleep(Einstellungen.BOT_PAUSE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Durchsucht die Hand des Bots nach erster passender Karte zur Ablage
    public Karte findePassendeKarte(List<Karte> handWithCards) {
        for (Karte karte : handWithCards) {
            if (istAblegbar(karte)) {
                return karte;
            }
        }
        return null; // Falls keine passende Karte vorhanden
    }

    // ================= SPIELER-ZUG =================
// Führt den Spielzug eines Menschen durch – mit Auswahlmenü
    private void spielerZug() {
        boolean aktionGemacht = false; // Flag, um Wiederholung zu steuern

        while (!aktionGemacht) {
            Spieler spieler = getAktuellerSpieler();

            // Aktuelle Ablagekarte anzeigen
            System.out.println("📥 \u001B[35mAblagestapel: " + getAktuelleAblage().anzeigen());
            spieler.handAnzeigen(); // Spieler sieht seine Handkarten

            // Auswahlmenü anzeigen
            System.out.println("\u001B[35m\n🎮 Was möchtest du tun?:\u001B[0m");
            System.out.println("\u001B[35m[Nr]\u001B[0m Karte spielen / " + "\u001B[35m[z]\u001B[0m Karte ziehen / " + "\u001B[35m[uno]\u001B[0m UNO! rufen / " + "\u001B[35m[k]\u001B[0m UNO! kontrollieren / " + Farbe.GELB.farbig("[h]") + " Hilfe / " + Farbe.ROT.farbig("[x]") + " Spiel beenden");
            System.out.print("Bitte Zahl oder Buchstabe eingeben: ");
            String eingabe = scanner.nextLine().trim();

            // Auswahl auswerten
            switch (eingabe) {
                case "z" -> {
                    karteZiehen();      // Spieler zieht eine Karte
                    aktionGemacht = true;
                }
                case "uno" -> unoRufen(spieler);   // UNO! rufen
                case "k" -> unoKontrollieren();    // UNO-Kontrolle durchführen
                case "h" -> hilfeAnzeigen();       // Hilfe-Seite öffnen
                case "x" -> programmBeenden();     // Spiel beenden
                default -> aktionGemacht = karteSpielen(eingabe); // Karte spielen versuchen
            }

            System.out.println(); // Leerzeile für Übersicht
        }
    }

    // ================= KARTE SPIELEN =================
// Spieler versucht, per Zahl eine Karte zu spielen – Rückgabe: ob erfolgreich
    private boolean karteSpielen(String eingabe) {
        Spieler aktuellerSpieler = getAktuellerSpieler();
        int index;

        try {
            index = Integer.parseInt(eingabe) - 1; // Umwandlung in Listen-Index (1-basiert zu 0-basiert)
        } catch (NumberFormatException e) {
            // Eingabe war keine Zahl
            System.out.println("❌ Ungültige Eingabe. Gültige Werte sind [z], [uno], [k], [h], [x] sowie Zahlen zwischen 1 und " + aktuellerSpieler.getHand().size() + "!");
            return false;
        }

        if (index < 0 || index >= aktuellerSpieler.getHand().size()) {
            // Zahl außerhalb des gültigen Bereichs
            System.out.println("❌ Ungültige Kartennummer. Gültige Werte sind [z], [uno], [k], [h], [x] sowie Zahlen zwischen 1 und " + aktuellerSpieler.getHand().size() + "!");
            return false;
        }

        Karte gewählte = aktuellerSpieler.getHand().get(index); // Gewählte Karte holen

        if (!istAblegbar(gewählte)) {
            // Karte passt nicht auf die aktuelle Ablage
            System.out.println("❌ Diese Karte passt nicht.");
            return false;
        }

        // Karte wird abgelegt und aus Hand entfernt
        stapel.ablegen(gewählte);
        aktuellerSpieler.karteSpielen(gewählte);
        System.out.println("✅ gelegt: " + gewählte.anzeigen());

        // Eventuelle Effekte anwenden (+2, Richtungswechsel, Spezialeffekte)
        kartenEffektAnwenden(gewählte);

        return true; // Spielzug war gültig und erfolgreich
    }

    private boolean istAblegbar(Karte karte) {
        return karte.istLegbarAuf(getAktuelleAblage());
    }

    // ================= EFFEKT-KARTEN ANWENDEN =================
// Diese Methode prüft, ob die gespielte Karte einen Effekt hat (zB. Richtungswechsel, +2, +4, Farbwahl)
// und führt entsprechende Aktionen aus
    private void kartenEffektAnwenden(Karte karte) {
        if (karte instanceof Aktionskarte ak) {
            // Effektkarte wie Richtungswechsel, Aussetzen oder +2
            switch (ak.getAktion()) {
                case RICHTUNG -> {
                    // Dreht die Spielrichtung (Uhrzeigersinn <-> gegen den Uhrzeigersinn)
                    spielrichtungUhrzeigersinn = !spielrichtungUhrzeigersinn;
                    System.out.println("🔁 Spielrichtung wurde gewechselt durch Spieler!");
                }
                case AUSSETZEN -> {
                    // Nächster Spieler wird übersprungen
                    spielerUeberspringen(getNaechsterSpieler());
                }
                case PLUS_ZWEI -> {
                    // Nächster Spieler muss 2 Karten ziehen und wird übersprungen
                    Spieler naechsterSpieler = getNaechsterSpieler();
                    rundenHeaderAnzeigen(naechsterSpieler);
                    strafkartenZiehen(naechsterSpieler, 2);
                    spielerUeberspringen(naechsterSpieler);
                }
            }
        } else if (karte instanceof Spezialkarte sk) {
            // Spezialkarten wie Farbwahl oder Farbwahl +4
            switch (sk.getSpezialAktion()) {
                case FARBWAHL -> {
                    // Spieler (oder Bot) wählt eine neue Farbe
                    farbeWaehlen(sk, getAktuellerSpieler());
                }
                case PLUS_VIER -> {
                    // Farbe wird gewählt, dann muss der nächste Spieler 4 Karten ziehen (evtl. Schummelprüfung)
                    farbeWaehlen(sk, getAktuellerSpieler());

                    Spieler opfer = getNaechsterSpieler();
                    rundenHeaderAnzeigen(opfer);

                    if (opfer.istBot()) {
                        // Bot zieht automatisch 4 Karten und wird übersprungen
                        strafkartenZiehen(opfer, 4);
                        spielerUeberspringen(opfer);
                    } else {
                        // Menschlicher Spieler bekommt die Möglichkeit, auf Schummeln zu prüfen
                        schummelnAbfragen(opfer);
                    }
                }
            }
        }
    }


    // ================= SPIELER ÜBERSPRINGEN =================
// Wird aufgerufen, wenn ein Spieler durch eine Karte (z.B. „Aussetzen“ oder „+2“) seinen Zug verliert
    private void spielerUeberspringen(Spieler spieler) {
        System.out.println("⏭️ Spieler " + spieler.getName() + " darf keine Karte ablegen und wird übersprungen.");
        letzteKarteWarAussetzen = true; // für mögliche spätere Prüfungen
    }

    // ================= SCHUMMELN PRÜFEN BEI +4 =================
    // Wird aufgerufen, wenn ein Spieler eine +4-Karte gespielt hat und der nächste Spieler Schummeln vermutet
    private void schummelnAbfragen(Spieler spieler) {
        System.out.println();
        System.out.println("Die Karte Farbwahl +4 wurde gelegt! Möchtest du auf Schummeln prüfen? (\u001B[35mj/n\u001B[0m)");

        String eingabe = scanner.nextLine().trim().toLowerCase();

        if (eingabe.equals("j")) {
            // Wenn der aktuelle Spieler eine Karte in seiner Hand hatte, die zur vorherigen Ablagefarbe gepasst hätte,
            // gilt das als Schummeln
            if (getWurdeGeschummelt()) {
                Spieler schummler = getAktuellerSpieler();
                System.out.println("Schummeln aufgedeckt! Spieler " + schummler.getName() + " muss 4 Karten ziehen.");
                strafkartenZiehen(schummler, 4);
            } else {
                System.out.println("Es wurde nicht geschummelt! Du musst 6 statt 4 Karten ziehen.");
                strafkartenZiehen(spieler, 6);
                spielerUeberspringen(spieler);
            }
        } else {
            // Spieler prüft nicht – muss normal 4 Karten ziehen
            strafkartenZiehen(spieler, 4);
            spielerUeberspringen(spieler);
        }
    }

    // Gibt true zurück, wenn der aktuelle Spieler eigentlich eine Karte in der Farbe der vorherigen Ablage hatte,
    // also die +4-Karte nicht nötig gewesen wäre → Schummeln!
    private boolean getWurdeGeschummelt() {
        Karte aktuelleAblage = getAktuelleAblage();
        if (aktuelleAblage == null || !(aktuelleAblage instanceof Spezialkarte sk && sk.getSpezialAktion() == SpezialTyp.PLUS_VIER)) {
            return false;
        }

        List<Karte> karten = stapel.getAblagestapel();

        if (karten.size() < 2) {
            return false;
        }

        Karte vorherigeAblage = karten.get(karten.size() - 2);

        for (Karte handKarte : getAktuellerSpieler().getHand()) {
            if (vorherigeAblage.getFarbe() == handKarte.getFarbe()) {
                return true; // Schummeln
            }
        }

        return false; // kein Schummeln
    }

    // Wird aufgerufen, wenn ein Spieler „UNO!“ ruft – entweder manuell oder automatisch (Bot)
    private void unoRufen(Spieler spieler) {
        Sound.playSound("UNO.wav"); // Ton für alle hörbar

        spieler.setUnoGerufen(true); // Spieler hat UNO gerufen
        System.out.println("🔊 Du hast hat UNO gerufen!");
    }

    // Wird von einem anderen Spieler ausgelöst, um zu prüfen, ob der vorherige Spieler UNO gesagt hat
    private void unoKontrollieren() {
        if (letzterSpieler == null) {
            return;
        } else if (letzterSpieler.getHand().size() > 1) {
            System.out.println("Spieler " + letzterSpieler.getName() + " musste nicht UNO rufen.");
        } else if (letzterSpieler.isUnoGerufen()) {
            System.out.println("Spieler " + letzterSpieler.getName() + " hat UNO gerufen.");
        } else {
            // Spieler hat vergessen UNO zu sagen – muss 1 Strafkarte ziehen
            System.out.println("Spieler " + letzterSpieler.getName() + " hat vergessen, Uno zu rufen!");
            strafkartenZiehen(letzterSpieler, 1);
            letzterSpieler.setUnoGerufen(true); // Flag setzen, damit es nicht doppelt bestraft wird
        }
    }

    // ================= FARBE WÄHLEN BEI +4 UND FARBWAHL =================
// Bot wählt zufällig, Mensch wählt durch Eingabe
    private void farbeWaehlen(Spezialkarte sk, Spieler spieler) {
        Farbe[] farben = {Farbe.ROT, Farbe.GELB, Farbe.GRUEN, Farbe.BLAU};

        if (spieler.istBot()) {
            int index = (int) (Math.random() * farben.length);
            Farbe farbe = farben[index];
            sk.farbeWaehlen(farbe);
            System.out.println("✅ Farbe gewählt: " + farbe.farbig(farbe.getDisplayName()));
            return;
        }

        // Menschlicher Spieler wählt manuell
        System.out.println("\n" +
                Farbe.ROT.farbig("●") +
                Farbe.GELB.farbig("●") +
                Farbe.GRUEN.farbig("●") +
                Farbe.BLAU.farbig("●") +
                "  Welche Farbe soll als nächstes gelten?");

        System.out.println(
                "[" + Farbe.ROT.farbig("1") + "] " + Farbe.ROT.farbig("ROT") + " | " +
                        "[" + Farbe.GELB.farbig("2") + "] " + Farbe.GELB.farbig("GELB") + " | " +
                        "[" + Farbe.GRUEN.farbig("3") + "] " + Farbe.GRUEN.farbig("GRÜN") + " | " +
                        "[" + Farbe.BLAU.farbig("4") + "] " + Farbe.BLAU.farbig("BLAU")
        );

        Farbe farbe = null;
        while (farbe == null) {
            System.out.print("Bitte Zahl eingeben: ");
            String eingabe = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(eingabe) - 1;
                if (index >= 0 && index < farben.length) {
                    farbe = farben[index];
                } else {
                    System.out.println("❌ Ungültige Auswahl.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Ungültige Eingabe.");
            }
        }

        sk.farbeWaehlen(farbe);
        System.out.println("✅ gewählt: " + farbe.farbig(farbe.getDisplayName()));
    }

    // Der angegebene Spieler muss n Karten ziehen
    private void strafkartenZiehen(Spieler opfer, int anzahl) {
        // Hinweis, wer betroffen ist
        System.out.println("⚠️ Spieler " + opfer.getName() + " hat " + anzahl + " Karten gezogen.");

        for (int i = 0; i < anzahl; i++) {
            opfer.karteZiehen(stapel.zieheKarte());
        }
    }


    // Wird ausgeführt, wenn ein Spieler "z" eingibt – also eine Karte ziehen möchte
    private void karteZiehen() {
        Spieler aktuellerSpieler = getAktuellerSpieler();

        // Eine neue Karte wird vom Ziehstapel gezogen
        Karte gezogen = stapel.zieheKarte();
        System.out.println("\n💥 gezogen: " + gezogen.anzeigen());

        // Prüfen: Passt die gezogene Karte zur aktuellen Ablage?
        if (istAblegbar(gezogen)) {
            // Spieler darf entscheiden, ob er sie gleich ablegen will
            System.out.print("✅ Die Karte passt. Möchtest du sie ablegen? (\u001B[35mj/n\u001B[0m): ");
            String wahl = scanner.nextLine().trim().toLowerCase();

            if (wahl.equals("j")) {
                stapel.ablegen(gezogen);
                System.out.println("✅ Karte gelegt: " + gezogen.anzeigen());
                kartenEffektAnwenden(gezogen); // Mögliche Effekte ausführen
            } else {
                aktuellerSpieler.karteZiehen(gezogen); // zur Hand hinzufügen
                System.out.println("📥 Karte wurde zu deiner Hand hinzugefügt.");
            }
        } else {
            // Karte passt nicht – wird zur Hand gelegt
            aktuellerSpieler.karteZiehen(gezogen);
            System.out.println("📥 Karte passt nicht – wurde zur Hand hinzugefügt.");
        }
    }

    // Berechnet den Index des nächsten Spielers, abhängig von der Spielrichtung
    private int berechneNaechstenIndex(int schritte) {
        int richtung = spielrichtungUhrzeigersinn ? 1 : -1;
        return (aktuellerIndex + richtung * schritte + spielerListe.size()) % spielerListe.size();
    }

    // Wird aufgerufen, wenn ein Spieler seine letzte Karte abgelegt hat
// Punkte werden anhand der Karten der anderen Spieler berechnet und gutgeschrieben
    private void rundeGewonnen(Spieler aktuellerSpieler) {
        int punkte = 0;

        for (Spieler s : spielerListe) {
            for (Karte k : s.getHand()) {
                if (k instanceof Zahlenkarte zk) {
                    punkte += zk.getZahl();         // Zahlenkarte → Wert entspricht Zahl
                } else if (k instanceof Aktionskarte) {
                    punkte += 20;                   // Aktionskarte (+2, Richtungswechsel...) → 20 Punkte
                } else if (k instanceof Spezialkarte) {
                    punkte += 50;                   // Spezialkarte (+4, Farbwahl) → 50 Punkte
                }
            }
        }

        System.out.println("\n🟥🟩🟦🟨  " + aktuellerSpieler.getName() +
                " hat diese Runde gewonnen: ⭐ " + punkte + " Punkte gutgeschrieben!");

        // Punktestand aktualisieren
        int neuePunkte = aktuellerSpieler.getPunkte() + punkte;
        aktuellerSpieler.setPunkte(neuePunkte);
    }

    // Öffnet eine lokale HTML-Datei mit den Spielregeln im Browser
    // Öffnet eine lokale HTML-Datei mit den Spielregeln im Browser
    private void hilfeAnzeigen() {
        File hilfeDatei = new File("src/uno.html"); // Lokale Datei
        String hilfeWebsite = hilfeDatei.toURI().toString();

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(hilfeWebsite)); // Versuche im Standardbrowser zu öffnen
            } else {
                System.out.println("Die Spielregeln findest du auf " + hilfeWebsite);
            }
        } catch (Exception e) {
            System.out.println("Die Spielregeln findest du auf " + hilfeWebsite); // Bei Fehler: einfach Link zeigen
        }
    }

    // Wird aufgerufen, wenn ein Spieler „x“ eingibt – Spiel wird sofort beendet
    private static void programmBeenden() {
        System.out.println("🚪 Spiel wurde manuell beendet.");
        System.exit(0);
    }
}
