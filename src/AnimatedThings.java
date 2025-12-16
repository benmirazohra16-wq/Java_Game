// Cette classe hérite de la classe SolidThings pour des objets solides ( avec qui on peut entrer en collision)
// Mais qui ont pour vocation de de bouger ou changer d'apparence, pour le moment il est static
import java.awt.Image;

public class AnimatedThings extends SolidThings { 
    public AnimatedThings(int x, int y, Image image) {
        super(x, y, image); // Me permet d'envoyer des informations à la classe mère
    }
}