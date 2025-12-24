// On importe les classes Java nécessaires pour le son (sampled) et la gestion de fichiers.
import javax.sound.sampled.*;
import java.io.File;

// On crée une classe AudioPlayer pour gérer tout ce qui touche au son dans notre jeu.
public class AudioPlayer {
    
    // On garde une référence au clip de musique de fond pour pouvoir l'arrêter ou modifier son volume.
    private Clip musicClip; 
    // On garde aussi le contrôle du volume pour la musique.
    private FloatControl musicVolume; 

    // Méthode pour lancer une musique de fond (en boucle).
    public void playMusic(String filePath) {
        // Avant de lancer une nouvelle musique, on arrête l'ancienne si elle joue déjà.
        stopMusic();
        try {
            // On vérifie si le fichier audio existe bien.
            File musicPath = new File(filePath);
            if (musicPath.exists()) {
                // On prépare le flux audio.
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                // On récupère un Clip (un conteneur pour le son).
                musicClip = AudioSystem.getClip();
                // On charge le son dans le Clip.
                musicClip.open(audioInput);
                
                // Si on peut contrôler le volume (ce n'est pas toujours le cas selon le PC), on le fait.
                if (musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    musicVolume = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
                    // On baisse un peu le son de la musique (-10dB) pour ne pas couvrir les bruitages.
                    musicVolume.setValue(-10.0f); 
                }
                
                // On lance la musique.
                musicClip.start();
                // Et on lui dit de tourner en boucle infinie (LOOP_CONTINUOUSLY).
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) { 
            // Si quelque chose se passe mal (fichier introuvable, format incorrect), on affiche l'erreur.
            e.printStackTrace(); 
        }
    }

    // Méthode pour jouer un bruitage court (effet sonore) une seule fois.
    public void playSound(String filePath) {
        try {
            File soundPath = new File(filePath);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip sfxClip = AudioSystem.getClip(); 
                sfxClip.open(audioInput);
                
                // Pour les bruitages, on met le volume plus fort (+6dB) pour qu'ils claquent bien !
                if (sfxClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gain = (FloatControl) sfxClip.getControl(FloatControl.Type.MASTER_GAIN);
                    gain.setValue(6.0f); 
                }
                
                // On lance le bruitage (pas de boucle ici, on veut juste un "BANG" ou un "CLING").
                sfxClip.start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Méthode pour arrêter proprement la musique.
    public void stopMusic() {
        // On vérifie si le clip existe et s'il est en train de jouer.
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();  // On arrête la lecture.
            musicClip.close(); // On libère la mémoire.
        }
    }

    // Méthode pour ajuster le volume en cours de jeu (avec les touches + et -).
    public void adjustVolume(float value) {
        if (musicVolume != null) {
            // On calcule le nouveau volume.
            float newVol = musicVolume.getValue() + value;
            
            // On s'assure de rester dans des limites raisonnables (entre -80dB et +6dB).
            if (newVol > 6.0f) newVol = 6.0f;     
            if (newVol < -80.0f) newVol = -80.0f; 
            
            // On applique le nouveau volume.
            musicVolume.setValue(newVol);
        }
    }
}