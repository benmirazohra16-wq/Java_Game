import java.awt.Image;

public class Monster extends DynamicThings {
    private boolean isAggro = false; 
    private double directionY = 1;    
    private double patrolSpeed = 2.0; 
    private double chaseSpeed = 2.5;  
    
    // --- MODIFICATION ICI : 8 PV pour tenir 4 coups ---
    private int maxLife = 8; 
    private int life = 8; 

    // --- VARIABLES DE RECUL ---
    private boolean isKnockback = false;
    private int knockbackTimer = 0;
    private double knockbackX = 0;
    private double knockbackY = 0;

    public Monster(int x, int y, Image image) {
        super(x, y, image);
        if (this.getHitBox() != null) this.getHitBox().setSize(32, 32);
    }

    // Méthode simple (compatibilité)
    public boolean takeDamage(int damage) {
        return takeDamage(damage, 0, 0); 
    }

    // Méthode avec RECUL
    public boolean takeDamage(int damage, double attackerX, double attackerY) {
        this.life -= damage;
        
        if (attackerX != 0 || attackerY != 0) {
            this.isKnockback = true;
            this.knockbackTimer = 5; 
            
            double dx = this.x - attackerX;
            double dy = this.y - attackerY;
            double dist = Math.sqrt(dx*dx + dy*dy);
            
            if (dist > 0) {
                this.knockbackX = (dx / dist) * 10; 
                this.knockbackY = (dy / dist) * 10;
            }
        }
        System.out.println("PV restants : " + this.life);
        return this.life <= 0;
    }

    public int getLife() { return life; }
    public int getMaxLife() { return maxLife; }

    public void update(Dungeon dungeon, Hero hero) {
        if (isKnockback) {
            moveIfPossible(knockbackX, knockbackY, dungeon);
            knockbackTimer--;
            if (knockbackTimer <= 0) isKnockback = false;
            return; 
        }

        double distHero = Math.sqrt(Math.pow(hero.getX() - this.x, 2) + Math.pow(hero.getY() - this.y, 2));

        if (!isAggro) {
            double detectionLine = this.x - 200; 
            boolean crossLine = hero.getX() > detectionLine;
            boolean notTooFar = hero.getX() < this.x + 100;
            boolean sameHeight = Math.abs(hero.getY() - this.y) < 100;
            if (crossLine && notTooFar && sameHeight) this.isAggro = true; 
        }

        if (isAggro) {
            Things target = null;       
            double minDist = distHero;  
            boolean targetIsHero = true;

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

            if (targetIsHero) {
                moveToTarget(hero.getX(), hero.getY(), dungeon);
                if (distHero > 600) isAggro = false; 
            } else {
                if (target != null) moveToTarget(target.x, target.y, dungeon);
            }
        } else {
            double oldY = this.y;
            moveIfPossible(0, directionY * patrolSpeed, dungeon);
            if (Math.abs(this.y - oldY) < 0.1) directionY = -directionY; 
        }
    }
    
    private void moveToTarget(double tx, double ty, Dungeon dungeon) {
        double dx = tx - this.x;
        double dy = ty - this.y;
        double dist = Math.sqrt(dx*dx + dy*dy);
        if (dist > 0) {
            dx = dx / dist;
            dy = dy / dist;
            moveIfPossible(dx * chaseSpeed, dy * chaseSpeed, dungeon);
        }
    }
}