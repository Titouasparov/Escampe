package escampe;

import iialib.games.algs.IHeuristic;

// Heuristique pour le jeu d'Escampe basée sur la mobilité des joueurs :
// c'est à dire le nombre de coups possibles pour chaque joueur.
// Plus un joueur a de coups possibles, mieux c'est pour lui.
// Le score est calculé comme la différence entre le nombre de coups possibles
// du joueur courant et celui de l'adversaire.
public class EscampeHeuristique implements IHeuristic<EscampeBoard, EscampeRole> {

    @Override
    public int eval(EscampeBoard board, EscampeRole role) {
        // --- HEURISTIQUE BASIQUE : MOBILITÉ ---

        // On récupère mes coups possibles
        int mesCoups = board.possibleMoves(role).size();

        // On récupère les coups de l'adversaire
        // (On inverse le rôle : si je suis blanc, l'autre est noir)
        String couleurAdverse = role.getName().equals("blanc") ? "noir" : "blanc";
        EscampeRole roleAdverse = new EscampeRole(couleurAdverse);
        int coupsAdverse = board.possibleMoves(roleAdverse).size();

        // Le score est la différence
        // Si j'ai 10 coups et lui 2, score = 8 (C'est bon pour moi)
        // Si j'ai 2 coups et lui 10, score = -8 (C'est mauvais)
        return mesCoups - coupsAdverse;
    }
}