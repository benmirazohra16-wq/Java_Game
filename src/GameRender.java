import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.BasicStroke;
import java.io.File;
import javax.imageio.ImageIO;

public class GameRender extends JPanel {
    private Dungeon dungeon;
    private Hero hero;
    private TileManager tileManager;
    
    private Image imgChevalier;
    private Image imgMagicien;
    private Image imgLutin;

    public enum State { MENU, PLAY, GAME_OVER, VICTORY }
    private State state = State.MENU;

    private int selectedHeroIndex = 0; 
    
    private long startTime;
    private int frames = 0;
    private long lastTime = 0;
    private int currentFPS = 0;

    public GameRender(Dungeon dungeon, Hero hero, TileManager tm) {
        this.dungeon = dungeon;
        this.hero = hero;
        this.tileManager = tm;
        
        this.startTime = System.currentTimeMillis();
        this.lastTime = System.currentTimeMillis();

        try {
            this.imgChevalier = ImageIO.read(new File("chevalier.png"));
            this.imgMagicien = ImageIO.read(new File("magicien.png"));
            this.imgLutin = ImageIO.read(new File("lutin.png"));
        } catch (Exception e) {
            System.out.println("Erreur chargement images menu : " + e.getMessage());
        }
    }

    public void setState(State state) {
        this.state = state;
        if (state == State.PLAY) this.startTime = System.currentTimeMillis();
    }

    public State getState() { return state; }

    public void setSelectedHeroIndex(int index) {
        this.selectedHeroIndex = index;
    }
    
    public int getSelectedHeroIndex() { return selectedHeroIndex; }
    
    // --- IMPORTANT : AJOUT DU GETTER ---
    public Dungeon getDungeon() { return this.dungeon; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        if (state == State.MENU) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("CHOISIS TON HÉROS", 200, 100);
            
            int yPos = 180;   
            int size = 120;   
            int space = 180;  
            int xStart = 130; 

            // 1. CHEVALIER
            int chevX = 76;  
            int chevY = 10;   
            int chevSize = 100; 

            if (imgChevalier != null) {
                g.drawImage(imgChevalier, 
                    xStart, yPos, xStart + size, yPos + size,  
                    chevX, chevY, chevX + chevSize, chevY + chevSize, 
                    null);
            }

            // 2. MAGICIEN
            int magX = 72;
            int magY = 20;    
            int magSize = 90; 

            if (imgMagicien != null) {
                g.drawImage(imgMagicien, 
                    xStart + space, yPos, xStart + space + size, yPos + size, 
                    magX, magY, magX + magSize, magY + magSize, 
                    null);
            }

            // 3. LUTIN
            int lutX = 72;
            int lutY = 10;    
            int lutSize = 80; 

            if (imgLutin != null) {
                g.drawImage(imgLutin, 
                    xStart + space * 2, yPos, xStart + space * 2 + size, yPos + size, 
                    lutX, lutY, lutX + lutSize, lutY + lutSize, 
                    null);
            }

            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(5));

            int selectX = xStart + (selectedHeroIndex * space);
            g.drawRect(selectX - 5, yPos - 5, size + 10, size + 10);

            g.setColor(Color.GRAY);
            g.fillRect(300, 450, 200, 60);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("JOUER", 345, 490);
            
            g.setFont(new Font("Arial", Font.ITALIC, 14));
            g.drawString("Choissisez votre héroine", 280, 400);
            
            return;
        }

        int centerX = this.getWidth() / 2;
        int centerY = this.getHeight() / 2;
        double camX = (hero.getX() + 16) - centerX;
        double camY = (hero.getY() + 16) - centerY;

        g2d.translate(-camX, -camY);

        for (Things thing : dungeon.getListThings()) thing.draw(g);
        hero.draw(g);
        
        g2d.translate(camX, camY);
        
        g.setColor(Color.BLACK); g.fillRect(5, 5, 202, 22); 
        g.setColor(Color.RED);
        int lifeWidth = hero.getLife() * 2;
        if (lifeWidth < 0) lifeWidth = 0;
        g.fillRect(6, 6, lifeWidth, 20); 
        g.setColor(Color.WHITE); g.drawRect(5, 5, 202, 22);

        if (state == State.PLAY) {
            long now = System.currentTimeMillis();
            long duration = (now - startTime) / 1000;
            frames++;
            if (now - lastTime >= 1000) {
                currentFPS = frames;
                frames = 0;
                lastTime = now;
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 15));
            g.drawString("Temps: " + duration + "s", 650, 30);
            g.drawString("FPS: " + currentFPS, 650, 50);
        }

        if (state == State.GAME_OVER) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, 800, 600);
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("GAME OVER", 200, 250);
            g.setColor(Color.WHITE);
            g.fillRect(300, 350, 200, 60);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("REJOUER", 325, 390);
        }
        else if (state == State.VICTORY) {
            g.setColor(new Color(255, 215, 0, 100));
            g.fillRect(0, 0, 800, 600);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("VICTOIRE", 230, 300);
        }
    }
}