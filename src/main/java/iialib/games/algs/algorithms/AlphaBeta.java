package iialib.games.algs.algorithms;

import iialib.games.algs.GameAlgorithm;
import iialib.games.algs.IHeuristic;
import iialib.games.model.IBoard;
import iialib.games.model.IMove;
import iialib.games.model.IRole;

public class AlphaBeta<Move extends IMove,Role extends IRole,Board extends IBoard<Move,Role,Board>> implements GameAlgorithm<Move,Role,Board> {

    // Constants
    /** Defaut value for depth limit
     */
    private int depthMax;

    /** number of internal visited (developed) nodes (for stats)
     */
    private int nbNodes;

    /** Heuristic used by the max player
     */
    private IHeuristic<Board, Role> h;

    /** number of leaves nodes nodes (for stats)

     */
    private int nbLeaves;

    // Attributes
    /** Role of the max player
     */
    private final Role playerMaxRole;

    /** Role of the min player
     */
    private final Role playerMinRole;

    public AlphaBeta(Role playerMaxRole, Role playerMinRole, IHeuristic<Board, Role> h, int depthMax) {
        this.h = h;
        this.playerMaxRole = playerMaxRole;
        this.playerMinRole = playerMinRole;
        this.depthMax = depthMax;
    }

    @Override
    public Move bestMove(Board board, Role playerRole) {
        this.nbNodes = 0;
        this.nbLeaves = 0;
        Move bestMove = null;
        int bestValue = IHeuristic.MIN_VALUE;
        for (Move move : board.possibleMoves(playerRole)) {
            Board nextBoard = board.play(move, playerRole);
            // After making a move for playerRole, the next level is the opponent (minimizer if we are maximizing)
            // We start recursion at depth=1 and indicate that the next node is a minimizing node
            int moveValue = alphaBeta(nextBoard, 1, IHeuristic.MIN_VALUE, IHeuristic.MAX_VALUE, false);
            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
            }
        }

        System.out.println("Nombre de noeuds internes visités : " + this.nbNodes);
        System.out.println("Nombre de feuilles évaluées : " + this.nbLeaves);
        return bestMove;
    }

    private int alphaBeta(Board board, int depth, int alpha, int beta, Boolean isMaximizingPlayer) {
        this.nbNodes++;

        // Determine current role based on whether this node is maximizing or minimizing
        Role currentRole = isMaximizingPlayer ? playerMaxRole : playerMinRole;

        if (board.isGameOver() || depth >= this.depthMax) {
            this.nbLeaves++;
            // Evaluate from the point of view of the MAX player for consistency with MiniMax
            return h.eval(board, playerMaxRole);
        }

        if (isMaximizingPlayer) {
            int bestVal = IHeuristic.MIN_VALUE;
            for (Move move : board.possibleMoves(currentRole)) {
                Board nextBoard = board.play(move, currentRole);
                // Next level will be minimizing
                int value = alphaBeta(nextBoard, depth + 1, alpha, beta, false);
                bestVal = Math.max(bestVal, value);
                alpha = Math.max(alpha, bestVal);
                if (beta <= alpha) {
                    break; // Beta cut-off
                }
            }
            return bestVal;
        }
        else {
            int bestVal = IHeuristic.MAX_VALUE;
            for (Move move : board.possibleMoves(currentRole)) {
                Board nextBoard = board.play(move, currentRole);
                // Next level will be maximizing
                int value = alphaBeta(nextBoard, depth + 1, alpha, beta, true);
                bestVal = Math.min(bestVal, value);
                beta = Math.min(beta, bestVal);
                if (beta <= alpha) {
                    break; // Alpha cut-off
                }
            }
            return bestVal;
        }
    }
}
