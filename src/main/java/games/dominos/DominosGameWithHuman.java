package games.dominos;

import java.util.ArrayList;
import java.util.Scanner;

import iialib.games.algs.AIPlayer;
import iialib.games.algs.HumanPlayer;
import iialib.games.algs.GameAlgorithm;
import iialib.games.algs.algorithms.AlphaBeta;
import iialib.games.algs.algorithms.MiniMax;
import iialib.games.model.Player;
import iialib.games.model.Score;

public class DominosGameWithHuman {

	private DominosBoard currentBoard;
	private ArrayList<Player<DominosRole>> players;

	public DominosGameWithHuman(ArrayList<Player<DominosRole>> players, DominosBoard initialBoard) {
		this.currentBoard = initialBoard;
		this.players = players;
	}

	public void runGame() {
		int index = 0;
		Player<DominosRole> currentPlayer = players.get(index);
		System.out.println("Game beginning - First player is: " + currentPlayer);
		System.out.println("The board is:");
		System.out.println(currentBoard);

		while (!currentBoard.isGameOver()) {
			System.out.println("\n===========================================");
			System.out.println("Next player is: " + currentPlayer);

			DominosMove nextMove = null;

			if (currentPlayer instanceof AIPlayer) {
				@SuppressWarnings("unchecked")
				AIPlayer<DominosMove, DominosRole, DominosBoard> aiPlayer =
					(AIPlayer<DominosMove, DominosRole, DominosBoard>) currentPlayer;
				nextMove = aiPlayer.bestMove(currentBoard);
			} else if (currentPlayer instanceof HumanPlayer) {
				@SuppressWarnings("unchecked")
				HumanPlayer<DominosMove, DominosRole, DominosBoard> humanPlayer =
					(HumanPlayer<DominosMove, DominosRole, DominosBoard>) currentPlayer;
				nextMove = humanPlayer.bestMove(currentBoard);
			}

			if (nextMove == null) {
				System.out.println("No move returned for player " + currentPlayer + ". Ending game.");
				break;
			}

			System.out.println("Move chosen: " + nextMove);
			currentBoard = currentBoard.play(nextMove, currentPlayer.getRole());
			System.out.println("The board is:");
			System.out.println(currentBoard);

			index = 1 - index;
			currentPlayer = players.get(index);
		}

		System.out.println("\n===========================================");
		System.out.println("Game over!");
		ArrayList<Score<DominosRole>> scores = currentBoard.getScores();
		for (Player<DominosRole> p : players) {
			for (Score<DominosRole> s : scores) {
				if (p.getRole() == s.getRole()) {
					System.out.println("" + p + " score is: " + s.getStatus() + " " + s.getScore());
				}
			}
		}
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.println("=== Dominos Game ===");
		System.out.println("Choose game mode:");
		System.out.println("1. Human vs AI");
		System.out.println("2. AI vs AI");
		System.out.print("Your choice (1 or 2): ");

		int mode = 1;
		try {
			mode = scanner.nextInt();
		} catch (Exception e) {
			System.out.println("Invalid input, defaulting to Human vs AI");
			mode = 1;
		}

		DominosRole roleV = DominosRole.VERTICAL;
		DominosRole roleH = DominosRole.HORIZONTAL;

		ArrayList<Player<DominosRole>> players = new ArrayList<>();

		if (mode == 1) {
			// Human vs AI
			System.out.println("\nChoose your role:");
			System.out.println("1. VERTICAL (plays vertically |)");
			System.out.println("2. HORIZONTAL (plays horizontally -)");
			System.out.print("Your choice (1 or 2): ");

			int roleChoice = 1;
			try {
				roleChoice = scanner.nextInt();
			} catch (Exception e) {
				System.out.println("Invalid input, defaulting to VERTICAL");
				roleChoice = 1;
			}

			System.out.print("\nChoose AI difficulty (depth 1-6, recommended 3-4): ");
			int aiDepth = 3;
			try {
				aiDepth = scanner.nextInt();
				if (aiDepth < 1) aiDepth = 1;
				if (aiDepth > 6) aiDepth = 6;
			} catch (Exception e) {
				System.out.println("Invalid input, defaulting to depth 3");
				aiDepth = 3;
			}

			if (roleChoice == 1) {
				// Human is VERTICAL
				HumanPlayer<DominosMove, DominosRole, DominosBoard> humanPlayer =
					new HumanPlayer<>(roleV);

				GameAlgorithm<DominosMove, DominosRole, DominosBoard> algH =
					new AlphaBeta<>(roleH, roleV, DominosHeuristics.hHorizontal, aiDepth);
				AIPlayer<DominosMove, DominosRole, DominosBoard> aiPlayer =
					new AIPlayer<>(roleH, algH);

				players.add(humanPlayer); // Human plays first (VERTICAL)
				players.add(aiPlayer);
			} else {
				// Human is HORIZONTAL
				GameAlgorithm<DominosMove, DominosRole, DominosBoard> algV =
					new AlphaBeta<>(roleV, roleH, DominosHeuristics.hVertical, aiDepth);
				AIPlayer<DominosMove, DominosRole, DominosBoard> aiPlayer =
					new AIPlayer<>(roleV, algV);

				HumanPlayer<DominosMove, DominosRole, DominosBoard> humanPlayer =
					new HumanPlayer<>(roleH);

				players.add(aiPlayer); // AI plays first (VERTICAL)
				players.add(humanPlayer);
			}

			System.out.println("\nStarting Human vs AI game!");
			System.out.println("AI difficulty: depth " + aiDepth);

		} else {
			// AI vs AI
			GameAlgorithm<DominosMove, DominosRole, DominosBoard> algV =
				new MiniMax<>(roleV, roleH, DominosHeuristics.hVertical, 3);
			GameAlgorithm<DominosMove, DominosRole, DominosBoard> algH =
				new AlphaBeta<>(roleH, roleV, DominosHeuristics.hHorizontal, 3);

			AIPlayer<DominosMove, DominosRole, DominosBoard> playerV =
				new AIPlayer<>(roleV, algV);
			AIPlayer<DominosMove, DominosRole, DominosBoard> playerH =
				new AIPlayer<>(roleH, algH);

			players.add(playerV);
			players.add(playerH);

			System.out.println("\nStarting AI vs AI game!");
		}

		DominosBoard initialBoard = new DominosBoard();
		DominosGameWithHuman game = new DominosGameWithHuman(players, initialBoard);
		game.runGame();

		scanner.close();
	}
}

