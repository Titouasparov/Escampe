package escampe;

import iialib.games.model.IMove;

// Le role de cette classe est de :
// - représenter les coups dans le jeu d'Escampe
// - implémenter l'interface IMove pour être compatible avec le framework de jeu
// - fournir une méthode pour obtenir une représentation en chaîne de caractères du coup
public class EscampeMove implements IMove {
    public final String moveStr; // Le coup en String (ex: "A1-B2")

    public EscampeMove(String moveStr) {
        this.moveStr = moveStr;
    }

    @Override
    public String toString() {
        return moveStr;
    }
}