// Ici, on importe tous les outils Java dont on a besoin (fenêtres, événements, listes...).
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

// On crée notre classe principale qui est une fenêtre (JFrame).
public class MainInterface extends JFrame {
    
    // Là, on déclare nos variables principales :
    private GameRender renderPanel; // Notre zone de dessin
    private AudioPlayer player;     // Notre lecteur de son
    // On prépare une liste pour stocker les tornades du Lutin
    private ArrayList<Tornado> activeTornados = new ArrayList<>();

    // C'est le constructeur : c'est ici qu'on configure tout au démarrage.
    public MainInterface(Dungeon dungeon, Hero hero, TileManager tm, AudioPlayer player) {
        super("Mon TP Java - Donjon"); // On donne un titre à la fenêtre
        this.player = player; 
        
        // On dit au programme de s'arrêter si on ferme la fenêtre.
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // On fixe la taille : 800 par 600 pixels.
        this.setSize(new Dimension(800, 600));
        // On la centre sur l'écran.
        this.setLocationRelativeTo(null); 
        
        // On initialise notre panneau de jeu et on l'ajoute à la fenêtre.
        this.renderPanel = new GameRender(dungeon, hero, tm);
        this.setContentPane(renderPanel);


        // Ici, on ajoute un "écouteur" pour surveiller les clics.
        renderPanel.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) { }
            
