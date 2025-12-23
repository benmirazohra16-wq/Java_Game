import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

public class MainInterface extends JFrame {
    
    private GameRender renderPanel;
    private AudioPlayer player;

    public MainInterface(Dungeon dungeon, Hero hero, TileManager tm, AudioPlayer player) {
        super("Mon Super TP Java - Donjon");
        this.player = player; 
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(800, 600));
        this.setLocationRelativeTo(null); 
        
        this.renderPanel = new GameRender(dungeon, hero, tm);
        this.setContentPane(renderPanel);

        // --- SOURIS ---
        renderPanel.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) { }
            @Override public void mousePressed(MouseEvent e) { 
                int x = e.getX();
                int y = e.getY();
                
                if (renderPanel.getState() == GameRender.State.MENU) {
                    if (y >= 180 && y <= 300) {
                        if (x >= 130 && x <= 250) { renderPanel.setSelectedHeroIndex(0); renderPanel.repaint(); }
                        else if (x >= 310 && x <= 430) { renderPanel.setSelectedHeroIndex(1); renderPanel.repaint(); }
                        else if (x >= 490 && x <= 610) { renderPanel.setSelectedHeroIndex(2); renderPanel.repaint(); }
                    }
                    if (x >= 300 && x <= 500 && y >= 450 && y <= 510) {
                        lancerLeJeu(hero, tm);
                    }
                }
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
                
                if (renderPanel.getState() == GameRender.State.MENU) {
                    if (key == KeyEvent.VK_ENTER) lancerLeJeu(hero, tm);
                    return; 
                }

                if (renderPanel.getState() == GameRender.State.PLAY && hero.getLife() > 0) {
                    double speed = hero.getSpeed(); 
                    
                    switch (key) {
                        case KeyEvent.VK_LEFT:  hero.setOrientation(Orientation.LEFT);   hero.moveIfPossible(-speed, 0, dungeon); break;
                        case KeyEvent.VK_RIGHT: hero.setOrientation(Orientation.RIGHT);  hero.moveIfPossible(speed, 0, dungeon); break;
                        case KeyEvent.VK_UP:    hero.setOrientation(Orientation.TOP);    hero.moveIfPossible(0, -speed, dungeon); break;
                        case KeyEvent.VK_DOWN:  hero.setOrientation(Orientation.BOTTOM); hero.moveIfPossible(0, speed, dungeon); break;
                        case KeyEvent.VK_PLUS: case KeyEvent.VK_ADD: player.adjustVolume(2.0f); break; 
                        case KeyEvent.VK_MINUS: case KeyEvent.VK_SUBTRACT: player.adjustVolume(-2.0f); break; 
                    } 
                    renderPanel.repaint();
                }
            }
        });

        // --- TIMER ---
        Timer timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (renderPanel.getState() != GameRender.State.PLAY) return;

                if (hero.getLife() <= 0) {
                    renderPanel.setState(GameRender.State.GAME_OVER); 
                    renderPanel.repaint();
                    return; 
                }

                int heroTileX = (int) (hero.getX() + 12) / 32;
                int heroTileY = (int) (hero.getY() + 16) / 32;
                char currentTile = dungeon.getTileChar(heroTileX, heroTileY);

                // --- RAMASSAGE D'OBJET ---
                if (currentTile == 'O') {
                    hero.setHasWeapon(true);
                    System.out.println("Arme récupérée !");
                    dungeon.removeThing(heroTileX, heroTileY);
                }

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
                
                if (currentTile == 'B') {
                    renderPanel.setState(GameRender.State.VICTORY);
                }

                renderPanel.repaint();
            }
        });
        timer.start();
        
        this.setVisible(true);
    }

    private void lancerLeJeu(Hero hero, TileManager tm) {
        int choix = renderPanel.getSelectedHeroIndex();
        if (choix == 0) hero.setType(HeroType.CHEVALIER); 
        if (choix == 1) hero.setType(HeroType.MAGICIEN); 
        if (choix == 2) hero.setType(HeroType.LUTIN);
        
        // IMPORTANT : On recharge les objets pour que le 'O' devienne la bonne arme
        renderPanel.getDungeon().respawnListOfThings();

        player.playMusic("game.wav"); 
        renderPanel.setState(GameRender.State.PLAY);
        renderPanel.repaint();
    }

    private void recommencerJeu(Hero hero, Dungeon dungeon) {
        hero.respawn(); 
        dungeon.loadLevel("level1.txt");
        player.playMusic("game.wav"); 
        renderPanel.setState(GameRender.State.PLAY); 
        renderPanel.repaint();
    }

    public static void main(String[] args) {
        try {
            File f = new File("tileSet.png");
            if (!f.exists()) System.out.println("ATTENTION: tileSet.png est introuvable !");

            TileManager tm = new TileManager(32, 32, "tileSet.png");
            Dungeon dungeon = new Dungeon("level1.txt", tm); 
            Hero hero = Hero.getInstance();
            hero.setPosition(300, 400); 

            AudioPlayer player = new AudioPlayer();
            player.playMusic("intro.wav"); 

            new MainInterface(dungeon, hero, tm, player);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur fatale :\n" + e.getMessage());
        }
    }
}