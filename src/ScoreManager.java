import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

// Classe simple pour stocker un score (Nom + Temps)
class Score implements Comparable<Score> {
    String name;
    long time;

    public Score(String name, long time) {
        this.name = name;
        this.time = time;
    }

    // Cette méthode permet de trier les scores du plus petit temps au plus grand
    @Override
    public int compareTo(Score other) {
        return Long.compare(this.time, other.time);
    }
}

public class ScoreManager {
    private String filePath = "scores.txt"; // Le fichier sera créé à la racine du projet

    // Sauvegarder un nouveau score
    public void saveScore(String name, long time) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(name + ":" + time);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Récupérer le Top 5 des meilleurs scores
    public ArrayList<Score> getTopScores() {
        ArrayList<Score> scores = new ArrayList<>();
        File file = new File(filePath);
        
        // Si le fichier n'existe pas encore, on renvoie une liste vide
        if (!file.exists()) return scores;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                // On vérifie que la ligne est bien formatée (Nom:Temps)
                if (parts.length == 2) {
                    try {
                        scores.add(new Score(parts[0], Long.parseLong(parts[1])));
                    } catch (NumberFormatException e) {
                        // Ignorer les lignes corrompues
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // On trie la liste (le plus rapide en premier)
        Collections.sort(scores);
        
        // On ne garde que les 5 premiers
        if (scores.size() > 5) {
            return new ArrayList<>(scores.subList(0, 5));
        }
        return scores;
    }
}