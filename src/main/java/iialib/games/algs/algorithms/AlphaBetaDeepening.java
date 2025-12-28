package iialib.games.algs.algorithms;

import iialib.games.algs.IHeuristic;
import iialib.games.model.IBoard;
import iialib.games.model.IMove;
import iialib.games.model.IRole;

public class AlphaBetaDeepening<Move extends IMove, Role extends IRole, Board extends IBoard<Move, Role, Board>> {

    private final AlphaBeta<Move, Role, Board> alphaBeta;
    private final int absoluteMaxDepth;

    public AlphaBetaDeepening(Role playerMax, Role playerMin, IHeuristic<Board, Role> h, int absoluteMaxDepth) {
        this.alphaBeta = new AlphaBeta<>(playerMax, playerMin, h, 1);
        this.absoluteMaxDepth = absoluteMaxDepth;
    }

    /**
     * @param board Le plateau actuel
     * @param role Le rôle de l'IA
     * @param timeLimitMs Le temps maximum alloué en millisecondes
     */
    public Move bestMove(Board board, Role role, long timeLimitMs) {
        long startTime = System.currentTimeMillis();
        Move bestMoveFound = null;
        int currentDepth = 1;

        // On boucle sur la profondeur
        while (currentDepth <= absoluteMaxDepth) {
            long elapsed = System.currentTimeMillis() - startTime;

            // Sécurité : Si on a consommé plus de 70% du temps,
            // on ne lance pas la profondeur suivante (qui sera bien plus longue)
            if (elapsed > timeLimitMs * 0.7) {
                break;
            }

            // Mise à jour de la profondeur de l'algorithme
            this.alphaBeta.setDepthMax(currentDepth);

            try {
                // On lance la recherche pour cette profondeur précise
                Move move = this.alphaBeta.bestMove(board, role);
                if (move != null) {
                    bestMoveFound = move;
                }
            } catch (Exception e) {
                // En cas d'erreur ou d'interruption
                break;
            }

            currentDepth++;
        }

        return bestMoveFound;
    }
}