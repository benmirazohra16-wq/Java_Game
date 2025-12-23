import java.awt.Image;

public final class Hero extends DynamicThings {
    private static volatile Hero instance = null;
    private HeroType type;
    private double speed = 10.0; 
    private Orientation orientation = Orientation.BOTTOM;
    
    private boolean hasWeapon = false; 

    private Image[][] sprites; 
    private int life = 100;

    private Hero() {
        super(0, 0, null);
        this.type = HeroType.CHEVALIER;
        this.speed = 5.0; 
    }

    public static Hero getInstance() {
        if (instance == null) {
            synchronized (Hero.class) {
                if (instance == null) instance = new Hero();
            }
        }
        return instance;
    }

    public void setType(HeroType type) {
        this.type = type;
        String filename = "chevalier.png";
        
        int largeurPerso = 24; 
        int hauteurPerso = 32;

        switch (type) {
            case CHEVALIER: this.speed = 5.0;  filename = "chevalier.png"; break;
            case MAGICIEN:  this.speed = 10.0; filename = "magicien.png"; break;
            case LUTIN:     this.speed = 20.0; filename = "lutin.png"; break;
        }

        try {
            TileManager tmHero = new TileManager(largeurPerso, hauteurPerso, filename); 
            this.sprites = new Image[4][3];

            for(int col=0; col<3; col++) {
                sprites[0][col] = tmHero.getTile(col, 0); // Dos
                sprites[1][col] = tmHero.getTile(col, 3); // Gauche
                sprites[2][col] = tmHero.getTile(col, 2); // Face
                sprites[3][col] = tmHero.getTile(col, 1); // Droite
            }
            this.setImage(sprites[2][1]); 
            
            // Hitbox réduite
            if (this.getHitBox() != null) {
                this.getHitBox().setSize(14, 10);
            }

        } catch (Exception e) {
            System.out.println("Problème image hero : " + filename);
        }
    }

    @Override
    public void setPosition(double x, double y) {
        this.x = (int)x;
        this.y = (int)y;
        
        if (getHitBox() != null) {
            // Centrage de la hitbox aux pieds
            getHitBox().setPosition(x + 5, y + 22);
        }
    }
    
    public void setHasWeapon(boolean has) { this.hasWeapon = has; }
    public boolean hasWeapon() { return this.hasWeapon; }

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
    
    public HeroType getType() { return type; }
    public double getSpeed() { return this.speed; }
    public int getLife() { return life; }
    public double getX() { return this.x; }
    public double getY() { return this.y; }

    public void takeDamage(int damage) {
        if (this.type == HeroType.CHEVALIER) damage = damage / 2; 
        this.life -= damage;
        if (this.life < 0) this.life = 0;
    }

    public void takePoint(int point) {
        this.life += point;
        if (this.life > 100) this.life = 100;
    }

    public void respawn() {
        this.life = 100;
        this.x = 300; 
        this.y = 400;
        this.hasWeapon = false;
        this.orientation = Orientation.BOTTOM;
        if(sprites != null) this.setImage(sprites[2][1]);
        if (getHitBox() != null) getHitBox().setPosition(x, y);
    }
}