import javax.swing.JFrame;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MainInterface extends JFrame {
    
    private AudioPlayer player;

    public MainInterface(Dungeon dungeon, Hero hero, TileManager tm, AudioPlayer player) {
        super("Mon Super TP Java - Donjon"); // J'ai remis votre titre original aussi
        this.player = player; 
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(800, 600));
        this.setLocationRelativeTo(null); 
        
        GameRender renderPanel = new GameRender(dungeon, hero, tm);
        this.setContentPane(renderPanel);

        // --- SOURIS ---
        renderPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) { }
            @Override
            public void mousePressed(MouseEvent e) { 
                int x = e.getX();
                int y = e.getY();
                
                if (renderPanel.getState() == GameRender.State.MENU) {
                    if (y >= 250 && y <= 314) {
                        if (x >= 200 && x <= 264) { renderPanel.setSelectedHeroIndex(0); renderPanel.repaint(); }
                        else if (x >= 368 && x <= 432) { renderPanel.setSelectedHeroIndex(1); renderPanel.repaint(); }
                        else if (x >= 536 && x <= 600) { renderPanel.setSelectedHeroIndex(2); renderPanel.repaint(); }
                    }

                    if (x >= 300 && x <= 500 && y >= 400 && y <= 460) {
                        int choix = renderPanel.getSelectedHeroIndex();
                        if (choix == 0) hero.setImage(tm.getTile(2, 4)); 
                        if (choix == 1) hero.setImage(tm.getTile(1, 4)); 
                        if (choix == 2) hero.setImage(tm.getTile(3, 4)); 
                        
                        player.playMusic("game.wav"); 
                        renderPanel.setState(GameRender.State.PLAY);
                        renderPanel.repaint();
                    }
                }
                
                else if (renderPanel.getState() == GameRender.State.GAME_OVER) {
                    if (x >= 300 && x <= 500 && y >= 350 && y <= 410) {
                        hero.respawn(); 
                        dungeon.loadLevel("level1.txt");
                        player.playMusic("game.wav"); 
                        renderPanel.setState(GameRender.State.PLAY); 
                        renderPanel.repaint();
                    }
                }
            }
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });

        // --- CLAVIER ---
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) { }
            @Override
            public void keyReleased(KeyEvent e) { }
            @Override
            public void keyPressed(KeyEvent e) {
                if (renderPanel.getState() != GameRender.State.PLAY) return;
                
                // Si le jeu est fini (vie=0), on empêche le héros de bouger !
                if (hero.getLife() <= 0) return;

                int key = e.getKeyCode();
                double speed = 10.0; 

                switch (key) {
                    case KeyEvent.VK_LEFT:  hero.moveIfPossible(-speed, 0, dungeon); break;
                    case KeyEvent.VK_RIGHT: hero.moveIfPossible(speed, 0, dungeon); break;
                    case KeyEvent.VK_UP:    hero.moveIfPossible(0, -speed, dungeon); break;
                    case KeyEvent.VK_DOWN:  hero.moveIfPossible(0, speed, dungeon); break;
                    
                    // --- VOLUME (+ et -) ---
                    case KeyEvent.VK_PLUS:       
                    case KeyEvent.VK_ADD:        
                        player.adjustVolume(2.0f); break; 

                    case KeyEvent.VK_MINUS:      
                    case KeyEvent.VK_SUBTRACT:   
                        player.adjustVolume(-2.0f); break; 
                } 
                renderPanel.repaint();
            }
        });

        // --- TIMER (Cerveau du jeu) ---
        Timer timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (renderPanel.getState() != GameRender.State.PLAY) return;

                // Vérification de la perte de tout nos points et donc la fin du jeu : Défaites
                if (hero.getLife() <= 0) {
                    renderPanel.setState(GameRender.State.GAME_OVER); 
                    renderPanel.repaint();
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
                if (currentTile == 'G') {
                    hero.takePoint(1); // Guérison
                }

                // Passage au niveau 2
                if (currentTile == 'N') {
                    dungeon.loadLevel("level2.txt");
                    hero.setPosition(80, 32); 
                    if (hero.getHitBox() != null) hero.getHitBox().setPosition(32, 32);
                    renderPanel.repaint();
                }

                // retour au niveau 1
                if (currentTile == 'P') {
                    dungeon.loadLevel("level1.txt");
                    hero.setPosition(300, 70); 
                    if (hero.getHitBox() != null) hero.getHitBox().setPosition(600, 400);
                    renderPanel.repaint();
                }
                
                // Vérification du contacte avec la porte E et donc de la fin de mission : Victoire
                if (currentTile == 'E') {
                    renderPanel.setState(GameRender.State.VICTORY); // On active l'écran victoire
                    renderPanel.repaint();
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
        // hero.setImage(tm.getTile(2, 4)); // Sera défini par le menu
        
        // Position de départ
        hero.setPosition(300, 400); 

        AudioPlayer player = new AudioPlayer();
        player.playMusic("intro.wav"); 

        new MainInterface(dungeon, hero, tm, player);
    }
}