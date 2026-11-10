# UNO-Kartenspiel für die Java-Konsole

Teamprojekt an der FH CAMPUS 02. Die Konsolenanwendung bildet ein UNO-Spiel mit vier Spielplätzen ab. Jeder Platz kann von einer Person oder einem Bot übernommen werden. Das Projekt verbindet objektorientierte Modellierung, regelbasierte Spiellogik und lokale Datenhaltung.

## Spielablauf

1. Beim Start erscheint die Bestenliste. Für jeden Spielplatz wird ein Name oder `bot` eingegeben. Die Startreihenfolge wird gemischt.
2. Jede Person und jeder Bot erhält sieben Karten. In der Konsole erscheinen die oberste Karte des Ablagestapels und die Karten auf der Hand. Das Spiel prüft, ob eine Karte abgelegt werden darf. Aktions- und Spezialkarten können die Reihenfolge ändern, jemanden aussetzen lassen, zusätzliche Karten erzwingen oder die nächste Farbe festlegen.
3. Wer zuerst alle Karten ablegt, gewinnt die Runde und erhält Punkte für die verbliebenen Karten der anderen. Danach zeigt die Konsole den aktuellen Spielstand. Das Spiel endet regulär, sobald jemand mindestens 500 Punkte erreicht.

| Eingabe | Aktion |
| --- | --- |
| Kartennummer | Karte aus der eigenen Hand ausspielen |
| `z` | Karte ziehen |
| `uno` | „UNO!“ rufen |
| `k` | UNO-Ruf kontrollieren |
| `h` | Hilfe anzeigen |
| `x` | Spiel beenden |

## Technischer Aufbau

| Bereich | Aufgabe |
| --- | --- |
| `Main` | Initialisiert die SQLite-Datenbank, die Konsoleneingabe und das Spiel. |
| `spiel.UnoSpiel` | Steuert den Ablauf über mehrere Runden, zeigt den Punktestand an und speichert das Ergebnis bei einem regulären Spielende. |
| `spiel.UnoSpielRunde` | Teilt Karten aus, verarbeitet Spielzüge und Karteneffekte und ermittelt den Rundensieg. |
| `karten` | Modelliert Zahlen-, Aktions- und Spezialkarten als Unterklassen von `Karte`. Enums bilden Farben und Kartentypen ab. `Kartenstapel` verwaltet die Karten. |
| `spieler` | `Spieler` verwaltet Handkarten und Punkte. `SpielerManager` erstellt die Spielenden und organisiert die Startreihenfolge. |
| `datenbank` | `SessionDB` legt die Tabelle an, speichert Ergebnisse und fragt Highscores ab. `SqliteClient` führt die SQL-Anweisungen über JDBC aus. |

**Technologien und Konzepte** · Java · objektorientierte Programmierung · JDBC · SQL · SQLite

## Punkte und Bestenliste

Die SQLite-Datei `sessiondb.sqlite` liegt im Arbeitsverzeichnis und wird bei Bedarf angelegt. Bei einem regulären Spielende speichert die Tabelle `Sessions` den Endpunktestand pro Spielperson mit Sitzungs- und Rundennummer. Die Kombination aus Name, Sitzung und Runde ist der Primärschlüssel. Beim nächsten Start fragt das Spiel bis zu fünf gespeicherte Ergebnisse ab 500 Punkten ab, absteigend nach Punktzahl.

## Lokal starten

Voraussetzungen sind ein JDK ab Version 21 und der mitgelieferte Treiber `lib/sqlite-jdbc-3.50.1.0.jar`. Das Projektverzeichnis muss beschreibbar sein, damit SQLite seine Datenbankdatei anlegen kann.

### macOS und Linux

Im Projektordner ausführen:

```bash
mkdir -p out/uno
find src -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -d out/uno
java -cp "out/uno:lib/*" Main
```

### IntelliJ IDEA

Den Projektordner öffnen und ein JDK ab Version 21 auswählen. Falls der SQLite-Treiber fehlt, die JAR-Datei aus `lib` als Modulabhängigkeit hinzufügen. Anschließend `Main.main()` ausführen und den Projektordner als Arbeitsverzeichnis verwenden.

