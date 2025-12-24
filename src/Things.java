import java.awt.Image;

public class Things {
    public double x;
    public double y;
    protected Image image;
    private HitBox hitBox;

    public Things(double x, double y, Image image) {
        this.x = x;
        this.y = y;
        this.image = image;
        // On crée une HitBox par défaut de 32x32
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

    // C'EST CETTE MÉTHODE QUI MANQUAIT POUR SOLIDTHINGS
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        if (hitBox != null) {
            hitBox.setPosition(x, y);
        }
    }
}