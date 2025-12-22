// Cette classe nous permet de créer un "modèle de conception" ou "Design Pattern" : Un Singleton
// Cette classe représente le joueur et hérite de : DynamicThings afin de pouvoir bouger et nous permet d'avoir un hitbox pour nos collisions 

import java.awt.Image;

public final class Hero extends DynamicThings {
    private static volatile Hero instance = null;

    private Hero() {
        // On passe null pour l'instant car on n'a pas encore le gestionnaire d'images
        super(0, 0, null); 
    }

    // On instancie ensuite la vie du hero
    private int life = 100;

    public static Hero getInstance() {
        // Si le hero n'existe pas encore
        if (instance == null) {
            synchronized (Hero.class) {
                if (instance == null) {
                    // On le crer une seule fois
                    instance = new Hero();
                }
            }
        }
        return instance;
    }

    public int getLife() {
        return life;
    }


    // Ici, on récupère la position X et Y de notre héro
    public double getX() {
        return this.x; // hérité de la classe Things
    }


    public double getY() {
        return this.y; //hérité de la classe Things
    }

    // On retire des poins de vie au héro, 
    // si il arrive à 0 c'est notre variable boléenne GameOver instancier dans GameRender qui passe à 1 et signifit la fin de partie
    public void takeDamage(int damage) {
        this.life -= damage;
        if (this.life < 0) {
            this.life = 0;
        }
        System.out.println("Attention touché !  Vie restante : " + life);
    }

    // On peut aussi ajouter de la vie a notre héros
    // si il arrive à 100, il ne prends plus rien
    public void takePoint(int point) {
        this.life += point;
        if (this.life > 100) {
            this.life = 100;
        }
        System.out.println("Guérisson en cours Vie restante : " + life);
    }

    public void respawn() {
        this.life = 100;
        // Remet ici les coordonnées de départ (ex: 300, 400 comme dans ton Main)
        this.x = 300; 
        this.y = 400;
        if (getHitBox() != null) {
            getHitBox().setPosition(x, y);
        }
    }
}