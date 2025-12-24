import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.BasicStroke;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.imageio.ImageIO;

class Score implements Comparable<Score> {
    String name;
    long time;
    public Score(String name, long time) { this.name = name; this.time = time; }
    @Override public int compareTo(Score other) { return Long.compare(this.time, other.time); }
}

public class GameRender extends JPanel {
    private Dungeon dungeon;
    private Hero hero;
    private TileManager tileManager;
    private String playerName = ""; 
    private ArrayList<Score> topScores;   
    private long finalTime = 0;           
    public boolean isTypingName = false; 
    private Image imgChevalier, imgMagicien, imgLutin;
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
        } catch (Exception e) {}
    }

    private void saveScore(String name, long time) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.txt", true))) {
            writer.write(name + ":" + time);
            writer.newLine();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private ArrayList<Score> getTopScores() {
        ArrayList<Score> scores = new ArrayList<>();
        File file = new File("scores.txt");
        if (!file.exists()) return scores;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    try { scores.add(new Score(parts[0], Long.parseLong(parts[1]))); } catch(Exception e) {}
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        Collections.sort(scores);
        if (scores.size() > 5) return new ArrayList<>(scores.subList(0, 5));
        return scores;
    }

    public void setPlayerName(String name) { this.playerName = name; }
    public String getPlayerName() { return this.playerName; }
    public void setState(State state) {
        if (state == State.VICTORY && this.state != State.VICTORY) {
            this.finalTime = (System.currentTimeMillis() - startTime) / 1000;
            String nomFinal = (playerName.trim().isEmpty()) ? "Anonyme" : playerName;
            saveScore(nomFinal, finalTime);     
            this.topScores = getTopScores();    
        }
        this.state = state;
        if (state == State.PLAY) this.startTime = System.currentTimeMillis();
    }
    public State getState() { return state; }
    public void setSelectedHeroIndex(int index) { this.selectedHeroIndex = index; }
    public int getSelectedHeroIndex() { return selectedHeroIndex; }
    public Dungeon getDungeon() { return this.dungeon; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        if (state == State.MENU) {
            // Fond du menu
            g.setColor(new Color(40, 40, 60)); g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("CHOISIS TON HÉROS", 200, 100);
            
            int yPos = 180; int size = 120; int space = 180; int xStart = 130; 
            
            // 1. DESSINER LES IMAGES
            if(imgChevalier!=null) g.drawImage(imgChevalier, xStart, yPos, xStart+size, yPos+size, 76, 10, 176, 110, null);
            if(imgMagicien!=null) g.drawImage(imgMagicien, xStart+space, yPos, xStart+space+size, yPos+size, 72, 20, 162, 110, null);
            if(imgLutin!=null) g.drawImage(imgLutin, xStart+space*2, yPos, xStart+space*2+size, yPos+size, 72, 3, 152, 83, null);

            // 2. DESSINER LES CADRES BLANCS (NOUVEAU)
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3)); // Épaisseur du trait
            g.drawRect(xStart, yPos, size, size);             // Cadre Chevalier
            g.drawRect(xStart + space, yPos, size, size);     // Cadre Magicien
            g.drawRect(xStart + space*2, yPos, size, size);   // Cadre Lutin

            // 3. DESSINER LA SELECTION (Cadre Jaune plus grand)
            g2d.setColor(Color.YELLOW); 
            g2d.setStroke(new BasicStroke(5)); 
            // On décale de -5 et on agrandit de +10 pour qu'il soit autour du cadre blanc
            g.drawRect(xStart + (selectedHeroIndex * space) - 5, yPos - 5, size + 10, size + 10);

            // Zone Pseudo
            int boxX = 300; int boxY = 380; int boxW = 200; int boxH = 50;
            g.setColor(Color.WHITE); g.fillRect(boxX, boxY, boxW, boxH);
            if (isTypingName) g.setColor(Color.CYAN); else g.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(3)); g.drawRect(boxX, boxY, boxW, boxH);
            g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 20));
            String textToShow = playerName;
            if (isTypingName) textToShow += "|";
            else if (playerName.isEmpty()) { g.setColor(Color.GRAY); textToShow = "  Nom  "; }
            g.drawString(textToShow, boxX + 10, boxY + 32);

            // Bouton Jouer
            g.setColor(new Color(0, 200, 0)); g.fillRect(300, 450, 200, 60);
            g.setColor(Color.WHITE); g.drawRect(300, 450, 200, 60);
            g.setFont(new Font("Arial", Font.BOLD, 30)); g.drawString("JOUER", 345, 490);
            return; 
        }

        // --- RESTE DU JEU (Rien n'a changé ici) ---
        int centerX = getWidth() / 2; int centerY = getHeight() / 2;
        double camX = (hero.getX() + 16) - centerX; double camY = (hero.getY() + 16) - centerY;
        g2d.translate(-camX, -camY);
        g.setColor(new Color(20, 20, 30)); g.fillRect((int)camX, (int)camY, getWidth(), getHeight());

        for (Things thing : dungeon.getListThings()) {
            g.drawImage(thing.getImage(), (int)thing.x, (int)thing.y, null);
            if (thing instanceof Monster) {
                Monster m = (Monster) thing;
                int barWidth = 32; int barHeight = 4;
                int barX = (int) m.x; int barY = (int) m.y - 8;
                g.setColor(Color.BLACK); g.fillRect(barX, barY, barWidth, barHeight);
                g.setColor(Color.RED);
                double ratio = (double) m.getLife() / m.getMaxLife();
                int lifeWidth = (int) (ratio * barWidth);
                if (lifeWidth < 0) lifeWidth = 0;
                g.fillRect(barX, barY, lifeWidth, barHeight);
                g.setColor(new Color(255, 255, 255, 100)); g.drawRect(barX, barY, barWidth, barHeight);
            }
        }
        g.drawImage(hero.getImage(), (int)hero.x, (int)hero.y, null);
        if (!playerName.isEmpty()) {
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(playerName);
            int heroCenter = (int)hero.x + 12; 
            g.drawString(playerName, heroCenter - (textWidth / 2), (int)hero.y - 8);
        }
        g2d.translate(camX, camY);
        
        if(state == State.PLAY || state == State.GAME_OVER) {
            int barX = 20; int barY = 20; int maxBarW = 200;
            int curBarW = (int) ((hero.getLife() / 100.0) * maxBarW);
            if (curBarW < 0) curBarW = 0;
            g.setColor(Color.DARK_GRAY); g.fillRect(barX, barY, maxBarW, 20);
            if (hero.getLife() > 50) g.setColor(new Color(0, 200, 0)); 
            else if (hero.getLife() > 20) g.setColor(Color.ORANGE);    
            else g.setColor(Color.RED);                                
            g.fillRect(barX, barY, curBarW, 20);
            g.setColor(Color.WHITE); g.drawRect(barX, barY, maxBarW, 20);
            g.setFont(new Font("Arial", Font.BOLD, 12)); g.drawString(hero.getLife()+" PV", barX + 210, barY + 15);
            
            if (hero.hasWeapon()) {
                g.setColor(new Color(0, 0, 0, 150)); g.fillRect(20, 50, 40, 40);
                g.setColor(Color.WHITE); g.drawRect(20, 50, 40, 40);
                Image w = null;
                if (hero.getType() == HeroType.CHEVALIER) w = tileManager.getTile(3, 8);
                if (hero.getType() == HeroType.MAGICIEN)  w = tileManager.getTile(8, 7);
                if (hero.getType() == HeroType.LUTIN)     w = tileManager.getTile(7, 8);
                if (w != null) g.drawImage(w, 24, 54, 32, 32, null);
            }
        }

        if (state == State.PLAY) {
            long now = System.currentTimeMillis();
            long duration = (now - startTime) / 1000;
            frames++;
            if (now - lastTime >= 1000) { currentFPS = frames; frames = 0; lastTime = now; }
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 15));
            g.drawString("Temps: " + duration + "s", 650, 30);
            g.drawString("FPS: " + currentFPS, 650, 50);
        }

        if (state == State.GAME_OVER) {
            g.setColor(new Color(0, 0, 0, 150)); g.fillRect(0, 0, 800, 600);
            g.setColor(Color.RED); g.setFont(new Font("Arial", Font.BOLD, 60)); g.drawString("GAME OVER", 220, 250);
            g.setColor(Color.WHITE); g.fillRect(300, 350, 200, 60);
            g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 30)); g.drawString("REJOUER", 325, 390);
        }
        else if (state == State.VICTORY) {
            g.setColor(new Color(0, 0, 50)); g.fillRect(0, 0, 800, 600);
            g.setColor(Color.YELLOW); g.setFont(new Font("Arial", Font.BOLD, 50)); g.drawString("VICTOIRE !", 280, 80);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.PLAIN, 20));
            String displayNom = (playerName.isEmpty()) ? "Anonyme" : playerName;
            g.drawString("Bravo " + displayNom + ", fini en " + finalTime + "s.", 250, 130);
            g.setColor(new Color(255, 255, 255, 30)); g.fillRect(200, 160, 400, 300);
            g.setColor(Color.WHITE); g.drawRect(200, 160, 400, 300);
            g.setFont(new Font("Arial", Font.BOLD, 25)); g.drawString("TOP 5 MEILLEURS TEMPS", 250, 200);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            int yList = 250;
            if (topScores != null && !topScores.isEmpty()) {
                for (int i = 0; i < topScores.size(); i++) {
                    Score s = topScores.get(i);
                    g.drawString((i+1) + ". " + s.name, 250, yList);
                    g.drawString(s.time + " s", 500, yList);
                    yList += 40;
                }
            } else { g.drawString("Aucun score enregistré.", 300, 250); }
        }
    }
}