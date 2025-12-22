//Cette classe nous permettra de gerer tout les attribut de notre jeu : fonction barre de vie, game over, victoire .....

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D; // NÉCESSAIRE POUR LA CAMÉRA
import java.awt.Color;
import java.awt.Font; // Import pour changer la taille du texte
import java.awt.Image; // Pour afficher les images des héros dans le menu
import java.awt.BasicStroke; // Pour épaissir le cadre de sélection

public class GameRender extends JPanel {
    private Dungeon dungeon;
    private Hero hero;
    private TileManager tileManager; // Pour récupérer les images des persos
    
    // on crer d'abord des variable boléenne qui nous permettrons d'activé nos affichages
    // On utilise un Enum pour gérer Menu, Jeu, Game Over, Victoire proprement
    public enum State { MENU, PLAY, GAME_OVER, VICTORY }
    private State state = State.MENU;

    // Index du héros choisi (0, 1 ou 2)
    private int selectedHeroIndex = 0; 

    // --- NOUVEAUX ATTRIBUTS (POUR FPS ET TIMER) ---
    private long startTime;       // Heure de début du jeu
    private int frames = 0;       // Compteur d'images
    private long lastTime = 0;    // Pour le calcul du FPS
    private int currentFPS = 0;   // La valeur affichée

    public GameRender(Dungeon dungeon, Hero hero, TileManager tm) {
        this.dungeon = dungeon;
        this.hero = hero;
        this.tileManager = tm;
        
        // Initialisation des chronomètres
        this.startTime = System.currentTimeMillis();
        this.lastTime = System.currentTimeMillis();
    }

    // Cette methode nous permettra de changer l'état du jeu depuis notre MainInterface
    public void setState(State state) {
        this.state = state;
        // Si on lance le jeu, on reset le timer
        if (state == State.PLAY) {
            this.startTime = System.currentTimeMillis();
        }
    }

    public State getState() {
        return state;
    }

    // Setter pour changer le héros sélectionné (0, 1 ou 2), cela nous permettra de choisir le joueurvoulu avant le début du jeu
    public void setSelectedHeroIndex(int index) {
        this.selectedHeroIndex = index;
    }
    
    // etter pour savoir lequel est choisi quand on lance le jeu
    public int getSelectedHeroIndex() {
        return selectedHeroIndex;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g; // Conversion pour la caméra
        
        // --- 1. FOND NOIR (CORRECTION DU BLANC) ---
        // On peint tout l'écran en noir avant de commencer pour éviter les bordures blanches
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        // Affichage du menu
        if (state == State.MENU) {
            // Le fond est déjà noir grâce au code ci-dessus
            
            // Titre
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("CHOISIS TON HÉROS", 200, 150);
            
            // Menu de selection des trois personnages
            // On récupère 3 images différentes depuis le TileManager
            Image hero1 = tileManager.getTile(2, 4); // Ton héros classique
            Image hero2 = tileManager.getTile(1, 4); // 
            Image hero3 = tileManager.getTile(3, 4); // 

            // On les dessine en plus gros (64x64) pour bien voir
            // Perso 1 (Gauche)
            g.drawImage(hero1, 200, 250, 64, 64, null);
            // Perso 2 (Milieu)
            g.drawImage(hero2, 368, 250, 64, 64, null);
            // Perso 3 (Droite)
            g.drawImage(hero3, 536, 250, 64, 64, null);

            // Cadre de sélection du joueur une fois sélectionner
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(3)); // On met un trait un peu plus épais pour bien voir
            
            // On dessine le cadre autour du bon perso selon selectedHeroIndex
            if (selectedHeroIndex == 0) g.drawRect(195, 245, 74, 74);
            if (selectedHeroIndex == 1) g.drawRect(363, 245, 74, 74);
            if (selectedHeroIndex == 2) g.drawRect(531, 245, 74, 74);

            // Petit texte d'aide pour le clavier
            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("Arial", Font.ITALIC, 15));
            g.drawString("Utilise les flèches <- -> et ENTRÉE", 280, 350);

            // Bouton JOUER
            g.setColor(Color.GRAY);
            g.fillRect(300, 400, 200, 60);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("JOUER", 345, 440);
            return; // On arrête l'affichage ici pour le menu
        }

        // --- GESTION CAMÉRA (Suivi Strict) ---
        // On calcule le centre de l'écran (400, 300)
        int centerX = this.getWidth() / 2;
        int centerY = this.getHeight() / 2;
        
        // On veut que le héros (hero.x, hero.y) soit affiché au centre.
        // On ajoute +16 pour centrer sur le milieu du sprite (32x32)
        double camX = (hero.getX() + 16) - centerX;
        double camY = (hero.getY() + 16) - centerY;

        // On applique le décalage (Translation négative)
        g2d.translate(-camX, -camY);

        // Affichage du jeu grace au "draw"
        // Tout ce qui est dessiné ici bougera avec la caméra
        for (Things thing : dungeon.getListThings()) {
            thing.draw(g);
        }
        hero.draw(g);
        
        // --- FIN CAMÉRA ---
        // On annule le décalage pour dessiner l'interface fixe par-dessus
        g2d.translate(camX, camY);
        
        // Affichage de la barre de vie de notre héro 
        g.setColor(Color.BLACK);// Contours
        g.fillRect(5, 5, 202, 22); 
        g.setColor(Color.RED); // Barre de vie
        int lifeWidth = hero.getLife() * 2;
        if (lifeWidth < 0) lifeWidth = 0;
        g.fillRect(6, 6, lifeWidth, 20); 
        g.setColor(Color.WHITE); // Fond de la barre
        g.drawRect(5, 5, 202, 22);

        // --- AFFICHAGE TIMER & FPS ---
        if (state == State.PLAY) {
            // Calcul du Timer
            long now = System.currentTimeMillis();
            long duration = (now - startTime) / 1000; // Temps en secondes
            
            // Calcul du FPS
            frames++;
            if (now - lastTime >= 1000) { // Si une seconde est passée
                currentFPS = frames;
                frames = 0;
                lastTime = now;
            }

            // Affichage en haut à droite
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 15));
            g.drawString("Temps: " + duration + "s", 650, 30);
            g.drawString("FPS: " + currentFPS, 650, 50);
        }

        // Affichage de l'écran de fin en fonction du status de GameOver et Victory
        if (state == State.GAME_OVER) {
            // Fond semi-transparent noir
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, 800, 600);
            
            // Texte de défaite
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("GAME OVER", 200, 250);

            // Bouton rejouer
            g.setColor(Color.WHITE);
            g.fillRect(300, 350, 200, 60);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("REJOUER", 325, 390);
        }
        else if (state == State.VICTORY) {
            // Fond semi-transparent doré/jaune
            g.setColor(new Color(255, 215, 0, 100));
            g.fillRect(0, 0, 800, 600);
            
            // Texte de victoire
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("VICTOIRE", 230, 300);
        }
    }
}