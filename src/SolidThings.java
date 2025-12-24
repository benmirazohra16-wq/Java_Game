import java.awt.Image;

// Cette classe sert à définir tous les objets qui bloquent le passage (Murs, Rochers, Arbres...).
// Elle hérite de 'Things', donc elle a une position et une image.
public class SolidThings extends Things {

    public SolidThings(double x, double y, Image image) {
        super(x, y, image);
        // C'est ici qu'on définit la règle physique :
        // On impose une HitBox de 32x32 pixels (la taille exacte d'une case de notre grille).
        // C'est grâce à ça que le système de collision saura qu'il y a un obstacle ici.
        if (getHitBox() != null) {
            getHitBox().setSize(32, 32);
        }
    }

    @Override
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
        // On garde cette méthode accessible "au cas où".
        // Si plus tard on veut faire des murs qui bougent (comme des portes coulissantes),
        // on pourra ajouter la logique ici sans casser le reste.
    }
}