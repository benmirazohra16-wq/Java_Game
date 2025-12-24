import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

// Petite classe interne pour stocker un score (Nom + Temps).
// Elle implémente "Comparable" pour qu'on puisse trier la liste automatiquement plus tard.
class Score implements Comparable<Score> {
    String name;
    long time;

    public Score(String name, long time) {
        this.name = name;
        this.time = time;
    }

    // Cette méthode explique à Java comment comparer deux scores.
    // Ici, on veut que le plus petit temps arrive en premier (c'est le meilleur).
    @Override
    public int compareTo(Score other) {
        return Long.compare(this.time, other.time);
    }
}

public class ScoreManager {
    // Le nom du fichier où seront stockés les scores.
    private String filePath = "scores.txt"; 

    // Méthode pour écrire un score sur le disque dur.
    public void saveScore(String name, long time) {
        // On utilise FileWriter avec le paramètre 'true' pour dire "AJOUTE à la fin du fichier"
        // (au lieu d'écraser tout le fichier à chaque fois).
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(name + ":" + time); // On écrit sous la forme "Pseudo:120"
            writer.newLine(); // On passe à la ligne suivante
        } catch (IOException e) {
            e.printStackTrace(); // En cas d'erreur (disque plein, etc.)
        }
    }

    // Méthode pour récupérer et trier les meilleurs scores.
    public ArrayList<Score> getTopScores() {
        ArrayList<Score> scores = new ArrayList<>();
        File file = new File(filePath);
        
        // Si le fichier n'existe pas encore (première partie), on renvoie une liste vide.
        if (!file.exists()) return scores;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            // On lit le fichier ligne par ligne.
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":"); // On sépare le nom et le temps
                
                // On vérifie que la ligne est bien formatée (il faut 2 parties).
                if (parts.length == 2) {
                    try {
                        // On convertit le temps (String) en nombre (Long) et on l'ajoute.
                        scores.add(new Score(parts[0], Long.parseLong(parts[1])));
                    } catch (NumberFormatException e) {
                        // Si une ligne est corrompue, on l'ignore silencieusement.
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // C'est ici que la magie opère : Java trie la liste grâce à notre "compareTo".
        Collections.sort(scores);
        
        // On coupe la liste pour ne garder que les 5 premiers (le Top 5).
        if (scores.size() > 5) {
            return new ArrayList<>(scores.subList(0, 5));
        }
        return scores;
    }
}