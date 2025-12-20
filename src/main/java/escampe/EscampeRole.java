package escampe;

import iialib.games.model.IRole;

// Le role de cette classe est de :
//- représenter les rôles des joueurs dans le jeu d'Escampe
//- implémenter l'interface IRole pour être compatible avec le framework de jeu
//- fournir des méthodes pour obtenir le nom du rôle, vérifier l'égalité entre rôles
//  et obtenir une représentation en chaîne de caractères du rôle

public class EscampeRole implements IRole {
    private final String couleur; // "blanc" ou "noir"

    public EscampeRole(String couleur) {
        this.couleur = couleur;
    }

    public String getName() {
        return couleur;
    }

    // Redéfinition de la méthode equals pour comparer les rôles basés sur la couleur
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EscampeRole that = (EscampeRole) o;
        return couleur.equals(that.couleur);
    }

    // Redéfinition de la méthode toString pour obtenir une représentation en chaîne de caractères du rôle
    // exemple : on passe de EscampeRole("blanc") à "blanc"
    @Override
    public String toString() {
        return couleur;
    }
}