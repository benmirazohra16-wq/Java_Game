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
    
    // On passe TileManager tm ici pour l'envoyer au Render (pour les images du menu)
    public MainInterface(Dungeon dungeon, Hero hero, TileManager tm) {
        super("Test Jeu - Donjon");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(800, 600));
        
        GameRender renderPanel = new GameRender(dungeon, hero, tm);
        this.setContentPane(renderPanel);

        // Gestionde la souris
        // IMPORTANT : On ajoute l'écouteur sur 'renderPanel' et pas sur 'this' pour avoir les bonnes coordonnées
        renderPanel.addMouseListener(new MouseListener() {
            
            @Override
            public void mouseClicked(MouseEvent e) { 
                // On a d'abord essayer cette fonction mais le click était pris en compte une fois sur deux donc on a décidée de passer au pressed qui détecte dès l'appuie du bouton
            }

            @Override
            public void mousePressed(MouseEvent e) { // Nouvelle méthode qui nous permettra de mieux sélectionner les différents boutons interactifs du menu
                // C'est ici qu'on gère le clic : mousePressed est instantané
                int x = e.getX();
                int y = e.getY();
                
                // Si nous nous trouvons dans le menu
                if (renderPanel.getState() == GameRender.State.MENU) {
                    
                    // Gestion du click concernant la selection des personnages
                    // On vérifie si on clique sur un des 3 héros possibles
                    if (y >= 250 && y <= 314) {
                        if (x >= 200 && x <= 264) {
                            renderPanel.setSelectedHeroIndex(0); // Choix 1
                            renderPanel.repaint();
                        }
                        else if (x >= 368 && x <= 432) {
                            renderPanel.setSelectedHeroIndex(1); // Choix 2
                            renderPanel.repaint();
                        }
                        else if (x >= 536 && x <= 600) {
                            renderPanel.setSelectedHeroIndex(2); // Choix 3
                            renderPanel.repaint();
                        }
                    }

                    // Gestion et prise en compte du bouton "Jouer"
                    if (x >= 300 && x <= 500 && y >= 400 && y <= 460) {
                        // on valide le choix et qu'on donne l'image au héros
                        int choix = renderPanel.getSelectedHeroIndex();
                        if (choix == 0) hero.setImage(tm.getTile(2, 4)); // Image Perso 1
                        if (choix == 1) hero.setImage(tm.getTile(1, 4)); // Image Perso 2
                        if (choix == 2) hero.setImage(tm.getTile(3, 4)); // Image Perso 3
                        
                        // On lance le jeu grace au : PLAY
                        renderPanel.setState(GameRender.State.PLAY);
                        renderPanel.repaint();
                    }
                }
                
                // SGestion et prise en compte du bouton "Rejouer"
                else if (renderPanel.getState() == GameRender.State.GAME_OVER) {
                    if (x >= 300 && x <= 500 && y >= 350 && y <= 410) {
                        hero.respawn(); // On remet la vie
                        // Parcontre si on meurt, on recommence au niveau 1, tout est reset ;
                        // ( on souhaite par la suite mettr en place des points de jeux afin d'accorder une force de frappe plus élever selon les points à notre héro
                        // par contre si il meurt, on perd ces points de jeux ) 
                        dungeon.loadLevel("level1.txt");
                        renderPanel.setState(GameRender.State.PLAY); // On relance le jeu
                        renderPanel.repaint();
                    }
                }
            }

            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });

        // Gestion du clavier 
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) { }
            @Override
            public void keyReleased(KeyEvent e) { }
            @Override
            public void keyPressed(KeyEvent e) {
                // Si on n'est pas en train de jouer, le clavier ne fait rien
                if (renderPanel.getState() != GameRender.State.PLAY) return;

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
        Timer timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                // Si on n'est pas en jeu (Menu ou Game Over), on arrête la logique
                if (renderPanel.getState() != GameRender.State.PLAY) return;

                // Vérification de la perte de tout nos points et donc la fin du jeu : Défaites
                if (hero.getLife() <= 0) {
                    renderPanel.setState(GameRender.State.GAME_OVER); // On active l'écran de défaite
                    renderPanel.repaint();
                    // On ne stoppe pas le timer pour que le bouton Rejouer reste cliquable
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
                
                // Guérison (Poisson)
                if (currentTile == 'G') {
                    hero.takePoint(1); // Soins (On reprend de la vie)
                }

                // On passe au niveau 2
                if (currentTile == 'N') {
                    System.out.println("Vers le niveau 2");
                    dungeon.loadLevel("level2.txt");
                    // On place le héros au début du niveau 2 (32, 32 = case 1,1)
                    hero.setPosition(80, 32); 
                    if (hero.getHitBox() != null) hero.getHitBox().setPosition(32, 32);
                    renderPanel.repaint();
                }

                // retour au niveau 1
                if (currentTile == 'P') {
                    System.out.println("Retour au niveau 1");
                    dungeon.loadLevel("level1.txt");
                    // On place le héros DEVANT la porte 'N' du niveau 1 (vers le bas à droite)
                    // Coordonnées approximatives : 600, 400 (case 18, 12 environ)
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
        
        // L'image sera définie quand on cliquera sur "JOUER".
        
        // Position de départ
        hero.setPosition(300, 400); 

        // On passe 'tm' au MainInterface
        new MainInterface(dungeon, hero, tm);
    }
}