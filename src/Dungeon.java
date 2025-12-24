import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Dungeon {
    private char[][] map;           
    private int height, width;              
    private TileManager tileManager; 
    private ArrayList<Things> listThings; 

    public Dungeon(String fileName, TileManager tileManager) {
        this.tileManager = tileManager;
        this.listThings = new ArrayList<>();
        loadLevel(fileName);
    }

    public void loadLevel(String fileName) {
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            int h = 0, w = 0; String line = br.readLine();
            while (line != null) { h++; if (line.length() > w) w = line.length(); line = br.readLine(); }
            br.close(); 
            this.height = h; this.width = w; this.map = new char[height][width];
            fr = new FileReader(fileName); br = new BufferedReader(fr);
            for (int i = 0; i < height; i++) {
                line = br.readLine();
                for (int j = 0; j < width; j++) map[i][j] = (j < line.length()) ? line.charAt(j) : ' ';
            }
            br.close();
        } catch (Exception e) { this.height=10; this.width=20; this.map=new char[height][width]; }
        respawnListOfThings();
    }

    public void respawnListOfThings() {
        listThings.clear(); 
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int x = j * tileManager.getWidth(); int y = i * tileManager.getHeight();
                char c = map[i][j];

                if (c == 'W') listThings.add(new SolidThings(x, y, tileManager.getTile(2, 1)));
                else if (c == 'S') listThings.add(new Things(x, y, tileManager.getTile(2, 2)));
                else if (c == 'L') listThings.add(new Things(x, y, tileManager.getTile(7, 1)));
                
                else if (c == 'V') { listThings.add(new Things(x, y, tileManager.getTile(2, 2))); listThings.add(new SolidThings(x, y, tileManager.getTile(0, 0))); }
                else if (c == 'F') { listThings.add(new Things(x, y, tileManager.getTile(2, 2))); listThings.add(new SolidThings(x, y, tileManager.getTile(1,0))); }
                else if (c == 'G') { listThings.add(new Things(x, y, tileManager.getTile(2, 2))); listThings.add(new Things(x, y, tileManager.getTile(1, 7))); }
                else if (c == 'B') { listThings.add(new Things(x, y, tileManager.getTile(2, 2))); listThings.add(new Things(x, y, tileManager.getTile(4, 7))); }
                
                else if (c == 'X') { listThings.add(new Things(x, y, tileManager.getTile(2, 2))); listThings.add(new Monster(x, y, tileManager.getTile(6, 4))); }
                else if (c == 'O') {
                    listThings.add(new Things(x, y, tileManager.getTile(2, 2)));
                    HeroType type = Hero.getInstance().getType();
                    if (type == HeroType.MAGICIEN) listThings.add(new Things(x, y, tileManager.getTile(8, 7))); 
                    else if (type == HeroType.LUTIN) listThings.add(new Things(x, y, tileManager.getTile(7, 9))); 
                    else listThings.add(new Things(x, y, tileManager.getTile(3, 8))); 
                }
                else if (c == 'N' || c == 'P' || c == 'E') listThings.add(new Things(x, y, tileManager.getTile(0, 3)));
                else listThings.add(new Things(x, y, tileManager.getTile(4, 0)));
            }
        }
    }

    public void removeThing(int xIndex, int yIndex) {
        if (xIndex < 0 || xIndex >= width || yIndex < 0 || yIndex >= height) return;
        map[yIndex][xIndex] = 'S'; 
        int targetX = xIndex * tileManager.getWidth();
        int targetY = yIndex * tileManager.getHeight();
        for (int i = listThings.size() - 1; i >= 0; i--) {
            if (listThings.get(i).x == targetX && listThings.get(i).y == targetY) { listThings.remove(i); break; }
        }
    }
    public ArrayList<Things> getListThings() { return listThings; }
    public char getTileChar(int x, int y) { if (y>=0 && y<height && x>=0 && x<width) return map[y][x]; return ' '; }
}