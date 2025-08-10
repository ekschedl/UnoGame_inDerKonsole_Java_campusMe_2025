package spieler;

import karten.Karte;

import java.util.ArrayList;

/**
 * repräsentiert einen Teilnehmer im UNO-Spiel (Mensch oder Bot)

 * Sie verwaltet:
 * - den Namen des Spielers
 * - die Handkarten (als Liste von Karten)
 * - Bot-Status (true = Bot, false = Mensch)
 * - UNO-Ruf (ob „UNO!“ gesagt wurde)
 * - Punktezahl des Spielers

 * Methoden:
 * - Karten ziehen und spielen
 * - Hand anzeigen
 * - UNO-Ruf prüfen/setzen
 * - Bot-Status setzen/abfragen
 * - Spieler-Status als Text liefern
 */
public class Spieler {
    private String name;                       // Name des Spielers
    private ArrayList<Karte> handWithCards;    // Liste der aktuellen Handkarten
    private boolean istBot;                    // true = automatischer Bot-Spieler
    private boolean unoGerufen;                // Hat Spieler „UNO!“ gerufen?
    private int punkte;                        // Aktuelle Punktzahl des Spielers

    // Konstruktor: erstellt neuen Spieler mit Name und leerer Hand
    public Spieler(String name) {
        this.name = name;
        this.handWithCards = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Karte> getHand() {
        return handWithCards;
    }

    // Spieler zieht eine Karte → wird zur Hand hinzugefügt
    public void karteZiehen(Karte karte) {
        handWithCards.add(karte);
    }

    // Spieler spielt eine Karte → sie wird aus seiner Hand entfernt
    // Rückgabe:
    //   true  → Karte war in der Hand und wurde erfolgreich entfernt
    //   false → sollte im normalen Spielverlauf nicht vorkommen – dient zur Sicherheit
    public boolean karteSpielen(Karte karte) {
        return handWithCards.remove(karte);
    }

    // Zeigt alle Karten in der Hand des Spielers mit Nummerierung
    public void handAnzeigen() {
        System.out.println();
        System.out.println("\u001B[1m\u001B[35mDu hast folgende Karten:\u001B[0m");
        for (int i = 0; i < handWithCards.size(); i++) {
            System.out.println((i + 1) + ": " + handWithCards.get(i).anzeigen());
        }
    }

    public boolean isUnoGerufen() {
        return unoGerufen;
    }

    public void setUnoGerufen(boolean unoGerufen) {
        this.unoGerufen = unoGerufen;
    }

    public boolean istBot() {
        return istBot;
    }

    public int getPunkte() {
        return punkte;
    }

    public void setPunkte(int punkte) {
        this.punkte = punkte;
    }

    // Setzt diesen Spieler auf Bot oder Mensch (true = Bot, false = Mensch)
    public void setIstBot(boolean istBot) {
        this.istBot = istBot;
    }

    // Gibt Status des Spielers zurück: z.bB. „Lena (Mensch)“ oder „bot_01 (Bot)“
    public String getStatusString() {
        return name + (istBot ? " (Bot)" : " (Mensch)");
    }
}