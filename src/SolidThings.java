import java.awt.Image;

public class SolidThings extends Things {

    public SolidThings(double x, double y, Image image) {
        super(x, y, image);
        // On définit la hitbox pour les murs (solides)
        if (getHitBox() != null) {
            getHitBox().setSize(32, 32);
        }
    }

    @Override
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
        // Si on a besoin de logique supplémentaire ici plus tard
    }
}