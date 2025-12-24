import java.awt.Image;

// C'est la CLASSE MÈRE de tous les objets du jeu.
// Hero, Monster, Mur, Potion... tout le monde hérite de 'Things'.
// Cela permet de tous les stocker dans une seule liste (ArrayList<Things>).
public class Things {
    
    // Position sur l'écran (en double pour permettre des mouvements fluides).
    public double x;
    public double y;
    
    // L'image qui sera dessinée par le GameRender.
    protected Image image;
    
    // La boîte de collision associée à cet objet.
    private HitBox hitBox;

    public Things(double x, double y, Image image) {
        this.x = x;
        this.y = y;
        this.image = image;
        // Par défaut, on crée une HitBox de 32x32 (la taille standard d'une case).
        // Les classes filles (comme Hero ou Monster) pourront modifier cette taille si besoin.
        this.hitBox = new HitBox(x, y, 32, 32); 
    }

    public Image getImage() {
        return this.image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public HitBox getHitBox() {
        return hitBox;
    }

    // Méthode cruciale : elle met à jour la position VISUELLE et PHYSIQUE.
    public void setPosition(double x, double y) {
        this.x = x; // Mise à jour visuelle
        this.y = y;
        // Si l'objet a une hitbox, on la déplace aussi.
        // Sinon, la collision resterait sur place pendant que l'objet bouge !
        if (hitBox != null) {
            hitBox.setPosition(x, y);
        }
    }
}