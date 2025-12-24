// Importations pour gérer les graphismes (couleurs, polices, images, formes).
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.BasicStroke;
import java.io.*; // Pour lire/écrire les scores
import java.util.ArrayList;
import java.util.Collections; // Pour trier les scores
import javax.imageio.ImageIO; // Pour charger les images PNG

// Petite classe interne pour stocker un Score (Nom + Temps)
class Score implements Comparable<Score> {
    String name;
    long time;
    public Score(String name, long time) { this.name = name; this.time = time; }
    // Permet de trier les scores du plus petit temps au plus grand.
    @Override public int compareTo(Score other) { return Long.compare(this.time, other.time); }
}

// Classe principale d'affichage qui hérite de JPanel (une surface de dessin).
public class GameRender extends JPanel {
    
    // Références vers les éléments du jeu à dessiner.
    private Dungeon dungeon;
    private Hero hero;
    private TileManager tileManager;
    
    // Gestion du joueur et des scores.
    private String playerName = ""; 
    private ArrayList<Score> topScores;   
    private long finalTime = 0;           
    public boolean isTypingName = false; // Est-ce qu'on est en train d'écrire notre nom ?
    
    // Images pour le menu de sélection.
    private Image imgChevalier, imgMagicien, imgLutin;
    
    // Les différents états possibles du jeu (Machine à états).
    public enum State { MENU, PLAY, GAME_OVER, VICTORY }
    private State state = State.MENU; // On commence par le menu.
    
    private int selectedHeroIndex = 0; // Quel héros est sélectionné (0=Chevalier, etc.)
    
    // Variables pour le temps et les FPS (Frames Per Second).
    private long startTime;
    private int frames = 0;
    private long lastTime = 0;
    private int currentFPS = 0;

    // Constructeur : Initialise les liens et charge les images du menu.
    public GameRender(Dungeon dungeon, Hero hero, TileManager tm) {
        this.dungeon = dungeon;
        this.hero = hero;
        this.tileManager = tm;
        this.startTime = System.currentTimeMillis();
        this.lastTime = System.currentTimeMillis();
        try {
            // Chargement des images des héros pour le menu.
            this.imgChevalier = ImageIO.read(new File("chevalier.png"));
            this.imgMagicien = ImageIO.read(new File("magicien.png"));
            this.imgLutin = ImageIO.read(new File("lutin.png"));
        } catch (Exception e) {}
    }

