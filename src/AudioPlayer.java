import javax.sound.sampled.*;
import java.io.File;

public class AudioPlayer {
    private Clip musicClip; 
    private FloatControl musicVolume; 

    public void playMusic(String filePath) {
        stopMusic();
        try {
            File musicPath = new File(filePath);
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                musicClip = AudioSystem.getClip();
                musicClip.open(audioInput);
                if (musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    musicVolume = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
                    musicVolume.setValue(-10.0f); // Musique plus douce
                }
                musicClip.start();
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void playSound(String filePath) {
        try {
            File soundPath = new File(filePath);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip sfxClip = AudioSystem.getClip(); 
                sfxClip.open(audioInput);
                if (sfxClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gain = (FloatControl) sfxClip.getControl(FloatControl.Type.MASTER_GAIN);
                    gain.setValue(6.0f); // Bruitage à fond !
                }
                sfxClip.start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
            musicClip.close();
        }
    }

    public void adjustVolume(float value) {
        if (musicVolume != null) {
            float newVol = musicVolume.getValue() + value;
            if (newVol > 6.0f) newVol = 6.0f;     
            if (newVol < -80.0f) newVol = -80.0f; 
            musicVolume.setValue(newVol);
        }
    }
}