package spieler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
/**
 * Klasse verwaltet die Spieler (Mensch oder Bot)
 * - erstellt Spieler
 * - erlaubt Mischung der Startreihenfolge
 * - speichert Spieler in einer Liste
 */
public class SpielerManager {
    private List<Spieler> spielerListe = new ArrayList<>(); // Liste aller Spieler
    private int botCounter = 1; // Nummerierung für Bots (bot_01, bot_02, ...)

    /**
     * Erstellt gewünschte Anzahl an Spielern (Mensch oder Bot)
     * Mischt am Ende die Reihenfolge zufällig
     */
    public void spielerErstellenUndMischen(Scanner scanner, int anzahl) {
        System.out.println("\u001B[1m\u001B[35mWer spielt?\u001B[0m [" +
                "\u001B[34mbot\u001B[0m = automatisch ⚙\uFE0F | " +
                "\u001B[34mbeliebiger Name\u001B[0m = manuell 👤]");

        for (int i = 1; i <= anzahl; i++) {
            String originalName = validateName(scanner); // Eingabe & Prüfung
            boolean istBot = originalName.equalsIgnoreCase("bot");

            // Wenn Bot, Name automatisch setzen (zb bot_01)
            String name = istBot ? String.format("bot_%02d", botCounter++) : originalName;

            // Spielerobjekt erstellen
            Spieler spieler = new Spieler(name);
            spieler.setIstBot(istBot);

            // Bot-Modus Hinweis ausgeben
            if (istBot) {
                System.out.println("\u001B[34m→ Bot-Modus ein! Spielt als: " + name + "\u001B[0m");
            }

            // Spieler zur Liste hinzufügen
            spielerListe.add(spieler);
        }

        // Startreihenfolge mischen
        Collections.shuffle(spielerListe);
    }

    /**
     * Gibt die aktuelle Spielerliste zurück
     */
    public List<Spieler> getSpielerListe() {
        return spielerListe;
    }

    /**
     * Setzt den gewünschten Spieler an erste Position (Startspieler)
     * Der Rest bleibt in Reihenfolge erhalten (durch Rotation)
     *
     */
    public void setStartSpieler(Spieler spieler) {
        int index = spielerListe.indexOf(spieler);
        if (index != -1) {
            Collections.rotate(spielerListe, -index); // verschiebt so, dass gewählter Spieler an 1. Stelle ist//
        }
    }

    /**
     * Gibt aktuelle Spielreihenfolge aus (mit Symbolen)
     */
    public void zeigeReihenfolge() {
        System.out.println("\n\u001B[1m\u001B[35m↓ Spielreihenfolge:\u001B[0m");
        for (Spieler s : spielerListe) {
            String icon = s.istBot() ? "⚙️ " : "👤 ";
            String spielerText = icon + s.getName() + (s.istBot() ? " (Bot)" : "");
            System.out.println(spielerText);
        }
    }

    /**
     * Fragt Name vom Benutzer ab, prüft auf Gültigkeit:
     * - nicht leer
     * - noch nicht vergeben
     */
    private String validateName(Scanner scanner) {
        while (true) {
            System.out.print("Name des Spielers: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("❌ Name darf nicht leer sein.");
                continue;
            }
            boolean vorhanden = spielerListe.stream()
                    .anyMatch(s -> s.getName().equalsIgnoreCase(name));
            if (vorhanden) {
                System.out.println("❌ Name schon vergeben.");
                continue;
            }
            return name;
        }
    }
}