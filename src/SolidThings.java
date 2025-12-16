import java.awt.Image;

public class SolidThings extends Things {
    private HitBox hitBox;

    public SolidThings(int x, int y, Image image) {
        super(x, y, image); // On passe l'image au parent
        // La hitbox prend la même taille que l'image
        this.hitBox = new HitBox(x, y, super.width, super.height);
    }

    public HitBox getHitBox() {
        return hitBox;
    }

    @Override
    public void setPosition(double x, double y) {
        // On change la position visuelle (via la classe mère Things)
        super.setPosition(x, y);
        
        // On change aussi la position de la HitBox
        if (hitBox != null) {
            hitBox.setPosition(x, y);
        }
    }

    //Lors de nos tests, nous nous sommes rendu compte qu'il n'y avait aucune collision avec le mur droit et le mur bas
    // Pour modifier cela, si on change notre image alors on redimensionne notre HitBox : 
    
    @Override
    public void setImage(Image image) {
        // On change l'image visuelle (via la classe mère)
        super.setImage(image);
        
        // On met à jour la taille de la HitBox pour qu'elle corresponde à l'image
        if (this.hitBox != null) {
            this.hitBox.setSize(this.width, this.height);
        }
    }
}