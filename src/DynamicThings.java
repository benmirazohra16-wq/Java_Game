// C'est dans cette classe, que toute la partie collision et déplacements et effectués

import java.awt.Image;

public class DynamicThings extends AnimatedThings {
    private double speedX;
    private double speedY;

    // On commence par appeller le constructeur parent afin de pouvoir initialiser la HitBox et l'image
    public DynamicThings(int x, int y, Image image) {
        super(x, y, image);
        this.speedX = 0;
        this.speedY = 0;
    }

    public void moveIfPossible(double dx, double dy, Dungeon dungeon) {
        // On applique le mouvement de manière positif, en parant du principe qu'il avancera 
        // et on verra si il rentre ou non en collision avec un mur ou autre
        this.x += dx;
        this.y += dy;
        
        // On met à jour la position de la HitBox aussi, sinon elle reste derrière
        if (this.getHitBox() != null) {
            this.getHitBox().setPosition(this.x, this.y); 
        }

        // On vérifie si on touche quelque chose de solide, définie grace 0 la classe SolidThings
        boolean isCollide = false;
        
        // On parcourt tous les objets présent sur notre carte dans le donjon
        for (Things thing : dungeon.getListThings()) {
            // On ne se teste pas contre soi-même (this), et on teste uniquement les objets de la classe SolidThings
            if (thing instanceof SolidThings && thing != this) {
                
                // On récupère la hitbox de l'obstacle
                HitBox otherHitBox = ((SolidThings) thing).getHitBox();
                
                // Si collision entre notre hitbox ( celle du hero du coup) et celle de l'obstacle ( n'impotre quel objet de type SolidThings)
                if (this.getHitBox().intersect(otherHitBox)) {
                    isCollide = true;
                    break; // Pas besoin de continuer, on a touché un obstacle
                }
            }
        }

        // Si collision, on annule le mouvement (on se retourve donc bloquer devant et on recule)
        if (isCollide) {
            this.x -= dx;
            this.y -= dy;
            // Et on remet la HitBox à sa place d'origine, la mise à jour est importante pour mettre à la hitbox d'être toujours opérationnel
            if (this.getHitBox() != null) {
                this.getHitBox().setPosition(this.x, this.y);
            }
        }
    }

}