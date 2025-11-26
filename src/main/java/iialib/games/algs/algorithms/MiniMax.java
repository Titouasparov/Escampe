package iialib.games.algs.algorithms;

import iialib.games.algs.GameAlgorithm;
import iialib.games.algs.IHeuristic;
import iialib.games.model.IBoard;
import iialib.games.model.IMove;
import iialib.games.model.IRole;

public class MiniMax<Move extends IMove,Role extends IRole,Board extends IBoard<Move,Role,Board>> implements GameAlgorithm<Move,Role,Board> {

	// Constants
	/** Defaut value for depth limit 
     */
	private final static int DEPTH_MAX_DEFAUT = 4;

	// Attributes
	/** Role of the max player 
     */
	private final Role playerMaxRole;

	/** Role of the min player 
     */
	private final Role playerMinRole;

	/** Algorithm max depth
     */
	private int depthMax = DEPTH_MAX_DEFAUT;

	
	/** Heuristic used by the max player 
     */
	private IHeuristic<Board, Role> h;

	//
	/** number of internal visited (developed) nodes (for stats)
     */
	private int nbNodes;
	
	/** number of leaves nodes nodes (for stats)

     */
	private int nbLeaves;

	// --------- Constructors ---------

	public MiniMax(Role playerMaxRole, Role playerMinRole, IHeuristic<Board, Role> h) {
		this.playerMaxRole = playerMaxRole;
		this.playerMinRole = playerMinRole;
		this.h = h;
	}

	//
	public MiniMax(Role playerMaxRole, Role playerMinRole, IHeuristic<Board, Role> h, int depthMax) {
		this(playerMaxRole, playerMinRole, h);
		this.depthMax = depthMax;
	}

	/*
	 * IAlgo METHODS =============
	 */

	@Override
	public Move bestMove(Board board, Role playerRole) {
		System.out.println("[MiniMax]");

		Move bestMove = null;
		this.nbNodes = 0;
		this.nbLeaves = 0;

		if (playerRole != playerMaxRole) {
			return null;
		}

		Iterable<Move> moves = board.possibleMoves(playerRole);
		int bestValue = IHeuristic.MIN_VALUE;
		for (Move m : moves) {
			Board nextBoard = board.play(m, playerRole);
			int value = minValue(nextBoard, 1);
			if (value > bestValue) {
				bestValue = value;
				bestMove = m;
			}
		}

		System.out.println("  Nombre de noeuds développés : " + this.nbNodes);
		System.out.println("  Nombre de feuilles évaluées : " + this.nbLeaves);
		return bestMove;
	}

	/*
	 * PUBLIC METHODS ==============
	 */

	public String toString() {
		return "MiniMax(ProfMax=" + depthMax + ")";
	}

	/*
	 * PRIVATE METHODS ===============
	 */
	private int maxValue(Board board, int depth) {
		// Compte ce nœud comme étant développé
		this.nbNodes++;

		// 1. Condition d'arrêt: Fin de partie ou horizon de recherche atteint
		if (board.isGameOver() || depth >= depthMax) {
			this.nbLeaves++;
			// On retourne l'évaluation heuristique (doit correspondre au type de retour)
			return h.eval(board, playerMaxRole);
		}

		int value = IHeuristic.MIN_VALUE;
		Iterable<Move> moves = board.possibleMoves(playerMaxRole);

		for (Move m : moves) {
			Board nextBoard = board.play(m, playerMaxRole);
			// 2. Appel récursif au niveau MIN (profondeur + 1)
			value = Math.max(value, minValue(nextBoard, depth + 1));
		}
		return value;
	}

	private int minValue(Board board, int depth) {
		// Compte ce nœud comme étant développé
		this.nbNodes++;

		// 1. Condition d'arrêt: Fin de partie ou horizon de recherche atteint
		if (board.isGameOver() || depth >= depthMax) {
			this.nbLeaves++;
			// On retourne l'évaluation heuristique (du point de vue de MAX)
			return h.eval(board, playerMaxRole);
		}

		int value = IHeuristic.MAX_VALUE;
		Iterable<Move> moves = board.possibleMoves(playerMinRole);

		for (Move m : moves) {
			Board nextBoard = board.play(m, playerMinRole);
			// 2. Appel récursif au niveau MAX (profondeur + 1)
			value = Math.min(value, maxValue(nextBoard, depth + 1));
		}
		return value;
	}
}
