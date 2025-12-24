// On a besoin de la classe Image pour afficher quelque chose à l'écran.
import java.awt.Image;

// La classe Effect hérite de "Things" car c'est un objet qui a une position et une image,
// mais qui ne bouge pas (pas "Dynamic") et qui ne bloque pas le passage (pas "Solid").
public class Effect extends Things {
    
    // On note l'heure précise (en millisecondes) à laquelle l'effet a été créé.
    private long creationTime;
    
    // On définit combien de temps (en millisecondes) l'effet doit rester visible.
    private int lifeTime; 

    // Constructeur : 
    // On accepte des positions en 'double' (nombres à virgule) car les missiles ou les collisions
    // peuvent se produire à des coordonnées précises (ex: 10.5, 20.3).
    public Effect(double x, double y, Image image, int lifeTime) {
        super(x, y, image); // On initialise l'image et la position via la classe mère (Things).
        this.creationTime = System.currentTimeMillis(); // On démarre le chrono
        this.lifeTime = lifeTime; // On enregistre la durée de vie.
    }

    // Cette méthode permet de savoir si l'effet est terminé.
    // On regarde si le temps écoulé depuis la création est supérieur à la durée de vie prévue.
    public boolean isExpired() {
        return (System.currentTimeMillis() - creationTime) > lifeTime;
    }
}