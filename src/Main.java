import datenbank.SessionDB;
import spiel.UnoSpiel;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SessionDB db;
        try {
            db = new SessionDB();
        } catch (Exception ex) {
            System.err.println("Datenbank konnte nicht geöffnet werden: " + ex.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        UnoSpiel spiel = new UnoSpiel(scanner, db);
        spiel.starten();
    }
}

//09.06 VAL
//ENUM Spezialtyp erstellt (anstelle von String)
//Spezialkarte - Farbparameter entfernt (da nur schwarz möglich)
//bei Spezialkarten wird die Farbe Schwarz nicht mitausgegeben in der Konsole
//schönere Anzeigenamen für Enum-Werte(Farbe, Aktionstyp, Spezialtyp) & getDisplayName-Methode dafür erstellt