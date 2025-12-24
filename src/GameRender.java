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
    
    // Images pour le menu
    private Image imgChevalier;
    private Image imgMagicien;
    private Image imgLutin;

    public enum State { MENU, PLAY, GAME_OVER, VICTORY }
    private State state = State.MENU;

    private int selectedHeroIndex = 0; 
    
    // Variables pour les FPS et le temps
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
            // Chargement des images pour le menu
            this.imgChevalier = ImageIO.read(new File("chevalier.png"));
            this.imgMagicien = ImageIO.read(new File("magicien.png"));
            this.imgLutin = ImageIO.read(new File("lutin.png"));
        } catch (Exception e) {
            System.out.println("Erreur chargement images menu : " + e.getMessage());
        }
    }

    // --- GETTERS ET SETTERS ---
    public void setState(State state) {
        this.state = state;
        if (state == State.PLAY) this.startTime = System.currentTimeMillis();
    }

    public State getState() { return state; }

    public void setSelectedHeroIndex(int index) {
        this.selectedHeroIndex = index;
    }
    
    public int getSelectedHeroIndex() { return selectedHeroIndex; }
    
    public Dungeon getDungeon() { return this.dungeon; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // --- 1. DESSIN DU MENU (VOS RÉGLAGES PRÉCIS) ---
        if (state == State.MENU) {
            // Fond du menu (Bleu nuit sympa)
            g.setColor(new Color(40, 40, 60));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("CHOISIS TON HÉROS", 200, 100);
            
            int yPos = 180;   
            int size = 120;   
            int space = 180;  
            int xStart = 130; 

            // CHEVALIER
            int chevX = 76; 
            int chevY = 10; 
            int chevSize = 100; 
            if (imgChevalier != null) {
                g.drawImage(imgChevalier, xStart, yPos, xStart + size, yPos + size, chevX, chevY, chevX + chevSize, chevY + chevSize, null);
            }

            // MAGICIEN
            int magX = 72; 
            int magY = 20; 
            int magSize = 90; 
            if (imgMagicien != null) {
                g.drawImage(imgMagicien, xStart + space, yPos, xStart + space + size, yPos + size, magX, magY, magX + magSize, magY + magSize, null);
            }

            // LUTIN
            int lutX = 72; 
            int lutY = 3; 
            int lutSize = 80; 
            if (imgLutin != null) {
                g.drawImage(imgLutin, xStart + space * 2, yPos, xStart + space * 2 + size, yPos + size, lutX, lutY, lutX + lutSize, lutY + lutSize, null);
            }

            // Cadre de sélection
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(5)); 
            int selectX = xStart + (selectedHeroIndex * space);
            g.drawRect(selectX - 5, yPos - 5, size + 10, size + 10);

            // Bouton Jouer
            g.setColor(new Color(0, 200, 0)); // Vert bouton
            g.fillRect(300, 450, 200, 60);
            g.setColor(Color.WHITE);
            g.drawRect(300, 450, 200, 60);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("JOUER", 345, 490);
            
            g.setFont(new Font("Arial", Font.ITALIC, 14));
            g.drawString("Choisit ton héroine", 260, 400);
            return; 
        }

        // ============================================================
        // 2. DESSIN DU JEU
        // ============================================================
        
        // Calcul Caméra
        int centerX = this.getWidth() / 2;
        int centerY = this.getHeight() / 2;
        double camX = (hero.getX() + 16) - centerX;
        double camY = (hero.getY() + 16) - centerY;

        g2d.translate(-camX, -camY);

        // A. FOND (MIEUX QU'AVANT)
        // On dessine un grand rectangle bleu sombre qui couvre la caméra
        g.setColor(new Color(20, 20, 30)); 
        g.fillRect((int)camX, (int)camY, getWidth(), getHeight());

        // B. MONDE (SANS ERREUR DRAW)
        for (Things thing : dungeon.getListThings()) {
            g.drawImage(thing.getImage(), (int)thing.x, (int)thing.y, null);
        }
        g.drawImage(hero.getImage(), (int)hero.x, (int)hero.y, null);
        
        // C. RETOUR A LA NORMALE (HUD FIXE)
        g2d.translate(camX, camY);
        
        // ============================================================
        // 3. INTERFACE (HUD AMÉLIORÉ)
        // ============================================================

        // --- BARRE DE VIE DYNAMIQUE ---
        int barX = 20;
        int barY = 20;
        int maxBarW = 200;
        int curBarW = (int) ((hero.getLife() / 100.0) * maxBarW);
        if (curBarW < 0) curBarW = 0;

        // Fond gris foncé de la barre
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, maxBarW, 20);

        // Couleur selon les PV
        if (hero.getLife() > 50) g.setColor(new Color(0, 200, 0)); // Vert
        else if (hero.getLife() > 20) g.setColor(Color.ORANGE);
        else g.setColor(Color.RED);

        // Jauge
        g.fillRect(barX, barY, curBarW, 20);
        
        // Contour Blanc
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, maxBarW, 20);
        
        // Texte
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(hero.getLife() + " PV", barX + 210, barY + 15);

        // --- ARME EQUIPÉE (EN HAUT A GAUCHE) ---
        if (hero.hasWeapon()) {
            // Petit fond semi-transparent pour l'arme
            g.setColor(new Color(0, 0, 0, 150)); 
            g.fillRect(20, 50, 40, 40);
            g.setColor(Color.WHITE); 
            g.drawRect(20, 50, 40, 40);
            
            // On récupère la bonne image selon le héros
            Image weaponImg = null;
            if (hero.getType() == HeroType.CHEVALIER) weaponImg = tileManager.getTile(3, 8); // Epée
            if (hero.getType() == HeroType.MAGICIEN)  weaponImg = tileManager.getTile(8, 7); // Baguette
            if (hero.getType() == HeroType.LUTIN)     weaponImg = tileManager.getTile(7, 9); // Tornade
            
            if (weaponImg != null) {
                g.drawImage(weaponImg, 24, 54, 32, 32, null);
            }
        }

        // --- FPS & CHRONO ---
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

        // ============================================================
        // 4. ÉCRANS DE FIN
        // ============================================================
        if (state == State.GAME_OVER) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, 800, 600);
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("GAME OVER", 220, 250);
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