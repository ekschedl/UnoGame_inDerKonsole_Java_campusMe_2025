package datenbank;

import spieler.Spieler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;


public class SessionDB {
    private SqliteClient client;
    private final int sessionId;

    public SessionDB() throws SQLException {
        client = new SqliteClient("sessiondb.sqlite"); //wichtig: dieser Datei-Name muss in gitignore eingetragen
        //werden, damit die Datei nicht auf Github mitgepusht wird

        if (!client.tableExists("Sessions")) {
            client.executeStatement("CREATE TABLE Sessions (Player varchar(100) NOT NULL, Session int NOT NULL, Round int NOT NULL, Score int NOT NULL, CONSTRAINT PK_Sessions PRIMARY KEY (Player, Session, Round));");
        }

        sessionId = getNeueSessionId();
    }

    // Findet die ID für die aktuelle Session heraus.
    // Wenn es noch keine Daten in der Datenbank gibt, ist die ID gleich 1;
    // Wenn es bereits alte Sessions gibt, dann ist die ID um 1 höher als die höchste
    private int getNeueSessionId() throws SQLException {
        List<HashMap<String, String>> result = client.executeQuery("SELECT MAX(Session) as MaxSession FROM Sessions;");
        String maxSessionString = result.getFirst().get("MaxSession");

        if (maxSessionString == null) {
            return 1;
        } else {
            return Integer.parseInt(maxSessionString) + 1;
        }
    }

    // Zeigt die 5 besten Ergebnisse in der Datenbank an.
    public void highscoreAnzeigen() throws SQLException {
        System.out.println();
        System.out.println("\u001B[1m\u001B[35mDie All-Time UNO Highscores:\u001B[0m");

        List<HashMap<String, String>> result = client.executeQuery("SELECT Player, Session, MAX(Score) as Score FROM Sessions WHERE Score >= 500 GROUP BY Player, Session ORDER BY Score DESC LIMIT 5;");
        for (HashMap<String, String> map : result) {
            System.out.println(map.get("Player") + ": " + map.get("Score") + " Punkte (Session " + map.get("Session") + ")");
        }

        System.out.println();
    }

    // Zeigt alle Daten in der Datenbank an.
    public void alleDatenAnzeigen() throws SQLException {
        List<HashMap<String, String>> result = client.executeQuery("SELECT * FROM Sessions ORDER BY Session, Round, Player;");
        for (HashMap<String, String> map : result) {
            System.out.println(map);
        }

        System.out.println();
    }

    //hier werden der Spielstand der Spieler und Runde gespeichert
    public void spielstandSpeichern(List<Spieler> spieler, int runde) throws SQLException {
        for (Spieler s : spieler) {
            client.executeStatement(String.format("INSERT INTO Sessions (Player, Session, Round, Score) VALUES ('%1s', %2d, %3d, %4d);",
                    s.getName(), sessionId, runde, s.getPunkte()));
        }
    }

    public void loeschen() throws SQLException {
        client.executeStatement("DROP TABLE Sessions;");
    }
}
