// Cette classe représente la "boîte de collision" invisible autour des objets.
// C'est ce qui permet de dire "Je touche quelque chose" ou "Je rentre dans un mur".
public class HitBox {
    
    // Coordonnées et dimensions de la boîte.
    // On utilise des 'double' (nombres à virgule) pour être précis au pixel près.
    private double x;
    private double y;
    private double width;  // Largeur
    private double height; // Hauteur

    // Constructeur : On crée la boîte en lui donnant sa position et sa taille de départ.
    public HitBox(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Méthode pour déplacer la boîte.
    // IMPORTANT : Quand le héros bouge, sa HitBox doit bouger avec lui !
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Méthode pour redimensionner la boîte si besoin (ex: un boss qui grossit, ou ajuster la taille du héros).
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    // C'est LE CŒUR du système de collision.
    // Cette méthode vérifie si "ma" boîte touche "l'autre" boîte.
    public boolean intersect(HitBox other) {
        // On utilise l'algorithme AABB (Axis-Aligned Bounding Box).
        // C'est une formule mathématique très rapide qui vérifie si deux rectangles se chevauchent.
        // Si les 4 conditions sont vraies, c'est qu'il y a collision.
        return (this.x < other.x + other.width &&   // Ma gauche est avant sa droite
                this.x + this.width > other.x &&   // Ma droite est après sa gauche
                this.y < other.y + other.height && // Mon haut est avant son bas
                this.y + this.height > other.y);   // Mon bas est après son haut
    }
    
    // Accesseurs pour récupérer les infos de la boîte si nécessaire ailleurs.
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}