// Cest notre classe mère, d'elle toutes les autres classes XThings découlent 

import java.awt.Image;
import java.awt.Graphics;


// La classe de base pour tous les objets visibles du jeu.
public class Things {
    // Ici on a des type protected afin de donner le droit au classe filles de modifier directement : x; y... sans passer par un "setters"
    protected int x, y;
    protected int width, height;
    protected Image image;

    // On initialise un objet avec sa position et son apparence propore.
    public Things(int x, int y, Image image) {
        this.x = x;
        this.y = y;
        this.image = image;

        //On calcule la taille de l'objet en fonction de l'image.
        if (image != null) {
            this.width = image.getWidth(null);
            this.height = image.getHeight(null);
        }
    }

    // Méthode pour dessiner - C'est ici qu'on dessine l'objet sur l'écran : Victoire ou Défaite
    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, x, y, null);
        }
    }

    // Permet de changer l'image d'un objet après sa création.
    public void setImage(Image image) {
        this.image = image;
        // Si on change l'image, on doit aussi mettre à jour la taille (width/height)
        if (image != null) {
            this.width = image.getWidth(null);
            this.height = image.getHeight(null);
        }
    }
// Mise à jour de la position de n'importe quels objets : Thins ou déscendants 
    public void setPosition(double x, double y) {
        this.x = (int) x; // ils nous sont utiles en double pour le calcul mais pour l'affichage il est mieux de paser en int
        this.y = (int) y;
    }
}