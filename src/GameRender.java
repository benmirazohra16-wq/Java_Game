//Cette classe nous permettra de gerer tout les attribut de notre jeu : fonction barre de vie, game over, victoire .....

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font; // Import pour changer la taille du texte

public class GameRender extends JPanel {
    private Dungeon dungeon;
    private Hero hero;
    
    // on crer d'abord des variable boléenne qui nous permettrons d'activé nos affichages
    // Affichage : Game Over 
    // Affiche Victoire
    private boolean isGameOver = false;
    private boolean isVictory = false;

    public GameRender(Dungeon dungeon, Hero hero) {
        this.dungeon = dungeon;
        this.hero = hero;
    }

    // Cette methode nous permettra de changer l'état du jeu depuis notre MainInterface selon le staut de GameOver
    public void setGameOver(boolean status) {
        this.isGameOver = status;
    }
    // Cette methode nous permettra de changer l'état du jeu depuis notre MainInterface selon le staut de Victory
    public void setVictory(boolean status) {
        this.isVictory = status;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Affichage du jeu grace au "draw"
        for (Things thing : dungeon.getListThings()) {
            thing.draw(g);
        }
        hero.draw(g);
        
        // Affichage de la barre de vie de notre héro 
        g.setColor(Color.BLACK);// Contours
        g.fillRect(19, 19, 202, 22); 
        g.setColor(Color.RED); // Barre de vie
        int lifeWidth = hero.getLife() * 2;
        if (lifeWidth < 0) lifeWidth = 0;
        g.fillRect(20, 20, lifeWidth, 20); 
        g.setColor(Color.WHITE); // Fond de la barre
        g.drawRect(19, 19, 202, 22);

        // Affichage de l'écran de fin en fonction du status de GameOver et Victory
        if (isGameOver) {
            // Fond semi-transparent noir
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, 800, 600);
            
            // Texte de défaite
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("GAME OVER : Vous êtes mort", 200, 300);
        }
        else if (isVictory) {
            // Fond semi-transparent doré/jaune
            g.setColor(new Color(255, 215, 0, 100));
            g.fillRect(0, 0, 800, 600);
            
            // Texte de victoire
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("VICTOIRE ! : Niveau 1 passé", 230, 300);
        }
    }
}