import java.awt.Image;

public class Effect extends Things {
    private long creationTime;
    private int lifeTime; 

    // On accepte des DOUBLE pour x et y maintenant
    public Effect(double x, double y, Image image, int lifeTime) {
        super(x, y, image);
        this.creationTime = System.currentTimeMillis();
        this.lifeTime = lifeTime;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - creationTime) > lifeTime;
    }
}