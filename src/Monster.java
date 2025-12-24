import java.awt.Image;

// Le monstre est un objet qui bouge, donc il hérite de DynamicThings.
public class Monster extends DynamicThings {
    
    // --- VARIABLES DE COMPORTEMENT ---
    private boolean isAggro = false;  // Est-ce qu'il a vu le héros ? (Oui/Non)
    private double directionY = 1;    // Direction de patrouille (Haut/Bas)
    private double patrolSpeed = 2.0; // Vitesse quand il patrouille (calme)
    private double chaseSpeed = 2.5;  // Vitesse quand il poursuit le héros (énervé)
    
    // --- STATISTIQUES ---
    // On met 8 PV pour qu'il résiste à 4 coups (si un coup = 2 dégâts).
    private int maxLife = 8; 
    private int life = 8; 

    // --- GESTION DU RECUL (KNOCKBACK) ---
    // Quand on tape un monstre, il doit reculer sous le choc.
    private boolean isKnockback = false; // Est-il en train de reculer ?
    private int knockbackTimer = 0;      // Combien de temps dure le recul ?
    private double knockbackX = 0;       // Vitesse de recul en X
    private double knockbackY = 0;       // Vitesse de recul en Y

    // Constructeur : Initialise la position, l'image et la HitBox.
    public Monster(int x, int y, Image image) {
        super(x, y, image);
        // On définit une taille de hitbox standard pour les monstres (32x32).
        if (this.getHitBox() != null) this.getHitBox().setSize(32, 32);
    }

    // Méthode simple pour prendre des dégâts (sans recul).
    // Utile si le monstre marche sur un piège par exemple.
    public boolean takeDamage(int damage) {
        return takeDamage(damage, 0, 0); 
    }

    // Méthode AVANCÉE pour prendre des dégâts AVEC recul.
    // attackerX/Y : la position de celui qui attaque (le héros ou le missile).
    public boolean takeDamage(int damage, double attackerX, double attackerY) {
        this.life -= damage;
        
        // Si l'attaquant a une position définie (différente de 0,0)
        if (attackerX != 0 || attackerY != 0) {
            this.isKnockback = true;  // Active le mode recul
            this.knockbackTimer = 5;  // Durée du recul (5 images, soit ~150ms)
            
            // Calcul du vecteur de recul (direction opposée à l'attaquant)
            double dx = this.x - attackerX;
            double dy = this.y - attackerY;
            double dist = Math.sqrt(dx*dx + dy*dy);
            
            // Normalisation du vecteur et application d'une force de 10.
            if (dist > 0) {
                this.knockbackX = (dx / dist) * 10; 
                this.knockbackY = (dy / dist) * 10;
            }
        }
        System.out.println("PV restants : " + this.life); // Debug console
        return this.life <= 0; // Renvoie 'true' si le monstre est mort.
    }

    // Accesseurs
    public int getLife() { return life; }
    public int getMaxLife() { return maxLife; }

    // C'est le cerveau du monstre. Appelé à chaque tour de boucle.
    public void update(Dungeon dungeon, Hero hero) {
        
        // --- GESTION DU RECUL ---
        // Si le monstre est sonné (recul), il ne réfléchit pas, il vole en arrière.
        if (isKnockback) {
            moveIfPossible(knockbackX, knockbackY, dungeon);
            knockbackTimer--;
            if (knockbackTimer <= 0) isKnockback = false; // Fin du recul
            return; // On arrête là pour ce tour.
        }

        // Calcul de la distance avec le héros.
        double distHero = Math.sqrt(Math.pow(hero.getX() - this.x, 2) + Math.pow(hero.getY() - this.y, 2));

        // --- DÉTECTION (AGGRO) ---
        // Si le monstre est calme, il vérifie si le héros passe devant lui.
        if (!isAggro) {
            double detectionLine = this.x - 200; // Il voit à 200 pixels devant lui (à gauche).
            boolean crossLine = hero.getX() > detectionLine;
            boolean notTooFar = hero.getX() < this.x + 100;
            boolean sameHeight = Math.abs(hero.getY() - this.y) < 100;
            
            // Si le héros est dans la zone de détection, le monstre s'énerve.
            if (crossLine && notTooFar && sameHeight) this.isAggro = true; 
        }

        // --- COMPORTEMENT (AGGRO vs PATROUILLE) ---
        if (isAggro) {
            // Le monstre cherche une cible (Héros ou Tornade).
            Things target = null;       
            double minDist = distHero;  
            boolean targetIsHero = true;

            // Il vérifie s'il y a une tornade plus proche que le héros (le Lutin détourne l'attention !)
            for (Things t : dungeon.getListThings()) {
                if (t instanceof Tornado) {
                    double d = Math.sqrt(Math.pow(t.x - this.x, 2) + Math.pow(t.y - this.y, 2));
                    if (d < minDist && d < 300) {
                        minDist = d;
                        target = t;
                        targetIsHero = false;
                    }
                }
            }

            // Si la cible est le héros, il fonce dessus.
            if (targetIsHero) {
                moveToTarget(hero.getX(), hero.getY(), dungeon);
                if (distHero > 600) isAggro = false; // Si le héros fuit trop loin, il se calme.
            } else {
                // Sinon, il fonce sur la tornade.
                if (target != null) moveToTarget(target.x, target.y, dungeon);
            }
        } else {
            // Si pas aggro, il fait des allers-retours (Patrouille).
            double oldY = this.y;
            moveIfPossible(0, directionY * patrolSpeed, dungeon);
            // S'il touche un mur (ne bouge plus), il change de sens.
            if (Math.abs(this.y - oldY) < 0.1) directionY = -directionY; 
        }
    }
    
    // Fonction utilitaire pour se déplacer vers un point précis (tx, ty).
    private void moveToTarget(double tx, double ty, Dungeon dungeon) {
        double dx = tx - this.x;
        double dy = ty - this.y;
        double dist = Math.sqrt(dx*dx + dy*dy);
        
        // On normalise le vecteur pour avoir une vitesse constante.
        if (dist > 0) {
            dx = dx / dist;
            dy = dy / dist;
            moveIfPossible(dx * chaseSpeed, dy * chaseSpeed, dungeon);
        }
    }
}