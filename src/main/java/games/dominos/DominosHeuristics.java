package games.dominos;

import iialib.games.algs.IHeuristic;

public class DominosHeuristics {

    // Constants for terminal state evaluation (large but safe values)
    private static final int WIN_VALUE = 100000000;
    private static final int LOSE_VALUE = -100000000;

    // Heuristics exposed for the two roles
    public static IHeuristic<DominosBoard, DominosRole> hVertical = (board, role) ->
            evaluate(board, DominosRole.VERTICAL);

    public static IHeuristic<DominosBoard, DominosRole> hHorizontal = (board, role) ->
            evaluate(board, DominosRole.HORIZONTAL);

    // Advanced evaluation from the perspective of `perspective`
    private static int evaluateAdvanced(DominosBoard board, DominosRole perspective) {
        // Terminal state: assign extreme values
        if (board.isGameOver()) {
            boolean vertZero = board.nbVerticalMoves() == 0;
            boolean horZero = board.nbHorizontalMoves() == 0;
            // Both zero => draw
            if (vertZero && horZero) return 0;
            // If vertical has no moves -> vertical lost
            if (vertZero) {
                return (perspective == DominosRole.VERTICAL) ? LOSE_VALUE : WIN_VALUE;
            }
            // Otherwise horizontal has no moves -> horizontal lost
            return (perspective == DominosRole.HORIZONTAL) ? LOSE_VALUE : WIN_VALUE;
        }

        int vert = board.nbVerticalMoves();
        int hor = board.nbHorizontalMoves();

        // 1. Mobility difference (basic advantage)
        int mobilityDiff = (perspective == DominosRole.VERTICAL) ? (vert - hor) : (hor - vert);

        // 2. Mobility ratio (more important in endgame)
        // Avoid division by zero: if opponent has 0 moves, we already won
        double mobilityRatio = 0;
        if (perspective == DominosRole.VERTICAL) {
            mobilityRatio = (hor > 0) ? (double) vert / hor : 10.0;
        } else {
            mobilityRatio = (vert > 0) ? (double) hor / vert : 10.0;
        }

        // 3. Penalty if we're getting dangerously low on moves
        int myMoves = (perspective == DominosRole.VERTICAL) ? vert : hor;
        int lowMovePenalty = 0;
        if (myMoves <= 2) {
            lowMovePenalty = -50 * (3 - myMoves); // -50 for 2 moves, -100 for 1 move
        }

        // 4. Bonus if opponent is getting low on moves
        int oppMoves = (perspective == DominosRole.VERTICAL) ? hor : vert;
        int oppLowBonus = 0;
        if (oppMoves <= 2) {
            oppLowBonus = 50 * (3 - oppMoves);
        }

        // 5. Control advantage (more moves = better board control)
        int totalMoves = vert + hor;
        int controlScore = (totalMoves > 0) ?
            (mobilityDiff * 100 / totalMoves) : 0;

        // Combine all factors with weights
        int score = mobilityDiff * 10              // Base mobility
                  + (int)(mobilityRatio * 20)      // Ratio importance
                  + lowMovePenalty                  // Our danger
                  + oppLowBonus                     // Opponent danger
                  + controlScore;                   // Board control

        return score;
    }

    // Simple evaluation (kept for reference/comparison)
    private static int evaluate(DominosBoard board, DominosRole perspective) {
        if (board.isGameOver()) {
            boolean vertZero = board.nbVerticalMoves() == 0;
            boolean horZero = board.nbHorizontalMoves() == 0;
            if (vertZero && horZero) return 0;
            if (vertZero) {
                return (perspective == DominosRole.VERTICAL) ? LOSE_VALUE : WIN_VALUE;
            }
            return (perspective == DominosRole.HORIZONTAL) ? LOSE_VALUE : WIN_VALUE;
        }

        int vert = board.nbVerticalMoves();
        int hor = board.nbHorizontalMoves();
        return (perspective == DominosRole.VERTICAL) ? (vert - hor) : (hor - vert);
    }

}
