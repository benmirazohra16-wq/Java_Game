import java.awt.Image;

// La Tornade est un objet statique (pour l'instant) qui reste sur place,
// donc elle hérite de 'Things' (elle a une position et une image).
public class Tornado extends Things {
    
    // On note l'heure exacte de sa création pour savoir quand la faire exploser.
    private long creationTime;

    // Constructeur : On place la tornade et on lance le chrono.
    public Tornado(int x, int y, Image image) {
        super(x, y, image);
        this.creationTime = System.currentTimeMillis();
    }

    // Cette méthode est appelée par le moteur du jeu (MainInterface) pour savoir si c'est le moment d'exploser.
    // Elle renvoie 'true' si 3000 millisecondes (3 secondes) se sont écoulées.
    public boolean isReadyToExplode() {
        return (System.currentTimeMillis() - creationTime) >= 3000;
    }
}