            // Quand on appuie sur un bouton de la souris...
            public void mousePressed(MouseEvent e) { 
                int x = e.getX(); // On récupère où on a cliqué (X)
                int y = e.getY(); // Et la hauteur (Y)
                
                // Si on est dans le MENU...
                if (renderPanel.getState() == GameRender.State.MENU) {
                    // Si on clique sur les images des héros...
                    if (y >= 180 && y <= 300) {
                        renderPanel.isTypingName = false;
                        // On regarde sur quel héros on a cliqué et on le sélectionne.
                        if (x >= 130 && x <= 250) renderPanel.setSelectedHeroIndex(0);
                        else if (x >= 310 && x <= 430) renderPanel.setSelectedHeroIndex(1);
                        else if (x >= 490 && x <= 610) renderPanel.setSelectedHeroIndex(2);
                        renderPanel.repaint(); // On redessine pour voir le cadre jaune.
                    }
                    
                    // Si on clique sur la zone de texte, on active l'écriture.
                    if (x >= 300 && x <= 500 && y >= 380 && y <= 430) { 
                        renderPanel.isTypingName = true; renderPanel.repaint(); 
                    }
                    else if (y < 380 || y > 430 || x < 300 || x > 500) { 
                        renderPanel.isTypingName = false; renderPanel.repaint(); 
                    }
                    
                    // Si on clique sur "JOUER", on lance la partie !
                    if (x >= 300 && x <= 500 && y >= 450 && y <= 510) lancerLeJeu(hero, tm);
                }
                // Si on est en GAME OVER, on regarde si on clique pour recommencer.
                else if (renderPanel.getState() == GameRender.State.GAME_OVER) {
                    if (x >= 300 && x <= 500 && y >= 350 && y <= 410) recommencerJeu(hero, dungeon);
                }
            }
            public void mouseReleased(MouseEvent e) {} public void mouseEntered(MouseEvent e) {} public void mouseExited(MouseEvent e) {}
        });

        // Là, on écoute les touches du clavier.
        this.addKeyListener(new KeyListener() {
            
            // Ici, on gère la saisie du nom dans le menu.
            public void keyTyped(KeyEvent e) { 
                if (renderPanel.getState() == GameRender.State.MENU && renderPanel.isTypingName) {
                    char c = e.getKeyChar();
                    String currentName = renderPanel.getPlayerName();
                    // On gère la touche "Effacer"
                    if (c == '\b') { if (currentName.length() > 0) renderPanel.setPlayerName(currentName.substring(0, currentName.length() - 1)); }
                    // Sinon on ajoute la lettre au nom
                    else if (Character.isLetterOrDigit(c) || c == ' ') { if (currentName.length() < 12) renderPanel.setPlayerName(currentName + c); }
                    renderPanel.repaint();
                }
            }
            public void keyReleased(KeyEvent e) { }
            
            // Ici, on gère les déplacements et actions du héros.
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                // Si on fait "Entrée" dans le menu, ça lance le jeu.
                if (renderPanel.getState() == GameRender.State.MENU) { if (key == KeyEvent.VK_ENTER) lancerLeJeu(hero, tm); return; }
                
                // Si le jeu est lancé et que le héros est en vie...
                if (renderPanel.getState() == GameRender.State.PLAY && hero.getLife() > 0) {
                    double speed = hero.getSpeed(); 
                    switch (key) {
                        // On gère les flèches : on change l'orientation et on bouge.
                        case KeyEvent.VK_LEFT:  hero.setOrientation(Orientation.LEFT);   hero.moveIfPossible(-speed, 0, dungeon); break;
                        case KeyEvent.VK_RIGHT: hero.setOrientation(Orientation.RIGHT);  hero.moveIfPossible(speed, 0, dungeon); break;
                        case KeyEvent.VK_UP:    hero.setOrientation(Orientation.TOP);    hero.moveIfPossible(0, -speed, dungeon); break;
                        case KeyEvent.VK_DOWN:  hero.setOrientation(Orientation.BOTTOM); hero.moveIfPossible(0, speed, dungeon); break;
                        
                        // Si on appuie sur ESPACE : on attaque !
                        case KeyEvent.VK_SPACE:
                            player.playSound("attack.wav"); // On lance le bruitage
                            hero.attack(dungeon, tm); // On lance l'anim visuelle

                            // Si c'est un LUTIN, on ajoute une tornade.
                            if (hero.getType() == HeroType.LUTIN && hero.hasWeapon()) {
                                Tornado t = new Tornado((int)hero.getX(), (int)hero.getY(), tm.getTile(7, 8)); 
                                activeTornados.add(t); dungeon.getListThings().add(t); 
                            }
                            
                            // Si c'est un CHEVALIER, on gère le corps-à-corps.
                            if (hero.getType() == HeroType.CHEVALIER && hero.hasWeapon()) {
                                for (int i = dungeon.getListThings().size() - 1; i >= 0; i--) {
                                    Things th = dungeon.getListThings().get(i);
                                    if (th instanceof Monster) {
                                        // On calcule si le monstre est assez près.
                                        double dist = Math.sqrt(Math.pow(th.x - hero.getX(), 2) + Math.pow(th.y - hero.getY(), 2));
                                        if (dist <= 64) { 
                                            // On lui inflige des dégâts.
                                            if (((Monster)th).takeDamage(1, hero.getX(), hero.getY())) {
                                                dungeon.getListThings().remove(i); // S'il meurt, on l'enlève.
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                            
                        // Touche '0' pour se soigner (petit cheat code pour tester).
                        case KeyEvent.VK_0: case KeyEvent.VK_NUMPAD0: hero.tryToHeal(); break;
                        // Touches +/- pour le volume du son.
                        case KeyEvent.VK_PLUS: case KeyEvent.VK_ADD: player.adjustVolume(2.0f); break; 
                        case KeyEvent.VK_MINUS: case KeyEvent.VK_SUBTRACT: player.adjustVolume(-2.0f); break; 
                    } 
                    renderPanel.repaint(); // On met à jour l'écran tout de suite.
                }
            }
        });


        // On crée un chrono qui se déclenche toutes les 30ms (le cœur du jeu).
        Timer timer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Si on ne joue pas, on rafraîchit juste l'image.
                if (renderPanel.getState() != GameRender.State.PLAY) { renderPanel.repaint(); return; }
                
                // Si le héros n'a plus de vie, on bascule en Game Over.
                if (hero.getLife() <= 0) { renderPanel.setState(GameRender.State.GAME_OVER); renderPanel.repaint(); return; }

                // ON MET À JOUR LES MONSTRES
                for (Things thing : dungeon.getListThings()) {
                    if (thing instanceof Monster) {
                        Monster m = (Monster) thing;
                        m.update(dungeon, hero); // On fait bouger le monstre
                        // Si le monstre touche le héros, on perd des points de vie / la vie.
                        if (m.getHitBox().intersect(hero.getHitBox())) hero.takeDamage(2); 
                    }
                }

                // On met a jour les projectiles
                for (int i = dungeon.getListThings().size() - 1; i >= 0; i--) {
                    Things thing = dungeon.getListThings().get(i);
                    // On supprime les explosions finies.
                    if (thing instanceof Effect) { if (((Effect) thing).isExpired()) dungeon.getListThings().remove(i); }
                    // On fait avancer les missiles.
                    else if (thing instanceof Missile) {
                        Missile m = (Missile) thing;
                        m.move(dungeon); 
                        
                        // Si le missile va trop loin, on l'enlève.
                        if (m.isExpired() || m.getDistanceTraveled() > 400) { dungeon.getListThings().remove(i); continue; }

                        // On regarde si le missile touche un monstre.
                        boolean hit = false;
                        for (int j = dungeon.getListThings().size() - 1; j >= 0; j--) {
                            if (dungeon.getListThings().get(j) instanceof Monster) {
                                Monster monstre = (Monster) dungeon.getListThings().get(j);
                                double distance = Math.sqrt(Math.pow(m.x - monstre.x, 2) + Math.pow(m.y - monstre.y, 2));
                                if (distance < 45) { 
                                    // On peut touché et causé des dégats aux monstres
                                    if (monstre.takeDamage(3, m.x, m.y)) { 
                                        dungeon.getListThings().remove(j);
                                        if (j < i) i--; 
                                    }
                                    hit = true;
                                    break;
                                }
                            }
                        }
                        if (hit) dungeon.getListThings().remove(i); // On supprime le missile après impact.
                    }
                }

                // Gestion des tornades pour notre "Lutin"
                for (int i = activeTornados.size() - 1; i >= 0; i--) {
                    Tornado t = activeTornados.get(i);
                    if (t.isReadyToExplode()) {
                        // On crée une explosion visuelle.
                        dungeon.getListThings().add(new Effect(t.x, t.y, tm.getTile(8, 1), 500));
                        // On blesse les monstres autour.
                        for (int j = dungeon.getListThings().size() - 1; j >= 0; j--) {
                            if (dungeon.getListThings().get(j) instanceof Monster) {
                                Monster m = (Monster) dungeon.getListThings().get(j);
                                double dist = Math.sqrt(Math.pow(m.x - t.x, 2) + Math.pow(m.y - t.y, 2));
                                if (dist < 100) { if (m.takeDamage(3, t.x, t.y)) dungeon.getListThings().remove(j); }
                            }
                        }
                        // Bien sur le hero subit egalement des dégats
                        if (Math.sqrt(Math.pow(hero.getX() - t.x, 2) + Math.pow(hero.getY() - t.y, 2)) < 40) hero.takeDamage(20);
                        // On nettoie la tornade.
                        dungeon.getListThings().remove(t); activeTornados.remove(i);
                    }
                }

                // La gestion des objets de nos héros (Armes, Potions, Sorties)
                int hx = (int) (hero.getX() + 12) / 32; int hy = (int) (hero.getY() + 16) / 32;
                char tile = dungeon.getTileChar(hx, hy);
                
                // Si on marche sur 'O', on récupère l'arme.
                if (tile == 'O') { player.playSound("object.wav"); hero.setHasWeapon(true); dungeon.removeThing(hx, hy); }
                // Si on marche sur de la lave ('L').
                if (tile == 'L') hero.takeDamage(3); 
                // Si on marche sur une potion ('G').
                if (tile == 'G') { player.playSound("bell.wav"); hero.takePoint(100); dungeon.removeThing(hx, hy); }
                // Changement de niveau ('N' ou 'P').
                if (tile == 'N') { dungeon.loadLevel("level2.txt"); hero.setPosition(80, 32); }
                if (tile == 'P') { dungeon.loadLevel("level1.txt"); hero.setPosition(300, 70); }
                // Victoire ('B' ou 'E').
                if (tile == 'B' || tile == 'E') {
                    if (renderPanel.getState() != GameRender.State.VICTORY) {
                        renderPanel.setState(GameRender.State.VICTORY);
                        player.stopMusic();
                        player.playSound("victoire.wav"); 
                    }
                }
                renderPanel.repaint(); // On redessine tout à la fin.
            }
        });
        timer.start(); // On démarre la boucle !
        this.setVisible(true);
    }

    // Cette fonction sert à initialiser une nouvelle partie.
    private void lancerLeJeu(Hero hero, TileManager tm) {
        int choix = renderPanel.getSelectedHeroIndex();
        // On définit le héros selon ce qu'on a choisi dans le menu.
        if (choix == 0) hero.setType(HeroType.CHEVALIER); 
        if (choix == 1) hero.setType(HeroType.MAGICIEN); 
        if (choix == 2) hero.setType(HeroType.LUTIN);
        
        renderPanel.getDungeon().respawnListOfThings(); // On remet les monstres.
        activeTornados.clear();
        player.playMusic("game.wav"); 
        renderPanel.setState(GameRender.State.PLAY); renderPanel.repaint();
    }

    // Cette fonction remet tout à zéro après une mort.
    private void recommencerJeu(Hero hero, Dungeon dungeon) {
        hero.respawn(); activeTornados.clear(); dungeon.loadLevel("level1.txt");
        player.playMusic("game.wav"); renderPanel.setState(GameRender.State.PLAY); renderPanel.repaint();
    }

    // C'est le point de départ du programme.
    public static void main(String[] args) {
        try {
            File f = new File("tileSet.png");
            if (!f.exists()) System.out.println("ATTENTION: tileSet.png est introuvable !");
            
            // On charge les images, la carte, le héros et le son.
            TileManager tm = new TileManager(32, 32, "tileSet.png");
            Dungeon dungeon = new Dungeon("level1.txt", tm); 
            Hero hero = Hero.getInstance(); hero.setPosition(300, 400); 
            AudioPlayer player = new AudioPlayer(); player.playMusic("intro.wav"); 
            
            // Et on lance la fenêtre !
            new MainInterface(dungeon, hero, tm, player);
        } catch (Exception e) { e.printStackTrace(); }
    }
}