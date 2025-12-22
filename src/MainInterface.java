import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.JOptionPane; // Pour afficher les erreurs
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

public class MainInterface extends JFrame {
    
    // --- ATTRIBUTS DE CLASSE (Accessibles partout) ---
    private GameRender renderPanel;
    private AudioPlayer player;

    public MainInterface(Dungeon dungeon, Hero hero, TileManager tm, AudioPlayer player) {
        super("Mon Super TP Java - Donjon");
        this.player = player; 
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(800, 600));
        this.setLocationRelativeTo(null); 
        
        // On initialise le panneau de rendu ici
        this.renderPanel = new GameRender(dungeon, hero, tm);
        this.setContentPane(renderPanel);

        // --- SOURIS ---
        renderPanel.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) { }
            @Override public void mousePressed(MouseEvent e) { 
                int x = e.getX();
                int y = e.getY();
                
                // Gestion du clic dans le MENU
                if (renderPanel.getState() == GameRender.State.MENU) {
                    // Sélection des têtes
                    if (y >= 250 && y <= 314) {
                        if (x >= 200 && x <= 264) renderPanel.setSelectedHeroIndex(0);
                        else if (x >= 368 && x <= 432) renderPanel.setSelectedHeroIndex(1);
                        else if (x >= 536 && x <= 600) renderPanel.setSelectedHeroIndex(2);
                        renderPanel.repaint();
                    }

                    // Bouton JOUER
                    if (x >= 300 && x <= 500 && y >= 400 && y <= 460) {
                        lancerLeJeu(hero, tm);
                    }
                }
                
                // Gestion du clic GAME OVER
                else if (renderPanel.getState() == GameRender.State.GAME_OVER) {
                    if (x >= 300 && x <= 500 && y >= 350 && y <= 410) {
                        recommencerJeu(hero, dungeon);
                    }
                }
            }
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });

        // --- CLAVIER ---
        this.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) { }
            @Override public void keyReleased(KeyEvent e) { }
            @Override public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                
                // --- NAVIGATION MENU ---
                if (renderPanel.getState() == GameRender.State.MENU) {
                    if (key == KeyEvent.VK_RIGHT) {
                        int index = renderPanel.getSelectedHeroIndex() + 1;
                        if (index > 2) index = 0; 
                        renderPanel.setSelectedHeroIndex(index);
                    }
                    else if (key == KeyEvent.VK_LEFT) {
                        int index = renderPanel.getSelectedHeroIndex() - 1;
                        if (index < 0) index = 2; 
                        renderPanel.setSelectedHeroIndex(index);
                    }
                    else if (key == KeyEvent.VK_ENTER) {
                        lancerLeJeu(hero, tm);
                    }
                    renderPanel.repaint();
                    return; 
                }

                // --- JEU EN COURS ---
                if (renderPanel.getState() == GameRender.State.PLAY && hero.getLife() > 0) {
                    double speed = 10.0; 
                    switch (key) {
                        case KeyEvent.VK_LEFT:  hero.moveIfPossible(-speed, 0, dungeon); break;
                        case KeyEvent.VK_RIGHT: hero.moveIfPossible(speed, 0, dungeon); break;
                        case KeyEvent.VK_UP:    hero.moveIfPossible(0, -speed, dungeon); break;
                        case KeyEvent.VK_DOWN:  hero.moveIfPossible(0, speed, dungeon); break;
                        case KeyEvent.VK_PLUS: case KeyEvent.VK_ADD: player.adjustVolume(2.0f); break; 
                        case KeyEvent.VK_MINUS: case KeyEvent.VK_SUBTRACT: player.adjustVolume(-2.0f); break; 
                    } 
                    renderPanel.repaint();
                }
            }
        });

        // --- TIMER (Boucle de jeu) ---
        Timer timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (renderPanel.getState() != GameRender.State.PLAY) return;

                if (hero.getLife() <= 0) {
                    renderPanel.setState(GameRender.State.GAME_OVER); 
                    renderPanel.repaint();
                    return; 
                }

                // Logique du jeu 
                int heroX = (int) hero.getX() / 32;
                int heroY = (int) hero.getY() / 32;
                char currentTile = dungeon.getTileChar(heroX, heroY);

                if (currentTile == 'X') hero.takeDamage(5);
                if (currentTile == 'L') hero.takeDamage(3);
                if (currentTile == 'G') hero.takePoint(1);

                if (currentTile == 'N') {
                    dungeon.loadLevel("level2.txt");
                    hero.setPosition(80, 32); 
                    if (hero.getHitBox() != null) hero.getHitBox().setPosition(32, 32);
                }

                if (currentTile == 'P') {
                    dungeon.loadLevel("level1.txt");
                    hero.setPosition(300, 70); 
                    if (hero.getHitBox() != null) hero.getHitBox().setPosition(600, 400);
                }
                
                if (currentTile == 'E') {
                    renderPanel.setState(GameRender.State.VICTORY);
                }

                renderPanel.repaint();
            }
        });
        timer.start();
        
        this.setVisible(true);
    }

    // Méthode helper pour lancer le jeu (évite de dupliquer le code)
    private void lancerLeJeu(Hero hero, TileManager tm) {
        int choix = renderPanel.getSelectedHeroIndex();
        
        // Vérification pour éviter le crash si l'image est nulle
        java.awt.Image img = null;
        if (choix == 0) img = tm.getTile(2, 4); 
        if (choix == 1) img = tm.getTile(1, 4); 
        if (choix == 2) img = tm.getTile(3, 4);
        
        if (img != null) {
            hero.setImage(img);
        } else {
            System.out.println("Attention : Image du héros introuvable !");
        }
        
        player.playMusic("game.wav"); 
        renderPanel.setState(GameRender.State.PLAY);
        renderPanel.repaint();
    }

    // Méthode helper pour recommencer
    private void recommencerJeu(Hero hero, Dungeon dungeon) {
        hero.respawn(); 
        dungeon.loadLevel("level1.txt");
        player.playMusic("game.wav"); 
        renderPanel.setState(GameRender.State.PLAY); 
        renderPanel.repaint();
    }

    // --- MAIN AVEC GESTION D'ERREUR ---
    public static void main(String[] args) {
        try {
            // Vérification basique des fichiers avant de lancer
            File f = new File("tileSet.png");
            if (!f.exists()) {
                System.out.println("ATTENTION: tileSet.png est introuvable à la racine du projet !");
            }

            TileManager tm = new TileManager(32, 32, "tileSet.png");
            Dungeon dungeon = new Dungeon("level1.txt", tm); 
            Hero hero = Hero.getInstance();
            
            hero.setPosition(300, 400); 

            AudioPlayer player = new AudioPlayer();
            player.playMusic("intro.wav"); 

            new MainInterface(dungeon, hero, tm, player);
            
        } catch (Exception e) {
            // Si ça plante, une fenêtre va s'ouvrir pour vous dire pourquoi
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur fatale au lancement :\n" + e.getMessage());
        }
    }
}