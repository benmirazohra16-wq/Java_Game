import java.awt.Image;

// La classe Hero est un singleton : cela signifie qu'il ne peut y avoir qu'UN SEUL héros dans tout le jeu.
// Elle est "final" car on ne veut pas qu'une autre classe hérite du héros.
public final class Hero extends DynamicThings {
    
    // L'instance unique du héros. "volatile" assure que tous les processus voient la même version.
    private static volatile Hero instance = null;
    
    // Caractéristiques du héros
    private HeroType type;
    private double speed = 10.0; 
    private Orientation orientation = Orientation.BOTTOM; // Direction du regard
    private boolean hasWeapon = false; // A-t-il ramassé son arme ?
    private int magicienHealCount = 0; // Compteur pour le soin spécial du Magicien
    private Image[][] sprites; // Tableau pour stocker les images d'animation (Haut, Bas, Gauche, Droite)
    private int life = 100; // Points de vie (PV)

    // Constructeur PRIVÉ : Personne d'autre ne peut créer de héros avec "new Hero()".
    private Hero() {
        super(0, 0, null); // Position 0,0 temporaire
        this.type = HeroType.CHEVALIER; // Type par défaut
        this.speed = 5.0; 
    }

    // Méthode publique pour récupérer l'unique héros (Pattern Singleton).
    // Si le héros n'existe pas encore, on le crée. Sinon, on renvoie celui qui existe.
    public static Hero getInstance() {
        if (instance == null) {
            synchronized (Hero.class) { // Sécurité pour éviter d'en créer deux en même temps
                if (instance == null) instance = new Hero();
            }
        }
        return instance;
    }

    // Définit le type de héros (choisi dans le menu) et charge ses images.
    public void setType(HeroType type) {
        this.type = type;
        String filename = "chevalier.png";
        int largeurPerso = 24; 
        int hauteurPerso = 32;

        // Configuration des stats selon la classe
        switch (type) {
            case CHEVALIER: this.speed = 5.0;  filename = "chevalier.png"; break; // Lent mais résistant
            case MAGICIEN:  this.speed = 10.0; filename = "magicien.png"; break;  // Moyen
            case LUTIN:     this.speed = 20.0; filename = "lutin.png"; break;     // Très rapide
        }

        try {
            // Chargement des sprites (découpage de l'image du héros)
            TileManager tmHero = new TileManager(largeurPerso, hauteurPerso, filename); 
            this.sprites = new Image[4][3]; // 4 directions, 3 images par direction (marche)
            for(int col=0; col<3; col++) {
                sprites[0][col] = tmHero.getTile(col, 0); // Haut
                sprites[1][col] = tmHero.getTile(col, 3); // Gauche
                sprites[2][col] = tmHero.getTile(col, 2); // Bas
                sprites[3][col] = tmHero.getTile(col, 1); // Droite
            }
            this.setImage(sprites[2][1]); // Image par défaut (regarde en bas)
            // Ajustement de la hitbox pour qu'elle colle bien aux pieds du perso
            if (this.getHitBox() != null) this.getHitBox().setSize(14, 10);
        } catch (Exception e) {
            System.out.println("Problème image hero : " + filename);
        }
    }

    // Mise à jour de la position (surcharge de la méthode parente pour ajuster la hitbox)
    @Override
    public void setPosition(double x, double y) {
        this.x = (int)x;
        this.y = (int)y;
        // On décale un peu la hitbox pour qu'elle soit centrée sur le bas du sprite
        if (getHitBox() != null) getHitBox().setPosition(x + 5, y + 22);
    }
    
    // Gère l'attaque du héros
    public void attack(Dungeon dungeon, TileManager tm) {
        if (!hasWeapon) { System.out.println("Pas d'arme !"); return; } // Impossible d'attaquer sans arme

        // Attaque du CHEVALIER (Corps à corps)
        if (this.type == HeroType.CHEVALIER) {
            // On calcule la case devant le héros
            int devantX = (int)x;
            int devantY = (int)y;
            if (orientation == Orientation.RIGHT) devantX += 32;
            if (orientation == Orientation.LEFT)  devantX -= 32;
            if (orientation == Orientation.BOTTOM) devantY += 32;
            if (orientation == Orientation.TOP)    devantY -= 32;

            // Effet visuel de coup d'épée
            dungeon.getListThings().add(new Effect(devantX, devantY, tm.getTile(5, 5), 200));

            // Dégâts de zone devant lui
            for (int i = dungeon.getListThings().size() - 1; i >= 0; i--) {
                Things thing = dungeon.getListThings().get(i);
                if (thing instanceof Monster) {
                    double distance = Math.sqrt(Math.pow(thing.x - this.x, 2) + Math.pow(thing.y - this.y, 2));
                    if (distance <= 64) { 
                        if (((Monster)thing).takeDamage(2)) dungeon.getListThings().remove(i); 
                    }
                }
            }
        }
        // Attaque du MAGICIEN (Projectile)
        else if (this.type == HeroType.MAGICIEN) {
            double dx = 0, dy = 0;
            // On tire dans la direction du regard
            if (orientation == Orientation.RIGHT) dx = 1;
            if (orientation == Orientation.LEFT)  dx = -1;
            if (orientation == Orientation.BOTTOM) dy = 1;
            if (orientation == Orientation.TOP)    dy = -1;
            dungeon.getListThings().add(new Missile((int)x, (int)y, tm.getTile(8, 1), dx, dy));
        }
    }

    // Soin spécial (limité à 2 fois pour le Magicien)
    public void tryToHeal() {
        if (type == HeroType.MAGICIEN && magicienHealCount < 2) {
            this.life = 100;
            magicienHealCount++;
        }
    }

    // Getters et Setters pour l'arme
    public void setHasWeapon(boolean has) { this.hasWeapon = has; }
    public boolean hasWeapon() { return this.hasWeapon; }

    // Change l'orientation et met à jour l'image affichée
    public void setOrientation(Orientation orientation) {
        if(this.orientation == orientation) return;
        this.orientation = orientation;
        switch(orientation) {
            case TOP:    this.setImage(sprites[0][1]); break;
            case LEFT:   this.setImage(sprites[1][1]); break;
            case BOTTOM: this.setImage(sprites[2][1]); break;
            case RIGHT:  this.setImage(sprites[3][1]); break;
        }
    }
    
    // Accesseurs divers
    public HeroType getType() { return type; }
    public double getSpeed() { return this.speed; }
    public int getLife() { return life; }
    public double getX() { return this.x; }
    public double getY() { return this.y; }

    // Gestion des dégâts reçus
    public void takeDamage(int damage) {
        if (this.type == HeroType.CHEVALIER) damage = damage / 2; // Le Chevalier résiste mieux (armure)
        this.life -= damage;
        if (this.life < 0) this.life = 0;
    }

    // Gestion du soin (par potion)
    public void takePoint(int point) {
        this.life += point;
        if (this.life > 100) this.life = 100;
    }

    // Réinitialisation du héros (nouvelle partie)
    public void respawn() {
        this.life = 100;
        this.x = 300; 
        this.y = 400;
        this.hasWeapon = false;
        this.magicienHealCount = 0;
        this.orientation = Orientation.BOTTOM;
        if(sprites != null) this.setImage(sprites[2][1]);
        if (getHitBox() != null) getHitBox().setPosition(x, y);
    }
}