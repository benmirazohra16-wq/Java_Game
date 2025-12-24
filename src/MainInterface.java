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

        renderPanel.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) { }
            public void mousePressed(MouseEvent e) { 
                int x = e.getX(); int y = e.getY();
                if (renderPanel.getState() == GameRender.State.MENU) {
                    if (y >= 180 && y <= 300) {
                        if (x >= 130 && x <= 250) { renderPanel.setSelectedHeroIndex(0); renderPanel.repaint(); }
                        else if (x >= 310 && x <= 430) { renderPanel.setSelectedHeroIndex(1); renderPanel.repaint(); }
                        else if (x >= 490 && x <= 610) { renderPanel.setSelectedHeroIndex(2); renderPanel.repaint(); }
                    }
                    if (x >= 300 && x <= 500 && y >= 450 && y <= 510) lancerLeJeu(hero, tm);
                }
                else if (renderPanel.getState() == GameRender.State.GAME_OVER) {
                    if (x >= 300 && x <= 500 && y >= 350 && y <= 410) recommencerJeu(hero, dungeon);
                }
            }
            public void mouseReleased(MouseEvent e) {} public void mouseEntered(MouseEvent e) {} public void mouseExited(MouseEvent e) {}
        });

        this.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) { } public void keyReleased(KeyEvent e) { }
            public void keyPressed(KeyEvent e) {
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
                        case KeyEvent.VK_SPACE:
                            if (hero.getType() == HeroType.LUTIN && hero.hasWeapon()) {
                                Tornado t = new Tornado((int)hero.getX(), (int)hero.getY(), tm.getTile(7, 9)); 
                                activeTornados.add(t); dungeon.getListThings().add(t); 
                            } else { hero.attack(dungeon, tm); }
                            break;
                        case KeyEvent.VK_0: case KeyEvent.VK_NUMPAD0: hero.tryToHeal(); break;
                        case KeyEvent.VK_PLUS: case KeyEvent.VK_ADD: player.adjustVolume(2.0f); break; 
                        case KeyEvent.VK_MINUS: case KeyEvent.VK_SUBTRACT: player.adjustVolume(-2.0f); break; 
                    } 
                    renderPanel.repaint();
                }
            }
        });

        Timer timer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (renderPanel.getState() != GameRender.State.PLAY) return;
                if (hero.getLife() <= 0) { renderPanel.setState(GameRender.State.GAME_OVER); renderPanel.repaint(); return; }

                for (Things thing : dungeon.getListThings()) {
                    if (thing instanceof Monster) {
                        Monster m = (Monster) thing;
                        m.update(dungeon, hero); 
                        if (m.getHitBox().intersect(hero.getHitBox())) hero.takeDamage(2); 
                    }
                }

                for (int i = dungeon.getListThings().size() - 1; i >= 0; i--) {
                    Things thing = dungeon.getListThings().get(i);
                    if (thing instanceof Effect) { if (((Effect) thing).isExpired()) dungeon.getListThings().remove(i); }
                    else if (thing instanceof Missile) {
                        Missile m = (Missile) thing;
                        m.move(dungeon); 
                        double dist = m.getDistanceTraveled();
                        if (dist > 128) { dungeon.getListThings().remove(i); continue; }
                        boolean hit = false;
                        for (int j = dungeon.getListThings().size() - 1; j >= 0; j--) {
                            if (dungeon.getListThings().get(j) instanceof Monster) {
                                if (m.getHitBox().intersect(((Monster) dungeon.getListThings().get(j)).getHitBox())) {
                                    if (dist >= 32) { if (((Monster)dungeon.getListThings().get(j)).takeDamage(2)) dungeon.getListThings().remove(j); }
                                    hit = true;
                                }
                            }
                        }
                        if (hit || m.isExpired()) dungeon.getListThings().remove(i);
                    }
                }

                for (int i = activeTornados.size() - 1; i >= 0; i--) {
                    Tornado t = activeTornados.get(i);
                    if (t.isReadyToExplode()) {
                        dungeon.getListThings().add(new Effect(t.x, t.y, tm.getTile(8, 1), 500));
                        for (int j = dungeon.getListThings().size() - 1; j >= 0; j--) {
                            if (dungeon.getListThings().get(j) instanceof Monster) {
                                Monster m = (Monster) dungeon.getListThings().get(j);
                                double dist = Math.sqrt(Math.pow(m.x - t.x, 2) + Math.pow(m.y - t.y, 2));
                                if (dist < 96) { if (m.takeDamage(3)) dungeon.getListThings().remove(j); }
                            }
                        }
                        if (Math.sqrt(Math.pow(hero.getX() - t.x, 2) + Math.pow(hero.getY() - t.y, 2)) < 40) hero.takeDamage(20);
                        dungeon.getListThings().remove(t); activeTornados.remove(i);
                    }
                }

                int hx = (int) (hero.getX() + 12) / 32; int hy = (int) (hero.getY() + 16) / 32;
                char tile = dungeon.getTileChar(hx, hy);
                if (tile == 'O') { hero.setHasWeapon(true); dungeon.removeThing(hx, hy); }
                if (tile == 'L') hero.takeDamage(3); 
                if (tile == 'G') hero.takePoint(1);  
                if (tile == 'N') { dungeon.loadLevel("level2.txt"); hero.setPosition(80, 32); }
                if (tile == 'P') { dungeon.loadLevel("level1.txt"); hero.setPosition(300, 70); }
                if (tile == 'B' || tile == 'E') renderPanel.setState(GameRender.State.VICTORY);

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