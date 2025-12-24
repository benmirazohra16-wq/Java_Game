// On importe de quoi lire des fichiers texte (BufferedReader) et gérer des listes.
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

// Cette classe représente la carte (le niveau) du jeu.
public class Dungeon {
    // La carte est stockée sous forme d'une grille de caractères (ex: 'W' pour mur, 'S' pour sol).
    private char[][] map;           
    private int height, width;      // Dimensions de la carte (hauteur, largeur).
    private TileManager tileManager; // Notre outil pour découper les images (tuiles).
    private ArrayList<Things> listThings; // La liste de TOUS les objets présents dans le niveau.

    // Constructeur : On charge le niveau dès la création du donjon.
    public Dungeon(String fileName, TileManager tileManager) {
        this.tileManager = tileManager;
        this.listThings = new ArrayList<>();
        loadLevel(fileName); // On lit le fichier texte pour construire la carte.
    }

    // Méthode pour lire un fichier texte (ex: level1.txt) et le transformer en grille.
    public void loadLevel(String fileName) {
        try {
            // Étape 1 : On lit le fichier une première fois juste pour calculer sa taille (hauteur/largeur).
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            int h = 0, w = 0; 
            String line = br.readLine();
            while (line != null) { 
                h++; // On compte les lignes.
                if (line.length() > w) w = line.length(); // On trouve la ligne la plus longue.
                line = br.readLine(); 
            }
            br.close(); 
            
            // On initialise notre tableau aux bonnes dimensions.
            this.height = h; this.width = w; this.map = new char[height][width];
            
            // Étape 2 : On relit le fichier pour remplir le tableau caractère par caractère.
            fr = new FileReader(fileName); br = new BufferedReader(fr);
            for (int i = 0; i < height; i++) {
                line = br.readLine();
                for (int j = 0; j < width; j++) {
                    // Si la ligne est trop courte, on comble avec des espaces.
                    map[i][j] = (j < line.length()) ? line.charAt(j) : ' ';
                }
            }
            br.close();
        } catch (Exception e) { 
            // Si le fichier n'existe pas, on crée une carte vide par défaut pour éviter le plantage.
            this.height=10; this.width=20; this.map=new char[height][width]; 
        }
        
        // Une fois la carte lue, on crée les objets graphiques correspondants.
        respawnListOfThings();
    }

    // Méthode qui parcourt la grille de caractères et crée les objets du jeu (Murs, Monstres...).
    public void respawnListOfThings() {
        listThings.clear(); // On vide la liste pour repartir à zéro.
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // On calcule la position en pixels (ex: case (2,3) -> x=64, y=96).
                int x = j * tileManager.getWidth(); 
                int y = i * tileManager.getHeight();
                char c = map[i][j]; // On regarde quel caractère est écrit à cet endroit.

                // 'W' = Wall (Mur) -> On crée un SolidThings (on ne peut pas traverser).
                if (c == 'W') listThings.add(new SolidThings(x, y, tileManager.getTile(2, 1)));
                // 'S' = Sol -> Simple image de fond.
                else if (c == 'S') listThings.add(new Things(x, y, tileManager.getTile(2, 2)));
                // 'L' = Lave -> Sol qui fait mal.
                else if (c == 'L') listThings.add(new Things(x, y, tileManager.getTile(7, 1)));
                
                // 'V' = Vase / 'F' = Fleur -> Sol + un objet décoratif solide par dessus.
                else if (c == 'V') { listThings.add(new Things(x, y, tileManager.getTile(2, 2))); listThings.add(new SolidThings(x, y, tileManager.getTile(0, 0))); }
                else if (c == 'F') { listThings.add(new Things(x, y, tileManager.getTile(2, 2))); listThings.add(new SolidThings(x, y, tileManager.getTile(1,0))); }
                
                // 'G' = Guérison -> Une potion.
                else if (c == 'G') { listThings.add(new Things(x, y, tileManager.getTile(2, 2))); listThings.add(new Things(x, y, tileManager.getTile(1, 7))); }
                // 'B' = Boss -> La case de fin.
                else if (c == 'B') { listThings.add(new Things(x, y, tileManager.getTile(2, 2))); listThings.add(new Things(x, y, tileManager.getTile(4, 7))); }
                
                // 'X' = Monstre -> On met du sol et on ajoute un ennemi par dessus.
                else if (c == 'X') { listThings.add(new Things(x, y, tileManager.getTile(2, 2))); listThings.add(new Monster(x, y, tileManager.getTile(6, 4))); }
                
                // 'O' = Objet (Arme) -> L'image change selon le héros choisi.
                else if (c == 'O') {
                    listThings.add(new Things(x, y, tileManager.getTile(2, 2)));
                    HeroType type = Hero.getInstance().getType();
                    if (type == HeroType.MAGICIEN) listThings.add(new Things(x, y, tileManager.getTile(8, 7))); // Bâton
                    else if (type == HeroType.LUTIN) listThings.add(new Things(x, y, tileManager.getTile(7, 8))); // Lance
                    else listThings.add(new Things(x, y, tileManager.getTile(3, 8))); // Épée (Chevalier)
                }
                
                // Portes et Sorties ('N', 'P', 'E').
                else if (c == 'N' || c == 'P' || c == 'E') listThings.add(new Things(x, y, tileManager.getTile(0, 3)));
                
                // Par défaut, si on ne connaît pas la lettre, on met de l'herbe/vide.
                else listThings.add(new Things(x, y, tileManager.getTile(4, 0)));
            }
        }
    }

    // Méthode pour supprimer un objet de la carte (ex: quand on ramasse une potion).
    public void removeThing(int xIndex, int yIndex) {
        if (xIndex < 0 || xIndex >= width || yIndex < 0 || yIndex >= height) return;
        
        map[yIndex][xIndex] = 'S'; // On remplace la case par du sol simple dans la mémoire.
        
        // On cherche l'objet visuel correspondant et on le retire de la liste d'affichage.
        int targetX = xIndex * tileManager.getWidth();
        int targetY = yIndex * tileManager.getHeight();
        for (int i = listThings.size() - 1; i >= 0; i--) {
            if (listThings.get(i).x == targetX && listThings.get(i).y == targetY) { listThings.remove(i); break; }
        }
    }

    // Accesseurs pour récupérer la liste des objets ou le type d'une case.
    public ArrayList<Things> getListThings() { return listThings; }
    public char getTileChar(int x, int y) { if (y>=0 && y<height && x>=0 && x<width) return map[y][x]; return ' '; }
}