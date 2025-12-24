public class HitBox {
    private double x;
    private double y;
    private double width;
    private double height;

    public HitBox(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public boolean intersect(HitBox other) {
        // Simple détection de collision AABB (Axis-Aligned Bounding Box)
        return (this.x < other.x + other.width &&
                this.x + this.width > other.x &&
                this.y < other.y + other.height &&
                this.y + this.height > other.y);
    }
    
    // Getters si besoin
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}