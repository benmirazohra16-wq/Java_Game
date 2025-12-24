import java.awt.Image;

public class Tornado extends Things {
    private long creationTime;

    public Tornado(int x, int y, Image image) {
        super(x, y, image);
        this.creationTime = System.currentTimeMillis();
    }

    public boolean isReadyToExplode() {
        return (System.currentTimeMillis() - creationTime) >= 3000;
    }
}