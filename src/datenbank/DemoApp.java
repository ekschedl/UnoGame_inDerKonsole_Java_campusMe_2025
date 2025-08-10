package datenbank;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.HashMap;


public class DemoApp {

    private static final String CREATETABLE = "CREATE TABLE Sessions (Player varchar(100) NOT NULL, Session int NOT NULL, Round int NOT NULL, Score int NOT NULL, CONSTRAINT PK_Sessions PRIMARY KEY (Player, Session, Round));";
    private static final String INSERT_TEMPLATE = "INSERT INTO Sessions (Player, Session, Round, Score) VALUES ('%1s', %2d, %3d, %4d);";
    private static final String SELECT_BYPLAYERANDSESSION = "SELECT Player, SUM(Score) AS Score FROM Sessions WHERE Player = '%1s' AND Session = %2d;";

    public static void main(String[] args) {
        try {
            SqliteClient client = new SqliteClient("demodatabase.sqlite");
            if (client.tableExists("Sessions")) {
                client.executeStatement("DROP TABLE Sessions;");
            }

            client.executeStatement("CREATE TABLE Sessions (Player varchar(100) NOT NULL, Session int NOT NULL, Round int NOT NULL, Score int NOT NULL, CONSTRAINT PK_Sessions PRIMARY KEY (Player, Session, Round));");
            client.executeStatement(String.format("INSERT INTO Sessions (Player, Session, Round, Score) VALUES ('%1s', %2d, %3d, %4d);", "Anita", 1, 1, 50));
            client.executeStatement(String.format("INSERT INTO Sessions (Player, Session, Round, Score) VALUES ('%1s', %2d, %3d, %4d);", "Hans", 1, 1, 0));
            client.executeStatement(String.format("INSERT INTO Sessions (Player, Session, Round, Score) VALUES ('%1s', %2d, %3d, %4d);", "Anita", 1, 2, 20));
            client.executeStatement(String.format("INSERT INTO Sessions (Player, Session, Round, Score) VALUES ('%1s', %2d, %3d, %4d);", "Hans", 1, 2, 100));

            for (HashMap<String, String> map : client.executeQuery(String.format("SELECT Player, SUM(Score) AS Score FROM Sessions WHERE Player = '%1s' AND Session = %2d;", "Anita", 1))) {
                PrintStream var10000 = System.out;
                String var10001 = (String) map.get("Player");
                var10000.println(var10001 + " hat derzeit:  " + (String) map.get("Score") + " Punkte");
            }
        } catch (SQLException ex) {
            System.out.println("Ups! Something went wrong:" + ex.getMessage());
            ex.printStackTrace();
        }

    }
}


