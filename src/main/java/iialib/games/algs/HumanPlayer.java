package iialib.games.algs;

import iialib.games.model.IBoard;
import iialib.games.model.IMove;
import iialib.games.model.IRole;
import iialib.games.model.Player;

import java.util.ArrayList;
import java.util.Scanner;

public class HumanPlayer<Move extends IMove, Role extends IRole, Board extends IBoard<Move, Role, Board>> extends Player<Role> {

    private Scanner scanner;

    public HumanPlayer(Role role) {
        super(role);
        this.scanner = new Scanner(System.in);
    }

    public Move bestMove(Board board) {
        ArrayList<Move> possibleMoves = (ArrayList<Move>) board.possibleMoves(this.getRole());

        if (possibleMoves.isEmpty()) {
            System.out.println("No possible moves!");
            return null;
        }

        System.out.println("\n=== Your turn (" + this.getRole() + ") ===");
        System.out.println("Available moves:");
        for (int i = 0; i < possibleMoves.size(); i++) {
            System.out.println("  [" + i + "] " + possibleMoves.get(i));
        }

        int choice = -1;
        while (choice < 0 || choice >= possibleMoves.size()) {
            System.out.print("Enter your choice (0-" + (possibleMoves.size() - 1) + "): ");
            try {
                choice = scanner.nextInt();
                if (choice < 0 || choice >= possibleMoves.size()) {
                    System.out.println("Invalid choice! Please enter a number between 0 and " + (possibleMoves.size() - 1));
                }
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine(); // Clear the buffer
                choice = -1;
            }
        }

        return possibleMoves.get(choice);
    }

    public Board playMove(Board board, Move move) {
        return board.play(move, this.getRole());
    }
}

