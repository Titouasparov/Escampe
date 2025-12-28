package escampe;

import iialib.games.algs.IHeuristic;
import java.util.HashMap;

public class EscampeHeuristique implements IHeuristic<EscampeBoard, EscampeRole> {

    @Override
    public int eval(EscampeBoard board, EscampeRole role) {
        // 1. Vérification de victoire/défaite immédiate
        if (board.gameOver()) {
            boolean licorneBlanchePresente = false;
            boolean licorneNoirePresente = false;
            for (int y = 0; y < 6; y++) {
                for (int x = 0; x < 6; x++) {
                    int p = board.getPiece(x, y);
                    if (p == EscampeBoard.LICORNE_BLANCHE) licorneBlanchePresente = true;
                    if (p == EscampeBoard.LICORNE_NOIRE) licorneNoirePresente = true;
                }
            }
            if (role.getName().equals("blanc")) {
                return licorneBlanchePresente ? MAX_VALUE : MIN_VALUE;
            } else {
                return licorneNoirePresente ? MAX_VALUE : MIN_VALUE;
            }
        }

        // 2. Initialisation des variables
        int mesCoups = board.possibleMoves(role).size(); //
        String couleurAdverse = role.getName().equals("blanc") ? "noir" : "blanc";
        EscampeRole roleAdverse = new EscampeRole(couleurAdverse);
        int coupsAdverse = board.possibleMoves(roleAdverse).size(); //

        double entropie = 0.0;
        HashMap<Integer, Integer> repartition = new HashMap<>();
        int totalPieces = 0;

        int myLicorneX = -1, myLicorneY = -1;
        int oppLicorneX = -1, oppLicorneY = -1;

        int myLicorneVal = role.getName().equals("blanc") ? EscampeBoard.LICORNE_BLANCHE : EscampeBoard.LICORNE_NOIRE; //
        int oppLicorneVal = role.getName().equals("blanc") ? EscampeBoard.LICORNE_NOIRE : EscampeBoard.LICORNE_BLANCHE; //

        // 3. Parcours du plateau pour l'entropie et la position des licornes
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                int piece = board.getPiece(x, y);
                if (piece != EscampeBoard.VIDE) { //
                    if (piece == myLicorneVal) { myLicorneX = x; myLicorneY = y; }
                    if (piece == oppLicorneVal) { oppLicorneX = x; oppLicorneY = y; }

                    if ((piece > 0 && role.getName().equals("blanc")) || (piece < 0 && role.getName().equals("noir"))) {
                        int lisere = board.getLisere(x, y); //
                        repartition.put(lisere, repartition.getOrDefault(lisere, 0) + 1);
                        totalPieces++;
                    }
                }
            }
        }

        // 4. Calcul de l'entropie de Shannon
        if (totalPieces > 0) {
            for (Integer count : repartition.values()) {
                double p = (double) count / totalPieces;
                entropie -= p * (Math.log(p) / Math.log(2));
            }
        }

        // 5. Calcul Sécurité et Pression via la méthode privée
        int danger = 0;
        int pression = 0;
        if (myLicorneX != -1) danger = countThreatsTo(board, myLicorneX, myLicorneY, couleurAdverse);
        if (oppLicorneX != -1) pression = countThreatsTo(board, oppLicorneX, oppLicorneY, role.getName());

        // 6. Score Final Pondéré
        int scoreMobilite = (mesCoups - coupsAdverse) * 2;
        int scoreEntropie = (int) (entropie * 60);
        int scoreSecurite = danger * -300;   // Malus pour chaque menace sur ma licorne
        int scorePression = pression * 200;  // Bonus pour chaque menace sur la sienne

        return scoreMobilite + scoreEntropie + scoreSecurite + scorePression;
    }

    /**
     * Méthode privée pour compter les menaces sur une case cible
     */
    private int countThreatsTo(EscampeBoard board, int tx, int ty, String joueurAttaquant) {
        int threats = 0;
        String targetCoord = indexToCoord(tx, ty);

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                int piece = board.getPiece(x, y);
                // Seul un paladin (valeur absolue 1) peut capturer une licorne
                if (Math.abs(piece) == 1) {
                    boolean isWhite = (piece > 0);
                    if (isWhite == joueurAttaquant.equalsIgnoreCase("blanc")) {
                        String startCoord = indexToCoord(x, y);
                        // On utilise la validation officielle du plateau
                        if (board.isValidMove(startCoord + "-" + targetCoord, joueurAttaquant)) {
                            threats++;
                        }
                    }
                }
            }
        }
        return threats;
    }

    /**
     * Helper pour convertir (x,y) en "A1"
     */
    private String indexToCoord(int x, int y) {
        return "" + (char) ('A' + x) + (6 - y);
    }
}