// Cette classe va nous permettre de charcger une seule grande image et la découper en plusieurs tiules ou icones

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class TileManager {
    // Attributs de taille des tuiles
    private final int width;
    private final int height;
    
    // Attributs pour la gestion des images  
    private Image[][] tiles; 
    // C'est l'image géante brute chargée avant découpage
    private BufferedImage tileSheet;

    // On modifie ensuite le constrcuteur pour accepter le nom du fichier
    public TileManager(int width, int height, String fileName) {
        this.width = width;
        this.height = height;
        this.tiles = new Image[100][100]; // On initialise un tableau assez grand pour stocker les tuiles, de max 100 par 100
        
        // On découpe ensuite les icones 
        setTiles(width, height, fileName);
    }

    // Getters simples
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Cette méthode va nous permettre de charger l'image et la découper 
    private void setTiles(int width, int height, String fileName) {
        try {
            // On lit le fichier image
            tileSheet = ImageIO.read(new File(fileName));
            
            // Calcul du nombre de colonnes et de lignes dans l'image
            // Cela permet de savoir combien de tuiles on peut découper
            int nbCols = tileSheet.getWidth() / width;
            int nbRows = tileSheet.getHeight() / height;

            // On découpe ensuite l'image (subImages) 
            for (int i = 0; i < nbRows; i++) {
                for (int j = 0; j < nbCols; j++) {
                    // On extrait le morceau d'image aux coordonnées (j, i)
                    tiles[j][i] = tileSheet.getSubimage(j * width, i * height, width, height);
                }
            }
        } catch (Exception e) {
            // Apres plusieurs erreurs nous avons ajouter un gestionnaire d'erreurs pour cette partie 
            System.out.println("Erreur lors du chargement de l'image : " + fileName);
            e.printStackTrace();
        }
    }

    // Méthode pour récupérer une tuile et donc l'image/icone spécifique dans le tableau 
    public Image getTile(int x, int y) {
        return tiles[x][y];
    }
}