    // Méthode pour écrire le score dans un fichier texte.
    private void saveScore(String name, long time) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.txt", true))) {
            writer.write(name + ":" + time);
            writer.newLine(); // Nouvelle ligne
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Méthode pour lire et trier les 5 meilleurs scores.
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
        Collections.sort(scores); // Tri du plus rapide au plus lent.
        // On ne garde que les 5 premiers.
        if (scores.size() > 5) return new ArrayList<>(scores.subList(0, 5));
        return scores;
    }

    // Accesseurs et Mutateurs pour gérer le nom et l'état du jeu.
    public void setPlayerName(String name) { this.playerName = name; }
    public String getPlayerName() { return this.playerName; }
    
    public void setState(State state) {
        // Si on vient de gagner, on sauvegarde le score et on charge le top 5.
        if (state == State.VICTORY && this.state != State.VICTORY) {
            this.finalTime = (System.currentTimeMillis() - startTime) / 1000;
            String nomFinal = (playerName.trim().isEmpty()) ? "Anonyme" : playerName;
            saveScore(nomFinal, finalTime);     
            this.topScores = getTopScores();    
        }
        this.state = state;
        // Si on lance le jeu, on reset le chrono.
        if (state == State.PLAY) this.startTime = System.currentTimeMillis();
    }
    public State getState() { return state; }
    public void setSelectedHeroIndex(int index) { this.selectedHeroIndex = index; }
    public int getSelectedHeroIndex() { return selectedHeroIndex; }
    public Dungeon getDungeon() { return this.dungeon; }

    // --- LA MÉTHODE PRINCIPALE DE DESSIN ---
    // C'est ici que tout s'affiche à l'écran. Elle est appelée en boucle par repaint().
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Nettoie l'écran.
        Graphics2D g2d = (Graphics2D) g; // Version plus avancée de Graphics pour les épaisseurs de traits.
        
        // Fond noir par défaut.
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        // --- ÉCRAN : MENU PRINCIPAL ---
        if (state == State.MENU) {
            // Fond bleu nuit
            g.setColor(new Color(40, 40, 60)); g.fillRect(0, 0, getWidth(), getHeight());
            
            // Titre
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("CHOISIS TON HÉROS", 200, 100);
            
            int yPos = 180; int size = 120; int space = 180; int xStart = 130; 
            
            // 1. DESSINER LES IMAGES DES HÉROS
            // Les coordonnées 76, 10, 176, 110 correspondent à la zone de l'image source (sprite sheet) qu'on veut afficher.
            if(imgChevalier!=null) g.drawImage(imgChevalier, xStart, yPos, xStart+size, yPos+size, 76, 10, 176, 110, null);
            if(imgMagicien!=null) g.drawImage(imgMagicien, xStart+space, yPos, xStart+space+size, yPos+size, 72, 20, 162, 110, null);
            if(imgLutin!=null) g.drawImage(imgLutin, xStart+space*2, yPos, xStart+space+size, yPos+size, 72, 3, 152, 83, null);

            // 2. DESSINER LES CADRES BLANCS AUTOUR DES IMAGES
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3)); // Trait épais de 3 pixels
            g.drawRect(xStart, yPos, size, size);             // Cadre Chevalier
            g.drawRect(xStart + space, yPos, size, size);     // Cadre Magicien
            g.drawRect(xStart + space*2, yPos, size, size);   // Cadre Lutin

            // 3. DESSINER LA SÉLECTION (Cadre Jaune)
            // On calcule la position X en fonction de l'index sélectionné (0, 1 ou 2).
            g2d.setColor(Color.YELLOW); 
            g2d.setStroke(new BasicStroke(5)); // Trait très épais
            // On le fait un peu plus grand (-5, +10) pour qu'il entoure le cadre blanc.
            g.drawRect(xStart + (selectedHeroIndex * space) - 5, yPos - 5, size + 10, size + 10);

            // Zone de saisie du Pseudo
            int boxX = 300; int boxY = 380; int boxW = 200; int boxH = 50;
            g.setColor(Color.WHITE); g.fillRect(boxX, boxY, boxW, boxH);
            // Bordure cyan si on écrit, grise sinon.
            if (isTypingName) g.setColor(Color.CYAN); else g.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(3)); g.drawRect(boxX, boxY, boxW, boxH);
            
            // Texte du pseudo
            g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 20));
            String textToShow = playerName;
            if (isTypingName) textToShow += "|"; // Petit curseur clignotant simulé
            else if (playerName.isEmpty()) { g.setColor(Color.GRAY); textToShow = "  Nom  "; } // Placeholder
            g.drawString(textToShow, boxX + 10, boxY + 32);

            // Bouton JOUER
            g.setColor(new Color(0, 200, 0)); g.fillRect(300, 450, 200, 60);
            g.setColor(Color.WHITE); g.drawRect(300, 450, 200, 60);
            g.setFont(new Font("Arial", Font.BOLD, 30)); g.drawString("JOUER", 345, 490);
            return; // On arrête ici pour le menu, pas besoin de dessiner le jeu en dessous.
        }

        // --- ÉCRAN : JEU (PLAY) ---
        // Calcul de la caméra : On veut que le héros soit toujours au centre de l'écran.
        int centerX = getWidth() / 2; int centerY = getHeight() / 2;
        double camX = (hero.getX() + 16) - centerX; double camY = (hero.getY() + 16) - centerY;
        
        // On déplace tout le contexte graphique (la "caméra").
        g2d.translate(-camX, -camY);
        
        // Fond du jeu (gris très foncé)
        g.setColor(new Color(20, 20, 30)); g.fillRect((int)camX, (int)camY, getWidth(), getHeight());

        // On dessine tous les objets (Murs, Sol, Monstres, Items...).
        for (Things thing : dungeon.getListThings()) {
            g.drawImage(thing.getImage(), (int)thing.x, (int)thing.y, null);
            
            // Si c'est un monstre, on dessine sa barre de vie au-dessus de lui.
            if (thing instanceof Monster) {
                Monster m = (Monster) thing;
                int barWidth = 32; int barHeight = 4;
                int barX = (int) m.x; int barY = (int) m.y - 8;
                // Fond noir
                g.setColor(Color.BLACK); g.fillRect(barX, barY, barWidth, barHeight);
                // Vie en rouge
                g.setColor(Color.RED);
                double ratio = (double) m.getLife() / m.getMaxLife();
                int lifeWidth = (int) (ratio * barWidth);
                if (lifeWidth < 0) lifeWidth = 0;
                g.fillRect(barX, barY, lifeWidth, barHeight);
                // Bordure blanche semi-transparente
                g.setColor(new Color(255, 255, 255, 100)); g.drawRect(barX, barY, barWidth, barHeight);
            }
        }
        
        // On dessine le héros par-dessus tout le reste.
        g.drawImage(hero.getImage(), (int)hero.x, (int)hero.y, null);
        
        // On affiche le pseudo du joueur au-dessus du héros.
        if (!playerName.isEmpty()) {
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics(); // Pour centrer le texte
            int textWidth = fm.stringWidth(playerName);
            int heroCenter = (int)hero.x + 12; 
            g.drawString(playerName, heroCenter - (textWidth / 2), (int)hero.y - 8);
        }
        
        // On annule le déplacement de la caméra pour dessiner l'interface fixe (HUD).
        g2d.translate(camX, camY);
        
        // --- INTERFACE UTILISATEUR (HUD) ---
        if(state == State.PLAY || state == State.GAME_OVER) {
            // Barre de vie du héros (en haut à gauche)
            int barX = 20; int barY = 20; int maxBarW = 200;
            int curBarW = (int) ((hero.getLife() / 100.0) * maxBarW);
            if (curBarW < 0) curBarW = 0;
            
            g.setColor(Color.DARK_GRAY); g.fillRect(barX, barY, maxBarW, 20); // Fond
            // Couleur changeante selon la vie (Vert -> Orange -> Rouge)
            if (hero.getLife() > 50) g.setColor(new Color(0, 200, 0)); 
            else if (hero.getLife() > 20) g.setColor(Color.ORANGE);    
            else g.setColor(Color.RED);                                
            g.fillRect(barX, barY, curBarW, 20); // Vie actuelle
            g.setColor(Color.WHITE); g.drawRect(barX, barY, maxBarW, 20); // Bordure
            g.setFont(new Font("Arial", Font.BOLD, 12)); g.drawString(hero.getLife()+" PV", barX + 210, barY + 15);
            
            // Affichage de l'arme équipée
            if (hero.hasWeapon()) {
                g.setColor(new Color(0, 0, 0, 150)); g.fillRect(20, 50, 40, 40); // Case noire
                g.setColor(Color.WHITE); g.drawRect(20, 50, 40, 40); // Bordure blanche
                Image w = null;
                // Sélection de l'image de l'arme selon le héros
                if (hero.getType() == HeroType.CHEVALIER) w = tileManager.getTile(3, 8);
                if (hero.getType() == HeroType.MAGICIEN)  w = tileManager.getTile(8, 7);
                if (hero.getType() == HeroType.LUTIN)     w = tileManager.getTile(7, 8);
                if (w != null) g.drawImage(w, 24, 54, 32, 32, null);
            }
        }

        // --- AFFICHAGE TEMPS & FPS ---
        if (state == State.PLAY) {
            long now = System.currentTimeMillis();
            long duration = (now - startTime) / 1000; // Temps écoulé en secondes
            
            // Calcul basique des FPS
            frames++;
            if (now - lastTime >= 1000) { currentFPS = frames; frames = 0; lastTime = now; }
            
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 15));
            g.drawString("Temps: " + duration + "s", 650, 30);
            g.drawString("FPS: " + currentFPS, 650, 50);
        }

        // --- ÉCRAN : GAME OVER ---
        if (state == State.GAME_OVER) {
            g.setColor(new Color(0, 0, 0, 150)); g.fillRect(0, 0, 800, 600); // Voile noir transparent
            g.setColor(Color.RED); g.setFont(new Font("Arial", Font.BOLD, 60)); g.drawString("GAME OVER", 220, 250);
            // Bouton Rejouer
            g.setColor(Color.WHITE); g.fillRect(300, 350, 200, 60);
            g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 30)); g.drawString("REJOUER", 325, 390);
        }
        
        // --- ÉCRAN : VICTOIRE ---
        else if (state == State.VICTORY) {
            g.setColor(new Color(0, 0, 50)); g.fillRect(0, 0, 800, 600); // Fond bleu foncé
            g.setColor(Color.YELLOW); g.setFont(new Font("Arial", Font.BOLD, 50)); g.drawString("VICTOIRE !", 280, 80);
            
            // Affichage du score personnel
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.PLAIN, 20));
            String displayNom = (playerName.isEmpty()) ? "Anonyme" : playerName;
            g.drawString("Bravo " + displayNom + ", fini en " + finalTime + "s.", 250, 130);
            
            // Affichage du TOP 5 (Tableau des scores)
            g.setColor(new Color(255, 255, 255, 30)); g.fillRect(200, 160, 400, 300); // Fond semi-transparent
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