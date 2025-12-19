package escampe;

import iialib.games.model.escampe.Partie1;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class EscampeBoard implements Partie1 {

    // --- CONSTANTES ---
    //tableau des liserés
    private static final int[][] LISERES = {
            {1, 2, 2, 3, 1, 2},
            {3, 1, 3, 1, 3, 2},
            {2, 3, 1, 2, 1, 3},
            {2, 1, 3, 2, 3, 1},
            {1, 3, 1, 3, 1, 2},
            {3, 2, 2, 1, 3, 2}
    };

    // Valeurs des pièces
    public static final int VIDE = 0;
    public static final int PALADIN_BLANC = 1;
    public static final int LICORNE_BLANCHE = 2;
    public static final int PALADIN_NOIR = -1;
    public static final int LICORNE_NOIRE = -2;

    private int[][] posPieces;
    private int lisereCourant;

    private String joueurCourant;

    // --- CONSTRUCTEUR ---
    public EscampeBoard() {
        this.posPieces = new int[6][6];
        this.lisereCourant = 0;
        this.joueurCourant = "blanc";
    }

    // --- GETTER ---
    public int getLisere(int x, int y) {
        return isValidCoordinate(x, y) ? LISERES[y][x] : -1;
    }

    public int getPiece(int x, int y) {
        return isValidCoordinate(x, y) ? posPieces[y][x] : 0;
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < 6 && y >= 0 && y < 6;
    }

    // --- CONVERSIONS & I/O ---
    /** Convertit une pièce en caractère pour l'affichage/sauvegarde.
     * exemple: on passe de l'entier 2 (LICORNE_BLANCHE) au caractère 'B'.
     */
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

    /** Convertit un caractère en pièce pour le chargement.
     * exemple: on passe du caractère 'B' à l'entier 2 (LICORNE_BLANCHE).
     */
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

    // --- LECTURE & SAUVEGARDE DE FICHIERS ---

    /*  Lecture d'un fichier texte
    Exemple de format de fichier :
     %
     % Sauvegarde
     %
     06 bbbbbb 06
     05 ------ 05
     04 ------ 04
     03 ------ 03
     02 ------ 02
     01 BBBBBB 01
     */
    @Override
    public void setFromFile(String fileName) {
        // On initialise un buffer de lecture
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();// Nettoyage des espaces
                if (line.isEmpty() || line.startsWith("%")) continue;// Ignorer les commentaires
                if (Character.isDigit(line.charAt(0))) {
                    String[] parts = line.split("\\s+");// separer par espaces, tabulations
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

    /** Sauvegarde de l'état actuel dans un fichier texte
     */
    @Override
    public void saveToFile(String fileName) {
        // On initialise un buffer d'écriture
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

    // --- CONVERSION COORDONNÉES ---

    /** Convertit une coordonnée de type "A1" en indices de tableau [y,x]
     * exemple: "A1" -> [5,0]
     * */
    private int[] parseCoordinate(String coord) {
        if (coord == null || coord.length() != 2) return null;
        int x = coord.charAt(0) - 'A';
        int y = 6 - (coord.charAt(1) - '0');
        if (!isValidCoordinate(x, y)) return null;
        return new int[]{y, x};
    }

    /** Convertit des indices de tableau [x,y] en coordonnée de type "A1"
     * exemple: [5,0] -> "A1"
     * */
    private String coordToString(int x, int y) {
        if (!isValidCoordinate(x, y)) return null;
        return "" + (char) ('A' + x) + (6 - y);
    }

    // --- VALIDATION DE COUPS  ---

    /** Vérifie si un coup est valide selon les règles du jeu.
     *
     * @param move   Le coup au format "A1-B2", "E" (passer), ou "A1/A2/A3" (placement)
     * @param player "blanc" ou "noir"
     * @return true si le coup est valide, false sinon
     */
    @Override
    public boolean isValidMove(String move, String player) {

        // Cas spéciaux : Passer ou Placement
        if (move.equals("E") || move.contains("/")) return true;

        // Coup normal : "A1-B2"
        String[] parts = move.split("-");

        if (parts.length != 2) return false;

        // Conversion des coordonnées : "A1" -> [y,x]
        int[] start = parseCoordinate(parts[0]);
        int[] end = parseCoordinate(parts[1]);
        if (start == null || end == null) return false;

        // Extraction des indices
        int y1 = start[0], x1 = start[1];
        int y2 = end[0], x2 = end[1];

        // === Verifications de la validité du coup ===
        // Vérif Pièce Départ (Existante et Appartient au Joueur)
        int piece = posPieces[y1][x1];
        if (piece == VIDE) return false;
        boolean isWhite = (piece > 0);
        if (isWhite != player.equalsIgnoreCase("blanc")) return false;

        // Vérif Liseré Imposé s'il y en a un
        int lisereDepart = getLisere(x1, y1);
        if (lisereCourant != 0 && lisereDepart != lisereCourant) return false;

        // Vérif Case Arrivée (Tir fratricide / Paladin imprenable)
        int target = posPieces[y2][x2];
        if (target != VIDE) {
            if ((target > 0) == isWhite) return false; // Ami
            if (Math.abs(target) == PALADIN_BLANC) return false; // Paladin adverse
        }

        // Vérif Chemin
        // Existe-t-il un chemin de longueur 'lisereDepart' allant de Départ à Arrivée ?
        // Contraintes : Pas de diagonale, cases intermédiaires vides, pas de retour arrière.
        return existsPath(x1, y1, x2, y2, lisereDepart, new HashSet<>());
    }

    /**
     * Vérifie récursivement si un chemin existe vers une case cible.
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
        visited.add(posKey);// Marquer la case courante comme visitée

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

            // Dans le plateau ?
            if (!isValidCoordinate(nx, ny)) continue;

            // Pas déjà visité (interdiction de repasser par la même case)
            if (visited.contains(nx + "," + ny)) continue;

            // Case intermédiaire doit être VIDE
            // (Sauf si c'est la toute dernière case du chemin, où on a le droit de manger)
            if (steps > 1 && posPieces[ny][nx] != VIDE) continue;

            // Appel récursif pour le voisin
            if (existsPath(nx, ny, tx, ty, steps - 1, visited)) {
                return true;
            }
        }

        // Aucun chemin trouvé, on demarque la case comme non visitée pour d'autres trajets
        visited.remove(posKey);
        return false;
    }

    // --- GÉNÉRATION DES COUPS POSSIBLES ---

    /** Génère tous les coups possibles pour le joueur donné.
     *
     * @param player "blanc" ou "noir"
     * @return tableau de coups possibles au format "A1-B2"
     */
    @Override
    public String[] possiblesMoves(String player) {
        // Initialisation de la liste des coups
        ArrayList<String> moves = new ArrayList<>();
        boolean isWhiteTurn = player.equalsIgnoreCase("blanc");

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                // Pièce présente ? Appartient au joueur courant ?
                int piece = posPieces[y][x];
                if (piece == VIDE) continue;
                if ((piece > 0) != isWhiteTurn) continue;

                // Vérif s'il y a un Liseré Imposé
                int lisere = getLisere(x, y);
                if (lisereCourant != 0 && lisere != lisereCourant) continue;

                // On lance la recherche de toutes les destinations possibles
                Set<String> destinations = new HashSet<>();
                findDestinations(x, y, lisere, new HashSet<>(), destinations, isWhiteTurn);

                // On ajoute les coups à la liste
                String startStr = coordToString(x, y);
                for (String destStr : destinations) {
                    moves.add(startStr + "-" + destStr);
                }
            }
        }
        return moves.toArray(new String[0]);
    }

    /**
     * Recherche récursive de toutes les destinations possibles depuis une position donnée sans verifier la validité des coups.
     *
     * @param cx          X courant
     * @param cy          Y courant
     * @param steps       Pas restants
     * @param visited     Cases visitées dans ce trajet
     * @param foundDests  Ensemble des destinations trouvées
     * @param isWhite     Couleur du joueur courant
     */
    private void findDestinations(int cx, int cy, int steps, Set<String> visited, Set<String> foundDests, boolean isWhite) {
        String posKey = cx + "," + cy;
        visited.add(posKey);

        // Cas de base : on a épuisé les pas
        if (steps == 0) {
            // Fin du trajet : c'est une destination valide
            foundDests.add(coordToString(cx, cy));
            visited.remove(posKey);
            return;
        }

        // Exploration des 4 voisins orthogonaux
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

    // --- PLAY & GAMEOVER ---
    @Override
    public void play(String move, String player) {
        // Passer
        if (move.equals("E")) {
            this.lisereCourant = 0;
            this.joueurCourant = player.equalsIgnoreCase("blanc") ? "noir" : "blanc";
            return;
        }
        // Placement
        if (move.contains("/")) {
            String[] positions = move.split("/");
            // Placer la licorne
            int licorneVal = player.equalsIgnoreCase("blanc") ? LICORNE_BLANCHE : LICORNE_NOIRE;
            int[] l = parseCoordinate(positions[0]);
            if (l != null) posPieces[l[0]][l[1]] = licorneVal;

            // Placer les paladins
            int paladinVal = player.equalsIgnoreCase("blanc") ? PALADIN_BLANC : PALADIN_NOIR;
            for (int i = 1; i < positions.length; i++) {
                int[] p = parseCoordinate(positions[i]);
                if (p != null) posPieces[p[0]][p[1]] = paladinVal;
            }
            // Reset liseré courant et changer de joueur
            this.lisereCourant = 0;
            this.joueurCourant = player.equalsIgnoreCase("blanc") ? "noir" : "blanc";
            return;
        }

        //Sionon Parsing du coup normal
        String[] parts = move.split("-");
        int[] start = parseCoordinate(parts[0]);
        int[] end = parseCoordinate(parts[1]);

        // Déplacement de la pièce
        int piece = posPieces[start[0]][start[1]];
        posPieces[start[0]][start[1]] = VIDE;
        posPieces[end[0]][end[1]] = piece;

        // Mise à jour du liseré courant et changement de joueur
        this.lisereCourant = LISERES[end[0]][end[1]];
        this.joueurCourant = player.equalsIgnoreCase("blanc") ? "noir" : "blanc";
    }

    /** Vérifie si la partie est terminée (un joueur a perdu sa licorne).
     *
     * @return true si la partie est terminée, false sinon
     */
    @Override
    public boolean gameOver() {
        boolean lb = false, ln = false;
        for (int[] row : posPieces) {
            for (int p : row) {
                // Les 2 licornes sont-elles encore présentes ?
                if (p == LICORNE_BLANCHE) lb = true;
                if (p == LICORNE_NOIRE) ln = true;
            }
        }
        return !(lb && ln);
    }

    // --- AFFICHAGE DU PLATEAU ---
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
    //4 fichiers de test fournis : test_input1.txt, test_input2.txt, test_input3.txt, test_input4.txt
    public static void main(String[] args) {
        EscampeBoard board = new EscampeBoard();

        // ---------------------------------------------------------------
        // TEST DE CHARGEMENT DES 4 FICHIERS
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
        // On vérifie que la pièce est bien arrivée en F4 (x=4, y=1)
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
