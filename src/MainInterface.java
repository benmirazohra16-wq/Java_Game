import javax.swing.JFrame;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList; 

public class MainInterface extends JFrame {
    private GameRender renderPanel;
    private AudioPlayer player;
    private ArrayList<Tornado> activeTornados = new ArrayList<>();

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
            public void mouseClicked(MouseEvent e) { }
            public void mousePressed(MouseEvent e) { 
                int x = e.getX(); int y = e.getY();
                if (renderPanel.getState() == GameRender.State.MENU) {
                    if (y >= 180 && y <= 300) {
                        renderPanel.isTypingName = false;
                        if (x >= 130 && x <= 250) renderPanel.setSelectedHeroIndex(0);
                        else if (x >= 310 && x <= 430) renderPanel.setSelectedHeroIndex(1);
                        else if (x >= 490 && x <= 610) renderPanel.setSelectedHeroIndex(2);
                        renderPanel.repaint();
                    }
                    if (x >= 300 && x <= 500 && y >= 380 && y <= 430) { renderPanel.isTypingName = true; renderPanel.repaint(); }
                    else if (y < 380 || y > 430 || x < 300 || x > 500) { renderPanel.isTypingName = false; renderPanel.repaint(); }
                    if (x >= 300 && x <= 500 && y >= 450 && y <= 510) lancerLeJeu(hero, tm);
                }
                else if (renderPanel.getState() == GameRender.State.GAME_OVER) {
                    if (x >= 300 && x <= 500 && y >= 350 && y <= 410) recommencerJeu(hero, dungeon);
                }
            }
            public void mouseReleased(MouseEvent e) {} public void mouseEntered(MouseEvent e) {} public void mouseExited(MouseEvent e) {}
        });

        // --- CLAVIER ---
        this.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) { 
                if (renderPanel.getState() == GameRender.State.MENU && renderPanel.isTypingName) {
                    char c = e.getKeyChar();
                    String currentName = renderPanel.getPlayerName();
                    if (c == '\b') { if (currentName.length() > 0) renderPanel.setPlayerName(currentName.substring(0, currentName.length() - 1)); }
                    else if (Character.isLetterOrDigit(c) || c == ' ') { if (currentName.length() < 12) renderPanel.setPlayerName(currentName + c); }
                    renderPanel.repaint();
                }
            }
            public void keyReleased(KeyEvent e) { }
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (renderPanel.getState() == GameRender.State.MENU) { if (key == KeyEvent.VK_ENTER) lancerLeJeu(hero, tm); return; }
                
                if (renderPanel.getState() == GameRender.State.PLAY && hero.getLife() > 0) {
                    double speed = hero.getSpeed(); 
                    switch (key) {
                        case KeyEvent.VK_LEFT:  hero.setOrientation(Orientation.LEFT);   hero.moveIfPossible(-speed, 0, dungeon); break;
                        case KeyEvent.VK_RIGHT: hero.setOrientation(Orientation.RIGHT);  hero.moveIfPossible(speed, 0, dungeon); break;
                        case KeyEvent.VK_UP:    hero.setOrientation(Orientation.TOP);    hero.moveIfPossible(0, -speed, dungeon); break;
                        case KeyEvent.VK_DOWN:  hero.setOrientation(Orientation.BOTTOM); hero.moveIfPossible(0, speed, dungeon); break;
                        
                        case KeyEvent.VK_SPACE:
                            player.playSound("attack.wav"); 
                            hero.attack(dungeon, tm); // Animation de base

                            if (hero.getType() == HeroType.LUTIN && hero.hasWeapon()) {
                                Tornado t = new Tornado((int)hero.getX(), (int)hero.getY(), tm.getTile(7, 8)); 
                                activeTornados.add(t); dungeon.getListThings().add(t); 
                            }
                            
                            // CHEVALIER : Dégâts bonus + Recul
                            if (hero.getType() == HeroType.CHEVALIER && hero.hasWeapon()) {
                                for (int i = dungeon.getListThings().size() - 1; i >= 0; i--) {
                                    Things th = dungeon.getListThings().get(i);
                                    if (th instanceof Monster) {
                                        double dist = Math.sqrt(Math.pow(th.x - hero.getX(), 2) + Math.pow(th.y - hero.getY(), 2));
                                        if (dist <= 64) { 
                                            // 1 dégât bonus (Total = 2 dégâts)
                                            if (((Monster)th).takeDamage(1, hero.getX(), hero.getY())) {
                                                dungeon.getListThings().remove(i); 
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                            
                        case KeyEvent.VK_0: case KeyEvent.VK_NUMPAD0: hero.tryToHeal(); break;
                        case KeyEvent.VK_PLUS: case KeyEvent.VK_ADD: player.adjustVolume(2.0f); break; 
                        case KeyEvent.VK_MINUS: case KeyEvent.VK_SUBTRACT: player.adjustVolume(-2.0f); break; 
                    } 
                    renderPanel.repaint();
                }
            }
        });

        // --- TIMER ---
        Timer timer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (renderPanel.getState() != GameRender.State.PLAY) { renderPanel.repaint(); return; }
                if (hero.getLife() <= 0) { renderPanel.setState(GameRender.State.GAME_OVER); renderPanel.repaint(); return; }

                // MONSTRES
                for (Things thing : dungeon.getListThings()) {
                    if (thing instanceof Monster) {
                        Monster m = (Monster) thing;
                        m.update(dungeon, hero); 
                        if (m.getHitBox().intersect(hero.getHitBox())) hero.takeDamage(2); 
                    }
                }

                // MISSILES
                for (int i = dungeon.getListThings().size() - 1; i >= 0; i--) {
                    Things thing = dungeon.getListThings().get(i);
                    if (thing instanceof Effect) { if (((Effect) thing).isExpired()) dungeon.getListThings().remove(i); }
                    else if (thing instanceof Missile) {
                        Missile m = (Missile) thing;
                        m.move(dungeon); 
                        if (m.isExpired() || m.getDistanceTraveled() > 400) { dungeon.getListThings().remove(i); continue; }

                        boolean hit = false;
                        for (int j = dungeon.getListThings().size() - 1; j >= 0; j--) {
                            if (dungeon.getListThings().get(j) instanceof Monster) {
                                Monster monstre = (Monster) dungeon.getListThings().get(j);
                                double distance = Math.sqrt(Math.pow(m.x - monstre.x, 2) + Math.pow(m.y - monstre.y, 2));
                                if (distance < 45) { 
                                    // 3 dégâts (Tue presque instantanément) + Recul
                                    if (monstre.takeDamage(3, m.x, m.y)) { 
                                        dungeon.getListThings().remove(j);
                                        if (j < i) i--; 
                                    }
                                    hit = true;
                                    break;
                                }
                            }
                        }
                        if (hit) dungeon.getListThings().remove(i);
                    }
                }

                // TORNADES
                for (int i = activeTornados.size() - 1; i >= 0; i--) {
                    Tornado t = activeTornados.get(i);
                    if (t.isReadyToExplode()) {
                        dungeon.getListThings().add(new Effect(t.x, t.y, tm.getTile(8, 1), 500));
                        for (int j = dungeon.getListThings().size() - 1; j >= 0; j--) {
                            if (dungeon.getListThings().get(j) instanceof Monster) {
                                Monster m = (Monster) dungeon.getListThings().get(j);
                                double dist = Math.sqrt(Math.pow(m.x - t.x, 2) + Math.pow(m.y - t.y, 2));
                                if (dist < 100) { if (m.takeDamage(3, t.x, t.y)) dungeon.getListThings().remove(j); }
                            }
                        }
                        if (Math.sqrt(Math.pow(hero.getX() - t.x, 2) + Math.pow(hero.getY() - t.y, 2)) < 40) hero.takeDamage(20);
                        dungeon.getListThings().remove(t); activeTornados.remove(i);
                    }
                }

                // ITEMS
                int hx = (int) (hero.getX() + 12) / 32; int hy = (int) (hero.getY() + 16) / 32;
                char tile = dungeon.getTileChar(hx, hy);
                if (tile == 'O') { player.playSound("object.wav"); hero.setHasWeapon(true); dungeon.removeThing(hx, hy); }
                if (tile == 'L') hero.takeDamage(3); 
                if (tile == 'G') { player.playSound("bell.wav"); hero.takePoint(100); dungeon.removeThing(hx, hy); }
                if (tile == 'N') { dungeon.loadLevel("level2.txt"); hero.setPosition(80, 32); }
                if (tile == 'P') { dungeon.loadLevel("level1.txt"); hero.setPosition(300, 70); }
                if (tile == 'B' || tile == 'E') {
                    if (renderPanel.getState() != GameRender.State.VICTORY) {
                        renderPanel.setState(GameRender.State.VICTORY);
                        player.stopMusic();
                        player.playSound("victoire.wav"); 
                    }
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
        renderPanel.getDungeon().respawnListOfThings();
        activeTornados.clear();
        player.playMusic("game.wav"); 
        renderPanel.setState(GameRender.State.PLAY); renderPanel.repaint();
    }

    private void recommencerJeu(Hero hero, Dungeon dungeon) {
        hero.respawn(); activeTornados.clear(); dungeon.loadLevel("level1.txt");
        player.playMusic("game.wav"); renderPanel.setState(GameRender.State.PLAY); renderPanel.repaint();
    }

    public static void main(String[] args) {
        try {
            File f = new File("tileSet.png");
            if (!f.exists()) System.out.println("ATTENTION: tileSet.png est introuvable !");
            TileManager tm = new TileManager(32, 32, "tileSet.png");
            Dungeon dungeon = new Dungeon("level1.txt", tm); 
            Hero hero = Hero.getInstance(); hero.setPosition(300, 400); 
            AudioPlayer player = new AudioPlayer(); player.playMusic("intro.wav"); 
            new MainInterface(dungeon, hero, tm, player);
        } catch (Exception e) { e.printStackTrace(); }
    }
}