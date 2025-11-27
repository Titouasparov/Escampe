package games.ecampe;

import iialib.games.model.escampe.Partie1;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class EscampeBoard implements Partie1 {

    // --- CONSTANTES ---
    private static final int[][] LISERES = {
            {1, 2, 2, 3, 1, 2},
            {3, 1, 3, 1, 3, 2},
            {2, 3, 1, 2, 1, 3},
            {2, 1, 3, 2, 3, 1},
            {1, 3, 1, 3, 1, 2},
            {3, 2, 2, 1, 3, 2}
    };

    public static final int VIDE = 0;
    public static final int PALADIN_BLANC = 1;
    public static final int LICORNE_BLANCHE = 2;
    public static final int PALADIN_NOIR = -1;
    public static final int LICORNE_NOIRE = -2;

    private int[][] posPieces;
    private int lisereCourant;
    private String joueurCourant;

    public EscampeBoard() {
        this.posPieces = new int[6][6];
        this.lisereCourant = 0;
        this.joueurCourant = "blanc";
    }

    // --- ACCESSEURS ---
    public int getLisere(int x, int y) {
        return isValidCoordinate(x, y) ? LISERES[y][x] : -1;
    }

    public int getPiece(int x, int y) {
        return isValidCoordinate(x, y) ? posPieces[y][x] : 0;
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < 6 && y >= 0 && y < 6;
    }

    // --- CONVERSIONS & I/O (Identique avant) ---
    private char pieceToChar(int piece) {
        switch (piece) {
            case LICORNE_NOIRE:
                return 'N';
            case PALADIN_NOIR:
                return 'n';
            case LICORNE_BLANCHE:
                return 'B';
            case PALADIN_BLANC:
                return 'b';
            default:
                return '-';
        }
    }

    private int charToPiece(char c) {
        switch (c) {
            case 'N':
                return LICORNE_NOIRE;
            case 'n':
                return PALADIN_NOIR;
            case 'B':
                return LICORNE_BLANCHE;
            case 'b':
                return PALADIN_BLANC;
            case '-':
                return VIDE;
            default:
                return VIDE;
        }
    }

    @Override
    public void setFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("%")) continue;
                if (Character.isDigit(line.charAt(0))) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        int rowNum = Integer.parseInt(parts[0]);
                        int y = 6 - rowNum;
                        for (int x = 0; x < 6; x++) {
                            this.posPieces[y][x] = charToPiece(parts[1].charAt(x));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveToFile(String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write("% Sauvegarde");
            bw.newLine();
            for (int y = 0; y < 6; y++) {
                int rowNum = 6 - y;
                String numStr = String.format("%02d", rowNum);
                bw.write(numStr + " ");
                for (int x = 0; x < 6; x++) bw.write(pieceToChar(posPieces[y][x]));
                bw.write(" " + numStr);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] parseCoordinate(String coord) {
        if (coord == null || coord.length() != 2) return null;
        int x = coord.charAt(0) - 'A';
        int y = 6 - (coord.charAt(1) - '0');
        if (!isValidCoordinate(x, y)) return null;
        return new int[]{y, x};
    }

    private String coordToString(int x, int y) {
        if (!isValidCoordinate(x, y)) return null;
        return "" + (char) ('A' + x) + (6 - y);
    }

    // --- LOGIQUE DE VALIDATION (Corrigée pour accepter les virages) ---

    @Override
    public boolean isValidMove(String move, String player) {
        if (move.equals("E") || move.contains("/")) return true;

        String[] parts = move.split("-");
        if (parts.length != 2) return false;
        int[] start = parseCoordinate(parts[0]);
        int[] end = parseCoordinate(parts[1]);
        if (start == null || end == null) return false;

        int y1 = start[0], x1 = start[1];
        int y2 = end[0], x2 = end[1];

        // 1. Vérif Propriétaire
        int piece = posPieces[y1][x1];
        if (piece == VIDE) return false;
        boolean isWhite = (piece > 0);
        if (isWhite != player.equalsIgnoreCase("blanc")) return false;

        // 2. Vérif Liseré Imposé
        int lisereDepart = getLisere(x1, y1);
        if (lisereCourant != 0 && lisereDepart != lisereCourant) return false;

        // 3. Vérif Case Arrivée (Tir fratricide / Paladin imprenable)
        int target = posPieces[y2][x2];
        if (target != VIDE) {
            if ((target > 0) == isWhite) return false; // Ami
            if (Math.abs(target) == PALADIN_BLANC) return false; // Paladin adverse
        }

        // 4. Vérif Chemin (Pathfinding)
        // Existe-t-il un chemin de longueur 'lisereDepart' allant de Départ à Arrivée ?
        // Contraintes : Pas de diagonale, cases intermédiaires vides, pas de retour arrière.
        return existsPath(x1, y1, x2, y2, lisereDepart, new HashSet<>());
    }

    /**
     * Vérifie récursivement si un chemin existe.
     *
     * @param cx      X courant
     * @param cy      Y courant
     * @param tx      X cible
     * @param ty      Y cible
     * @param steps   Pas restants
     * @param visited Cases visitées dans ce trajet
     */
    private boolean existsPath(int cx, int cy, int tx, int ty, int steps, Set<String> visited) {
        String posKey = cx + "," + cy;
        visited.add(posKey);

        // Cas de base : on a épuisé les pas
        if (steps == 0) {
            // On renvoie Vrai si on est sur la cible
            return (cx == tx && cy == ty);
        }

        // Exploration des 4 voisins orthogonaux
        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] d : dirs) {
            int nx = cx + d[0];
            int ny = cy + d[1];

            // 1. Dans le plateau ?
            if (!isValidCoordinate(nx, ny)) continue;

            // 2. Pas déjà visité (interdiction de repasser par la même case)
            if (visited.contains(nx + "," + ny)) continue;

            // 3. Case intermédiaire doit être VIDE
            // (Sauf si c'est la toute dernière case du chemin, où on a le droit de manger)
            if (steps > 1 && posPieces[ny][nx] != VIDE) continue;

            // Appel récursif
            // On crée une COPIE du set visited pour la branche suivante (ou on retire après)
            // Ici pour simplifier on retire après (backtracking)
            if (existsPath(nx, ny, tx, ty, steps - 1, visited)) {
                return true;
            }
        }

        visited.remove(posKey); // Backtracking
        return false;
    }

    // --- GÉNÉRATION DES COUPS (Corrigée avec Pathfinding) ---

    @Override
    public String[] possiblesMoves(String player) {
        ArrayList<String> moves = new ArrayList<>();
        boolean isWhiteTurn = player.equalsIgnoreCase("blanc");

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                int piece = posPieces[y][x];
                if (piece == VIDE) continue;
                if ((piece > 0) != isWhiteTurn) continue; // Pas ma pièce

                int lisere = getLisere(x, y);
                if (lisereCourant != 0 && lisere != lisereCourant) continue;

                // On lance la recherche de toutes les destinations possibles
                Set<String> destinations = new HashSet<>();
                findDestinations(x, y, lisere, new HashSet<>(), destinations, isWhiteTurn);

                String startStr = coordToString(x, y);
                for (String destStr : destinations) {
                    moves.add(startStr + "-" + destStr);
                }
            }
        }
        return moves.toArray(new String[0]);
    }

    private void findDestinations(int cx, int cy, int steps, Set<String> visited, Set<String> foundDests, boolean isWhite) {
        String posKey = cx + "," + cy;
        visited.add(posKey);

        if (steps == 0) {
            // Fin du trajet : c'est une destination valide
            foundDests.add(coordToString(cx, cy));
            visited.remove(posKey);
            return;
        }

        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] d : dirs) {
            int nx = cx + d[0];
            int ny = cy + d[1];

            if (!isValidCoordinate(nx, ny)) continue;
            if (visited.contains(nx + "," + ny)) continue;

            // Vérification obstacle
            int targetPiece = posPieces[ny][nx];

            if (steps > 1) {
                // Case intermédiaire : DOIT être vide
                if (targetPiece == VIDE) {
                    findDestinations(nx, ny, steps - 1, visited, foundDests, isWhite);
                }
            } else {
                // Dernière case (steps == 1 -> 0) : PEUT être vide OU capture valide
                boolean canLand = false;
                if (targetPiece == VIDE) canLand = true;
                else {
                    boolean isTargetWhite = (targetPiece > 0);
                    // Capture ennemi autorisée SI ce n'est pas un paladin
                    if (isTargetWhite != isWhite && Math.abs(targetPiece) != PALADIN_BLANC) {
                        canLand = true;
                    }
                }

                if (canLand) {
                    findDestinations(nx, ny, steps - 1, visited, foundDests, isWhite);
                }
            }
        }
        visited.remove(posKey);
    }

    // --- PLAY & GAMEOVER (Inchangés) ---
    @Override
    public void play(String move, String player) {
        if (move.equals("E")) {
            this.lisereCourant = 0;
            this.joueurCourant = player.equalsIgnoreCase("blanc") ? "noir" : "blanc";
            return;
        }
        if (move.contains("/")) {
            String[] positions = move.split("/");
            int licorneVal = player.equalsIgnoreCase("blanc") ? LICORNE_BLANCHE : LICORNE_NOIRE;
            int[] l = parseCoordinate(positions[0]);
            if (l != null) posPieces[l[0]][l[1]] = licorneVal;

            int paladinVal = player.equalsIgnoreCase("blanc") ? PALADIN_BLANC : PALADIN_NOIR;
            for (int i = 1; i < positions.length; i++) {
                int[] p = parseCoordinate(positions[i]);
                if (p != null) posPieces[p[0]][p[1]] = paladinVal;
            }
            this.lisereCourant = 0;
            this.joueurCourant = player.equalsIgnoreCase("blanc") ? "noir" : "blanc";
            return;
        }

        String[] parts = move.split("-");
        int[] start = parseCoordinate(parts[0]);
        int[] end = parseCoordinate(parts[1]);
        int piece = posPieces[start[0]][start[1]];
        posPieces[start[0]][start[1]] = VIDE;
        posPieces[end[0]][end[1]] = piece;
        this.lisereCourant = LISERES[end[0]][end[1]];
        this.joueurCourant = player.equalsIgnoreCase("blanc") ? "noir" : "blanc";
    }

    @Override
    public boolean gameOver() {
        boolean lb = false, ln = false;
        for (int[] row : posPieces) {
            for (int p : row) {
                if (p == LICORNE_BLANCHE) lb = true;
                if (p == LICORNE_NOIRE) ln = true;
            }
        }
        return !(lb && ln);
    }

    // --- MAIN DE TEST (Mis à jour pour tester les virages) ---
    public void printBoard() {
        System.out.println("     A B C D E F");
        System.out.println("   +-------------+");
        for (int y = 0; y < 6; y++) {
            System.out.print(" " + (6 - y) + " | ");
            for (int x = 0; x < 6; x++) {
                char c = pieceToChar(posPieces[y][x]);
                System.out.print((c == '-' ? '.' : c) + " ");
            }
            System.out.println("| " + (6 - y));
        }
    }

    // --- PROGRAMME PRINCIPAL DE TEST ---
    public static void main(String[] args) {
        EscampeBoard board = new EscampeBoard();


        // ---------------------------------------------------------------
        // TEST DE CHARGEMENT SUR LES 4 FICHIERS
        // ---------------------------------------------------------------
        System.out.println("===== TESTS DE CHARGEMENT DES FICHIERS =====\n");
        String[] filesToTest = {"test_input1.txt", "test_input2.txt", "test_input3.txt", "test_input4.txt"};

        for (String fileName : filesToTest) {
            System.out.println("--- Chargement de : " + fileName + " ---");
            board.setFromFile(fileName);
            board.printBoard();
            System.out.println("Lecture OK.\n");
        }

        // ---------------------------------------------------------------
        // TEST DE JEU (PLAY) ET SAUVEGARDE
        // ---------------------------------------------------------------
        System.out.println("===== TESTS DE FAIRE DES COUPS + SAUVEGARDE DE FICHIERS =====\n");

        // Avec le fichier test_input2.txt on fait le coup F6-F4 QUI EST INVALIDE
        System.out.println("1. Chargement de l'état initial (test_input2.txt)...");
        board.setFromFile("test_input2.txt");
        board.printBoard();

        String coup = "F6-F4";
        String joueur = "blanc";

        System.out.println("2. Le joueur " + joueur + " tente de jouer : " + coup);
        System.out.println("   (Analyse : Départ F6 sur Liseré " + board.getLisere(5, 0) + " -> Distance requise : 2)");
        if (board.isValidMove(coup, joueur)) {
            board.play(coup, joueur);
            System.out.println(">> Coup VALIDE et JOUÉ.");
        } else {
            System.out.println(">> ERREUR : Coup considéré invalide .");
        }

        //PUIS AVEC UN COUP VALIDE : F6-E5
        coup = "F6-E5";
        joueur = "blanc";

        System.out.println("2. Le joueur " + joueur + " tente de jouer : " + coup);
        System.out.println("   (Analyse : Départ F6 sur Liseré " + board.getLisere(5, 0) + " -> Distance requise : 2)");
        if (board.isValidMove(coup, joueur)) {
            board.play(coup, joueur);
            System.out.println(">> Coup VALIDE et JOUÉ.");
        } else {
            System.out.println(">> ERREUR : Coup considéré invalide .");
        }

        System.out.println("\n3. État du plateau après le coup :");
        board.printBoard();

        // 3. Sauvegarde
        String saveName = "test_output.txt";
        System.out.println("4. Sauvegarde de l'état final dans '" + saveName + "'...");
        board.saveToFile(saveName);

        // 4. Vérification (Relecture)
        System.out.println("5. Vérification : Relecture du fichier sauvegardé...");
        EscampeBoard checkBoard = new EscampeBoard();
        checkBoard.setFromFile(saveName);
        // On vérifie que la pièce est bien arrivée en F4 (x=5, y=2)
        // F4 correspond à la ligne 4 (index 2 dans le tableau)
        int pieceF4 = checkBoard.getPiece(4, 1);
        if (pieceF4 == PALADIN_BLANC) {
            System.out.println(">> SUCCÈS : La sauvegarde contient bien le Paladin Blanc en F4 !");
        } else {
            System.out.println(">> ÉCHEC : La pièce en F4 est " + pieceF4 + " (Attendu : 1)");
        }

        // ---------------------------------------------------------------
        // AFFICHAGE DES COUPS DISPONIBLES
        // ---------------------------------------------------------------
        System.out.println("\n===== TESTS D'AFFICHAGE DES COUPS POSSIBLES =====");

        // Boucle sur les fichiers pour afficher les coups sans contrainte particulière (lisereCourant = 0 par défaut au chargement)
        for (String fileName : filesToTest) {
            System.out.println("\n--- Analyse des coups pour : " + fileName + " ---");
            board.setFromFile(fileName);
            // On réinitialise le liseré courant à 0 pour simuler un début de tour libre
            // (Note: setFromFile ne change pas lisereCourant, il faut le faire si on veut tester "à vide")
            board.lisereCourant = 0;

            String[] movesBlanc = board.possiblesMoves("blanc");
            String[] movesNoir = board.possiblesMoves("noir");

            board.printBoard();

            System.out.println("Coups Blancs (" + movesBlanc.length + ") : " + Arrays.toString(movesBlanc));
            System.out.println("Coups Noirs  (" + movesNoir.length + ") : " + Arrays.toString(movesNoir));
        }

        // ---------------------------------------------------------------
        // FICHIER 4 AVEC CONTRAINTE IMPOSÉE
        // ---------------------------------------------------------------
        System.out.println("\n--- Test Spécial : Contrainte sur test_input4.txt ---");
        board.setFromFile("test_input4.txt");

        // Configuration du test_input4 :
        // - Blanc a un Paladin 'b' en A1 (Liseré 3)
        // - Blanc a une Licorne 'B' en F3 (Liseré 1)

        // CAS 1 : On impose le Liseré 1
        board.lisereCourant = 1;
        System.out.println(">> IMPOSITION LISERÉ COURANT = 1");
        System.out.println("   (Seule la Licorne 'B' en F3 est sur un liseré 1)");

        String[] movesContraints1 = board.possiblesMoves("blanc");
        String movesContraints1_str = Arrays.toString(movesContraints1);
        board.printBoard();
        System.out.println("Coups trouvés : " + movesContraints1_str);


        if (Objects.equals(movesContraints1_str, "[F3-F2, F3-E3]")) {
            System.out.println(">> SUCCÈS : Filtre OK (Seulement F3 proposé et il peut aller en F2 ou E3).");
        }
        else {
            System.out.println(">> ÉCHEC : Filtre incorrect.");
        }


        // CAS 2 : On impose le Liseré 3
        board.lisereCourant = 3;
        System.out.println("\n>> IMPOSITION LISERÉ COURANT = 3");
        System.out.println("   (Seul le Paladin 'b' en A1 est sur un liseré 3 et il peutu uniquement aller en B3)");

        String[] movesContraints3 = board.possiblesMoves("blanc");
        String movesContraints3_str = Arrays.toString(movesContraints3);
        board.printBoard();
        System.out.println("Coups trouvés : " + movesContraints3_str);

        if (Objects.equals(movesContraints3_str, "[A1-B3]")) {
            System.out.println(">> SUCCÈS : Filtre OK (Seulement A1 proposé).");
        }
        else {
            System.out.println(">> ÉCHEC : Filtre incorrect.");
        }



        System.out.println("\n=== FIN DES TESTS COMPLETS ===");
    }
}