import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Dungeon {
    private char[][] map;           
    private int height;             
    private int width;              
    private TileManager tileManager; 
    private ArrayList<Things> listThings; 

    public Dungeon(String fileName, TileManager tileManager) {
        this.tileManager = tileManager;
        this.listThings = new ArrayList<>();
        loadLevel(fileName);
    }

    public void loadLevel(String fileName) {
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fileReader);
            
            int h = 0;
            int w = 0;
            String line = br.readLine();

            while (line != null) {
                h++; 
                if (line.length() > w) w = line.length(); 
                line = br.readLine();
            }
            br.close(); 

            this.height = h;
            this.width = w;
            this.map = new char[height][width];

            fileReader = new FileReader(fileName);
            br = new BufferedReader(fileReader);
            
            for (int i = 0; i < height; i++) {
                line = br.readLine();
                for (int j = 0; j < width; j++) {
                    if (j < line.length()) map[i][j] = line.charAt(j);
                    else map[i][j] = ' ';
                }
            }
            br.close();

        } catch (Exception e) {
            System.out.println("Erreur chargement niveau: " + e.getMessage());
            this.height = 10;
            this.width = 20;
            this.map = new char[height][width];
        }
        respawnListOfThings();
    }

    public void respawnListOfThings() {
        listThings.clear(); 
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int x = j * tileManager.getWidth();
                int y = i * tileManager.getHeight();
                char c = map[i][j];

                if (c == 'W') {
                    listThings.add(new SolidThings(x, y, tileManager.getTile(2, 1)));
                } 
                else if (c == 'L') {
                    listThings.add(new Things(x, y, tileManager.getTile(7, 1)));
                }
                else if (c == 'V') {
                    listThings.add(new Things(x, y, tileManager.getTile(0, 0)));
                }
                else if (c == 'F') {
                    listThings.add(new Things(x, y, tileManager.getTile(1,0)));
                }
                else if (c == 'S') {
                    listThings.add(new Things(x, y, tileManager.getTile(2, 2)));
                }
                else if (c == 'B') {
                    listThings.add(new Things(x, y, tileManager.getTile(4, 7)));
                }
                else if (c == 'X') {
                    listThings.add(new Things(x, y, tileManager.getTile(6, 4)));
                }
                else if (c == 'N' || c == 'P' || c == 'E') {
                    listThings.add(new Things(x, y, tileManager.getTile(0, 3)));
                }
                else if (c == 'G') {
                    listThings.add(new Things(x, y, tileManager.getTile(1, 7)));
                }
                // --- GESTION INTELLIGENTE DE L'OBJET 'O' ---
                else if (c == 'O') {
                    HeroType type = Hero.getInstance().getType();
                    
                    if (type == HeroType.MAGICIEN) {
                        listThings.add(new Things(x, y, tileManager.getTile(8, 7))); // Baguette
                    } 
                    else if (type == HeroType.LUTIN) {
                        listThings.add(new Things(x, y, tileManager.getTile(7, 9))); // Tornade
                    } 
                    else {
                        listThings.add(new Things(x, y, tileManager.getTile(3, 8))); // Épée (Chevalier)
                    }
                }
                else {
                    listThings.add(new Things(x, y, tileManager.getTile(4, 0)));
                }
            }
        }
    }

    // Méthode pour supprimer un objet quand on le ramasse
    public void removeThing(int xIndex, int yIndex) {
        if (xIndex < 0 || xIndex >= width || yIndex < 0 || yIndex >= height) return;

        map[yIndex][xIndex] = ' '; 
        int targetX = xIndex * tileManager.getWidth();
        int targetY = yIndex * tileManager.getHeight();
        
        for (int i = listThings.size() - 1; i >= 0; i--) {
            Things thing = listThings.get(i);
            if (thing.x == targetX && thing.y == targetY) {
                listThings.remove(i);
                // On remplace par du sol
                listThings.add(new Things(targetX, targetY, tileManager.getTile(4, 0)));
                break;
            }
        }
    }

    public ArrayList<Things> getListThings() { return listThings; }
    public int getHeight() { return height; }
    public int getWidth() { return width; }

    public char getTileChar(int indexX, int indexY) {
        if (indexY >= 0 && indexY < height && indexX >= 0 && indexX < width) {
            return map[indexY][indexX];
        }
        return ' '; 
    }
}