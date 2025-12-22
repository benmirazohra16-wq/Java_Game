import java.awt.Image;
import java.util.ArrayList;

public final class Hero extends DynamicThings {
    private static volatile Hero instance = null;
    private HeroType type;
    private double speed = 10.0; 
    private Orientation orientation = Orientation.BOTTOM;

    private Hero() {
        super(0, 0, null);
        this.type = HeroType.CHEVALIER;
        this.speed = 5.0; 
    }

    private int life = 100;

    public static Hero getInstance() {
        if (instance == null) {
            synchronized (Hero.class) {
                if (instance == null) {
                    instance = new Hero();
                }
            }
        }
        return instance;
    }

    public void setType(HeroType type) {
        this.type = type;
        String filename = "chevalier.png";
        
        // Largeur ajustée pour tes images (les persos sont fins !)
        int largeurPerso = 24; 

        switch (type) {
            case CHEVALIER:
                this.speed = 5.0;
                filename = "chevalier.png"; 
                break;
            case MAGICIEN:
                this.speed = 10.0;
                filename = "magicien.png"; 
                break;
            case LUTIN:
                this.speed = 20.0;
                filename = "lutin.png";
                break;
        }

        try {
            System.out.println("Chargement de " + filename + " avec largeur " + largeurPerso);
            
            // On utilise 24 en largeur, mais on garde 32 en hauteur
            TileManager tmHero = new TileManager(largeurPerso, 32, filename); 
            
            // On prend l'image du milieu (index 1)
            this.setImage(tmHero.getTile(1, 2)); 
            
        } catch (Exception e) {
            System.out.println("Problème avec l'image : " + filename);
            e.printStackTrace();
        }
    }
    
    public HeroType getType() { return type; }
    public double getSpeed() { return this.speed; }
    public int getLife() { return life; }
    public double getX() { return this.x; }
    public double getY() { return this.y; }

    public void takeDamage(int damage) {
        if (this.type == HeroType.CHEVALIER) {
            damage = damage / 2; 
        }
        this.life -= damage;
        if (this.life < 0) this.life = 0;
        System.out.println("Aïe ! (" + type + ") Vie : " + life);
    }

    public void takePoint(int point) {
        this.life += point;
        if (this.life > 100) this.life = 100;
    }

    public void respawn() {
        this.life = 100;
        this.x = 300; 
        this.y = 400;
        if (getHitBox() != null) getHitBox().setPosition(x, y);
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Orientation getOrientation() {
        return orientation;
    }
}