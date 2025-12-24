import java.awt.Image;

public class Monster extends DynamicThings {
    private boolean isAggro = false; 
    private double directionY = 1;    
    private double patrolSpeed = 2.0; 
    private double chaseSpeed = 2.5;  
    private int life = 6; 

    public Monster(int x, int y, Image image) {
        super(x, y, image);
        if (this.getHitBox() != null) this.getHitBox().setSize(32, 32);
    }

    public boolean takeDamage(int damage) {
        this.life -= damage;
        System.out.println("Monstre touché ! PV restants : " + this.life);
        return this.life <= 0;
    }

    public void update(Dungeon dungeon, Hero hero) {
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