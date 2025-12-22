import javax.sound.sampled.*;
import java.io.File;

public class AudioPlayer {
    private Clip clip;
    private FloatControl volumeControl; // Le "bouton" de volume

    public void playMusic(String filePath) {
        // On arrête toujours la musique précédente avant de lancer la nouvelle
        stopMusic();

        try {
            File musicPath = new File(filePath);
            
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                clip = AudioSystem.getClip();
                clip.open(audioInput);

                // On essaie de récupérer le contrôle du volume
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                }

                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY); // Musique en boucle
            } else {
                System.out.println("Fichier introuvable : " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    // Pour monter ou baisser le son
    public void adjustVolume(float value) {
        if (volumeControl != null) {
            float current = volumeControl.getValue();
            float newValue = current + value;
            
            // Limites pour ne pas faire planter le son (Max 6.0, Min -80.0)
            if (newValue > 6.0f) newValue = 6.0f;     
            if (newValue < -80.0f) newValue = -80.0f; 

            volumeControl.setValue(newValue);
            System.out.println("Volume : " + newValue);
        }
    }
}