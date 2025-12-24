import java.awt.Image;

public class Missile extends DynamicThings {
    private double directionX;
    private double directionY;
    private double startX, startY;
    private long creationTime;

    public Missile(int x, int y, Image image, double dx, double dy) {
        super(x, y, image);
        this.directionX = dx;
        this.directionY = dy;
        this.startX = x;
        this.startY = y;
        this.creationTime = System.currentTimeMillis();
        
        if (getHitBox() != null) getHitBox().setSize(24, 24);
    }

    public void move(Dungeon dungeon) {
        moveIfPossible(directionX * 10, directionY * 10, dungeon);
        
        // Mise à jour de la Hitbox
        if (getHitBox() != null) {
            getHitBox().setPosition(this.x, this.y);
        }
    }
    
    public boolean isExpired() {
        return (System.currentTimeMillis() - creationTime) > 2000;
    }

    public double getDistanceTraveled() {
        return Math.sqrt(Math.pow(this.x - startX, 2) + Math.pow(this.y - startY, 2));
    }
}