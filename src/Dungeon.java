import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

// La classe Dungeon est notre terrain de jeu et l'un des classes les plus importantes, 
// Cette classe va nous permettre de creer notre espace de jeux : mur, monstres, hero, décor...

public class Dungeon {
    private char[][] map;           // On instancie notre carte de jeu
    private int height;             // Hauteur de notre carte
    private int width;              // Largeur de notre carte
    private TileManager tileManager; // Référence vers le gestionnaire d'images (notre TileSet)
    private ArrayList<Things> listThings; // La liste de tous les objets (murs, sol, monstres) à afficher sur notre carte

// On créer ensuite un constructeur afin de charger depuis notre fichier texte les éléments de notre décor : murs, Sols, Monstres..
    public Dungeon(String fileName, TileManager tileManager) {
        this.tileManager = tileManager;
        this.listThings = new ArrayList<>();

        try {
            // On lit le fichier une première fois juste pour connaître la taille de celui-ci
            FileReader fileReader = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fileReader);
            
            int h = 0;
            int w = 0;
            String line = br.readLine();

            while (line != null) {
                h++; // On compte les lignes
                if (line.length() > w) {
                    w = line.length(); // On garde la largeur maximale trouvée, la dernière est la taille finale
                }
                line = br.readLine();
            }
            br.close(); // Nous permet de libérer la mémoire

            // On initialise le tableau avec les dimensions trouvées, le but est ensuite de le remplir
            this.height = h;
            this.width = w;
            this.map = new char[height][width];

            // On relit le fichier du début pour remplir le tableau : notre carte
            fileReader = new FileReader(fileName);
            br = new BufferedReader(fileReader);
            
            for (int i = 0; i < height; i++) {
                line = br.readLine();
                for (int j = 0; j < width; j++) {
                    // Si la ligne contient un caractère, on le copie. Sinon on met un espace.
                    if (j < line.length()) {
                        map[i][j] = line.charAt(j);
                    } else {
                        map[i][j] = ' ';
                    }
                }
            }
            br.close();

        } catch (Exception e) {
            // Apres plusierus erreurs, nousa vons déciderde mettre en place un gestionnaire d'erreurs 
            // afin de comprendre d'ou provenait certaines de nos erreurs
            System.out.println("Erreur chargement niveau: " + e.getMessage());
            this.height = 10;
            this.width = 20;
            this.map = new char[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    // Bords en murs ('W'), intérieur vide (' ')
                    map[i][j] = (i==0 || i==height-1 || j==0 || j==width-1) ? 'W' : ' ';
                }
            }
        }
        
        // Une fois notre carte remplie de lettres, on crée les vrais objets graphiques
        respawnListOfThings();
    }

// Une fois la premier patie faite, nous allons ensuite faire correspondre chacune de ces lettres à un icone précis
// Cette opération nous permet de voir le jeu prendre vie avec ces décors et son cheminement. 
    public void respawnListOfThings() {
        listThings.clear(); // On vide la liste avant de la remplir
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // On calcul de la position en pixels (x = colonne * largeur_tuile, y = ligne * hauteur_tuile)
                int x = j * tileManager.getWidth();
                int y = i * tileManager.getHeight();
                char c = map[i][j];

                // Création des différents icônes et son association avec nos lettres
                if (c == 'W') {
                    // MUR : SolidThings (Bloquant) - Image (2,1)
                    listThings.add(new SolidThings(x, y, tileManager.getTile(2, 1)));
                } 
                else if (c == 'L') {
                    // LAVE : Things (Traversable -> Piège) - Image (7,1)
                    // C'est un Things et pas un SolidThings car le héros doit marcher dessus pour se brûler.
                    // A la base cela devait être un SolidThings, mais nous avons adapter afin d'avoir un "piège" dans notre jeu 
                    listThings.add(new Things(x, y, tileManager.getTile(7, 1)));
                }
                else if (c == 'X') {
                    // ENNEMIE : Things, ces ennemies nous permettrons de nous battrent a long terme
                    // mais également de nous infliger des dégats  - Image (6,4)
                    listThings.add(new Things(x, y, tileManager.getTile(6, 4)));
                }
                else if (c == 'E') {
                    // SORTIE : Things - Image (0,3)
                    listThings.add(new Things(x, y, tileManager.getTile(0, 3)));
                }
                else if (c == 'F') {
                    // FONTAINE BLEU : SolidThings (Décor bloquant) - Image (1, 0)
                    listThings.add(new SolidThings(x, y, tileManager.getTile(1, 0))); 
                }
                else if (c == 'V') {
                    // FONTAINE ROUGE : SolidThings (Décor bloquant) - Image (0, 0)
                    listThings.add(new SolidThings(x, y, tileManager.getTile(0, 0)));
                }
                else if (c == 'S') {
                    // SOL 1 : Things - Image (2, 2)
                    listThings.add(new Things(x, y, tileManager.getTile(2, 2)));
                }
                else {
                    // PAR DEFAUT : Sol simple - Image (4,0)
                    listThings.add(new Things(x, y, tileManager.getTile(4, 0)));
                }
            }
        }
    }

    // Getters afin de pouvoir avoir accès aux informations dans d'aures classes
    public ArrayList<Things> getListThings() { return listThings; }
    public int getHeight() { return height; }
    public int getWidth() { return width; }

// Cette fonction vérifie d'abord si les coordonnées sont valides. 
// Si oui, elle renvoie la lettre (L : Lave, W :Mur...). Si non, elle renvoie du vide pour ne pas planter
    public char getTileChar(int indexX, int indexY) {
        // On vérifie que les coordonnées sont bien dans la carte pour éviter un crash
        if (indexY >= 0 && indexY < height && indexX >= 0 && indexX < width) {
            return map[indexY][indexX];
        }
        return ' '; // Retourne vide si on est hors map
    }
}