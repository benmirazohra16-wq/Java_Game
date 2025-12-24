// On a besoin de la classe Image pour afficher la boule de feu.
import java.awt.Image;

// La classe Missile hérite de DynamicThings car c'est un objet qui bouge et qui a une HitBox.
public class Missile extends DynamicThings {
    
    // Direction du tir (-1, 0, ou 1 pour X et Y).
    private double directionX;
    private double directionY;
    
    // Point de départ (pour calculer la distance parcourue).
    private double startX, startY;
    
    // Heure de création (pour qu'il disparaisse après quelques secondes).
    private long creationTime;

    // Constructeur : On initialise la position, l'image et la direction.
    public Missile(int x, int y, Image image, double dx, double dy) {
        super(x, y, image); // Appel au constructeur parent
        this.directionX = dx;
        this.directionY = dy;
        this.startX = x;
        this.startY = y;
        this.creationTime = System.currentTimeMillis(); // On lance le chrono
        
        // On ajuste la taille de la HitBox pour qu'elle corresponde à la boule de feu.
        if (getHitBox() != null) getHitBox().setSize(24, 24);
    }

    // Méthode pour faire avancer le missile.
    public void move(Dungeon dungeon) {
        // On utilise moveIfPossible pour que le missile s'arrête s'il touche un mur.
        // La vitesse est fixée à 10 (plus rapide que le héros).
        moveIfPossible(directionX * 10, directionY * 10, dungeon);
        
        // On s'assure que la HitBox suit bien l'image.
        if (getHitBox() != null) {
            getHitBox().setPosition(this.x, this.y);
        }
    }
    
    // Vérifie si le missile est trop vieux (plus de 2 secondes).
    public boolean isExpired() {
        return (System.currentTimeMillis() - creationTime) > 2000;
    }

    // Calcule la distance parcourue depuis le départ (Pythagore).
    // Utile pour supprimer le missile s'il va trop loin.
    public double getDistanceTraveled() {
        return Math.sqrt(Math.pow(this.x - startX, 2) + Math.pow(this.y - startY, 2));
    }
}