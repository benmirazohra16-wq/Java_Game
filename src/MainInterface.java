import javax.swing.JFrame;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MainInterface extends JFrame {
    
    public MainInterface(Dungeon dungeon, Hero hero) {
        super("Mon Super TP Java - Donjon");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(800, 600));
        
        GameRender renderPanel = new GameRender(dungeon, hero);
        this.setContentPane(renderPanel);

        // --- CLAVIER ---
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) { }
            @Override
            public void keyReleased(KeyEvent e) { }
            @Override
            public void keyPressed(KeyEvent e) {
                // Si le jeu est fini (vie=0), on empêche le héros de bouger
                if (hero.getLife() <= 0) return;

                int key = e.getKeyCode();
                double speed = 10.0; 

                switch (key) {
                    case KeyEvent.VK_LEFT:  hero.moveIfPossible(-speed, 0, dungeon); break;
                    case KeyEvent.VK_RIGHT: hero.moveIfPossible(speed, 0, dungeon); break;
                    case KeyEvent.VK_UP:    hero.moveIfPossible(0, -speed, dungeon); break;
                    case KeyEvent.VK_DOWN:  hero.moveIfPossible(0, speed, dungeon); break;
                } 
                renderPanel.repaint();
            }
        });

        // La partie suivante permet de lancer le jeu 
        Timer timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                // Vérification de la perte de tout nos points et donc la fin du jeu : Défaites
                if (hero.getLife() <= 0) {
                    renderPanel.setGameOver(true); // On active l'écran de défaite
                    renderPanel.repaint();
                    ((Timer)e.getSource()).stop(); //  on stop le jeu
                    return; 
                }

                // Logique du jeu 
                int heroX = (int) hero.getX() / 32;
                int heroY = (int) hero.getY() / 32;
                char currentTile = dungeon.getTileChar(heroX, heroY);

                // Pièges
                if (currentTile == 'X') {
                    hero.takeDamage(5); // 5 points perdu lorsque l'on rentre en contacte avec des ennemies
                }
                if (currentTile == 'L') {
                    hero.takeDamage(3); // 3 points perdu lorsque l'on rentre en contacte avec de la lave
                }
                
                // Vérification du contacte avec la porte E et donc de la fin de mission : Victoire
                if (currentTile == 'E') {
                    renderPanel.setVictory(true); // On active l'écran victoire
                    renderPanel.repaint();
                    ((Timer)e.getSource()).stop(); // on stop le jeu
                    return;
                }

                renderPanel.repaint();
            }
        });
        timer.start();
        
        this.setVisible(true);
    }
// Instanciation des éléments de notre jeu : perso, dungeon..;
    public static void main(String[] args) {
        TileManager tm = new TileManager(32, 32, "tileSet.png");
        Dungeon dungeon = new Dungeon("level1.txt", tm); 
        Hero hero = Hero.getInstance();
        // Perso choisit
        hero.setImage(tm.getTile(2, 4)); 
        // Position de départ
        hero.setPosition(300, 400); 

        new MainInterface(dungeon, hero);
    }
}