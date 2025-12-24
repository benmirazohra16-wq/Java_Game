// Encore une énumération (Enum).
// Elle sert à définir les 4 directions possibles pour le regard du héros.
// C'est utilisé pour savoir quelle image afficher (dos, face, profil)
// et dans quelle direction tirer les missiles.
public enum Orientation {
    TOP,    // Le héros regarde vers le HAUT (on voit son Dos)
    BOTTOM, // Le héros regarde vers le BAS (on voit sa Face)
    LEFT,   // Le héros regarde vers la GAUCHE (Profil Gauche)
    RIGHT   // Le héros regarde vers la DROITE (Profil Droit)
}