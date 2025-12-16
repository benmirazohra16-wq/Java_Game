// Cette classe permet créer la boite invisible qui nous permettra de rentrer en collisions avec différents objets
public class HitBox {
    // Déclaration des attributs de notre HitBox
    private double x;
    private double y;
    private double width;
    private double height;



    // Initialise la boîte avec une position et une taille.
    public HitBox(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }

    //Permettent de lire les valeurs private depuis l'extérieur.
    public double getX() {
        return this.x;
    }
    
    public double getY() {
        return this.y;
    }

        public double getHeight() {
        return this.height;
    }

        public double getWidth() {
        return this.width;
    }
    // Mise a jour de la positionde notre boite 
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Méthode pour vérifier la collision entre deux rectangles
    public boolean intersect(HitBox anotherHitBox) {
        
        
        double leftX;   // La position X du rectangle le plus à gauche
        double rightX;  // La position X de l'autre rectangle
        double widthLeft; // La largeur du rectangle le plus à gauche
        
        // On cherche qui est le plus à gauche (le plus petit x)
        if (this.x < anotherHitBox.x) {
            leftX = this.x;
            rightX = anotherHitBox.x;
            widthLeft = this.width;
        } else {
            leftX = anotherHitBox.x;
            rightX = this.x;
            widthLeft = anotherHitBox.width;
        }
        
        // Il y a chevauchement si la largeur dépasse la distance entre les deux
        // Distance = (rightX - leftX)
        boolean xOverlap = (widthLeft > (rightX - leftX));


        // On a exactement la même logique pour Y (topY, bottomY, heightTop)
        double topY;
        double bottomY;
        double heightTop;

        if (this.y < anotherHitBox.y) {
             topY = this.y;
             bottomY = anotherHitBox.y;
             heightTop = this.height;
        } else {
             topY = anotherHitBox.y;
             bottomY = this.y;
             heightTop = anotherHitBox.height;
        }

        // Calcule yOverlap
        boolean yOverlap = (heightTop > (bottomY - topY));

        // Résultat Final
        return xOverlap && yOverlap;

    }

    //Lors de nos tests, nous nous sommes rendu compte qu'il n'y avait aucune collision avec le mur droit et le mur bas
    // Pour modifier cela, nous mettons a jour la taille de la HitBox quand nous changeons notre image : 

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
}