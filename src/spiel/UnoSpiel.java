package spiel;

import datenbank.SessionDB;
import karten.Farbe;
import spieler.Spieler;
import spieler.SpielerManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Zentrale Spiellogik für ein UNO-Spiel mit Mensch und Bot:

 * - Begrüßung
 * - Highscore-Anzeige zu Beginn
 * - Erstellung und zufällige Reihenfolge der Spieler (Name oder Bot)
 * - Ablauf der Spielrunden (über UnoSpielRunde)
 * - Verwaltung des Rundenzählers und Spielerreihenfolge
 * - Entscheidung über Spielende (Siegpunktzahl oder manuelles Beenden)
 * - Anzeige des Punktestands nach jeder Runde
 * - Anzeige und Speicherung des endgültigen Siegers

 * - Farbige Konsolenausgabe für Spieler, Punkte und Glückwunsch
 * - Speicherung des Spielstands in einer SQL-Datenbank
 */
public class UnoSpiel {
    private final Scanner scanner;
    private final SpielerManager spielerManager;
    private final SessionDB db;

    public UnoSpiel(Scanner scanner, SessionDB db) {
        this.scanner = scanner;
        this.spielerManager = new SpielerManager();
        this.db = db;
    }

    public void starten() {
        System.out.println("\n🟥🟩🟦🟨 Willkommen bei UNO!");

        highscoreAnzeigen(); // Zeigt Highscores aus der Datenbank

        // Spieler erstellen (mit Namen oder als Bot) und mischen
        spielerManager.spielerErstellenUndMischen(scanner, Einstellungen.ANZAHL_SPIELER);

        int rundenNummer = 0;
        boolean spielLaeuft = true;

        // Hauptspielschleife: läuft so lange, bis jemand 500 Punkte hat oder abgebrochen wird
        while (spielLaeuft && getGewinner() == null) {
            rundenNummer++; // Zähler erhöhen

            List<Spieler> spielerListe = spielerManager.getSpielerListe(); // aktuelle Reihenfolge

            zeigeRundenStartInfo(rundenNummer, spielerListe); // Ausgabe: Runde + Reihenfolge

            // Startet eine neue Spielrunde mit dieser Spielerreihenfolge
            UnoSpielRunde runde = new UnoSpielRunde(scanner, spielerListe);
            Spieler rundenGewinner = runde.starten(); // Rückgabe: Sieger dieser Runde

            // Der Gewinner der Runde soll die nächste Runde beginnen
            spielerManager.setStartSpieler(rundenGewinner);

            // Nach jeder Runde den Punktestand anzeigen
            System.out.println("\n📊 Spielstand nach Runde " + rundenNummer + ":");
            punktestandAnzeigen();

            // Wenn jemand 500+ Punkte hat → Spielende
            if (getGewinner() != null) break;

            // Abfrage: Wollen die Spieler weiterspielen?
            System.out.print("\nMöchtet ihr eine weitere Runde spielen? (\u001B[1m\u001B[35mj/n\u001B[0m): ");
            String antwort = scanner.nextLine().trim().toLowerCase();
            if (!antwort.equals("j")) {
                System.out.println("🚪 Spiel beendet.");
                break;
            }
        }

        // Nach dem Spiel → Gewinnertext + Spielstand speichern
        Spieler gewinner = getGewinner();
        if (gewinner != null) {
            spielstandSpeichern(rundenNummer); // Spielstand speichern

            // Glückwunsch-Text mit wechselnden Farben
            String[] farben = { "\u001B[31m", "\u001B[93m", "\u001B[32m", "\u001B[34m" };
            String text = "✨Du hast gewonnen! ✨";
            StringBuilder farbigText = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
                char buchstabe = text.charAt(i);
                String farbe = farben[i % farben.length];
                farbigText.append(farbe).append(buchstabe);
            }
            farbigText.append("\u001B[0m"); // Farb-Reset

            System.out.println("\n🟥🟩🟦🟨 Herzlichen Glückwunsch, " +
                    Farbe.GELB.farbig(gewinner.getName()) +
                    "!\n " + farbigText);
        }
    }

    // Gibt aktuellen Punktestand aller Spieler farbig aus
    private void punktestandAnzeigen() {
        List<Spieler> spielerListe = spielerManager.getSpielerListe();
        String[] farben = { "\u001B[91m", "\u001B[92m", "\u001B[94m", "\u001B[93m" };

        for (int i = 0; i < spielerListe.size(); i++) {
            Spieler s = spielerListe.get(i);
            String farbe = farben[i % farben.length];

            System.out.println(farbe + s.getName() + (s.istBot() ? " (Bot)" : "") +
                    "\u001B[0m: " + s.getPunkte() + " Punkte");
        }
    }

    // Zeigt die Highscore-Tabelle aus der Datenbank
    private void highscoreAnzeigen() {
        try {
            db.highscoreAnzeigen();
        } catch (SQLException e) {
            System.out.println("Fehler beim Zugriff auf die Datenbank: " + e.getMessage());
        }
    }

    // Speichert aktuellen Spielstand in die Datenbank
    private void spielstandSpeichern(int runde) {
        try {
            db.spielstandSpeichern(spielerManager.getSpielerListe(), runde);
        } catch (SQLException e) {
            System.out.println("Fehler beim Zugriff auf die Datenbank: " + e.getMessage());
        }
    }

    // Gibt Spieler zurück, wenn dieser mindestens 500 Punkte hat, sonst null
    private Spieler getGewinner() {
        for (Spieler s : spielerManager.getSpielerListe()) {
            if (s.getPunkte() >= Einstellungen.SIEG_PUNKTE) {
                return s;
            }
        }
        return null;
    }

    // Zeigt den Beginn der Runde + aktuelle Reihenfolge der Spieler
    private void zeigeRundenStartInfo(int runde, List<Spieler> spielerListe) {
        System.out.println("\n🟥🟩🟦🟨 Runde " + runde + " beginnt!");
        spielerManager.zeigeReihenfolge();
    }
}