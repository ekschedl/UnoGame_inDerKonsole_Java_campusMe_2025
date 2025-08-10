package spiel;

import java.awt.*;
import java.io.File;

import javax.sound.sampled.*;



public class Sound {

    // UNO sagen
    public static void playSound(String filename) {
        try {
            // Lade die Audiodatei ( "UNO.wav")
            File soundFile = new File(filename);

            // Erzeuge einen Audio-Stream daraus
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);

            // Erzeuge ein Clip-Objekt (zum Abspielen kurzer Sounds)
            Clip clip = AudioSystem.getClip();

            // Clip mit dem Sound verbinden
            clip.open(audioStream);

            // Sound abspielen
            clip.start();

            // Programmablauf unterbrechen, bis die Datei abgespielt wurde
            //Thread.sleep(clip.getMicrosecondLength());

        } catch (Exception e) {
            // Wenn ein Fehler beim Abspielen passiert oder falsches Format
            e.printStackTrace();

            // Als Notlösung – Standard-System-Beep abspielen
            Toolkit.getDefaultToolkit().beep();
        }
    }
}