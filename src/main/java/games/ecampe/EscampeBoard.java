package games.ecampe;


import iialib.games.model.escampe.Partie1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class EscampeBoard implements Partie1 {

    // --- CONSTANTES ---
    // LE PLATEAU
    // Représentation des liserés du plateau (1, 2, ou 3).
    private static final int[][] LISERES = {
            {1, 2, 2, 3, 1, 2}, //A
            {3, 1, 3, 1, 3, 2},
            {2, 3, 1, 2, 1, 3},
            {2, 1, 3, 2, 3, 1},
            {1, 3, 1, 3, 1, 2},
            {3, 2, 2, 1, 3, 2}
    };

    //LES PIECES
    public static final int VIDE = 0;
    public static final int PALADIN_BLANC = 1;
    public static final int LICORNE_BLANCHE = 2;
    public static final int PALADIN_NOIR = -1;
    public static final int LICORNE_NOIRE = -2;

    // --- ATTRIBUTS DYNAMIQUES (L'État du jeu) ---

    // Position des pièces
    // 0 = vide
    // 1 = Paladin Blanc, 2 = Licorne Blanche
    // -1 = Paladin Noir, -2 = Licorne Noire
    private int[][] posPieces;

    // Le liseré imposé par le coup précédent (0 si aucun, 1, 2, ou 3)
    private int lisereCourant;

    // Le joueur qui doit jouer ("blanc" ou "noir")
    private String joueurCourant;

    // --- CONSTRUCTEUR ---

    public EscampeBoard() {
        // Initialisation d'un plateau vide 6x6
        this.posPieces = new int[6][6];
        this.lisereCourant = 0; // Pas de contrainte au début
        this.joueurCourant = "blanc"; // Par défaut, les blancs commencent
    }

    // --- GETTERS ET VERIFICATIONS ---

    // Récupère le type de liseré pour une coordonnée (x=colonne, y=ligne)
    // ATTENTION : y=0 correspond ici à la ligne du haut du plateau
    public int getLisere(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return LISERES[y][x];
        }
        return -1; // Erreur
    }

    // Récupère la pièce sur une case (1,2,-1,-2 ou 0)
    // De meme : y=0 correspond ici à la ligne du haut du plateau
    public int getPiece(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return posPieces[y][x];
        }
        return 0; // Considéré vide si hors bornes
    }

    // Vérifie si une coordonnée est dans le plateau
    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < 6 && y >= 0 && y < 6;
    }

    // --- MÉTHODES DE CONVERSION ---

    /**
     * Convertit un entier (représentation interne) en caractère (fichier).
     */
    private char pieceToChar(int piece) {
        switch (piece) {
            case LICORNE_NOIRE: return 'N';
            case PALADIN_NOIR:  return 'n';
            case LICORNE_BLANCHE: return 'B';
            case PALADIN_BLANC:   return 'b';
            default:              return '-';
        }
    }

    /**
     * Convertit un caractère (fichier) en entier (représentation interne).
     */
    private int charToPiece(char c) {
        switch (c) {
            case 'N': return LICORNE_NOIRE;
            case 'n': return PALADIN_NOIR;
            case 'B': return LICORNE_BLANCHE;
            case 'b': return PALADIN_BLANC;
            case '-': return VIDE;
            default: throw new IllegalArgumentException("Caractère inconnu : " + c);
        }
    }

    // --- MÉTHODES DE L'INTERFACE ---
    /**
    * Permet d'initialiser le plateau à partir d'un fichier texte.
    */
    @Override
    public void setFromFile(String fileName) {
        // Initialisation d'un buffer pour lire le fichier
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            // Lecture ligne par ligne
            while ((line = br.readLine()) != null) {
                // Retire les espaces
                line = line.trim();
                // On ignore les lignes vides ou les commentaires % en debut ou en fin de fichier
                //% ABCDEF
                //01 bb---- 01
                //02 -Bb-bb 02
                //03 ------ 03
                //04 ------ 04
                //05 -n-n-n 05
                //06 n-N-n- 06
                //% ABCDEF
                if (line.isEmpty() || line.startsWith("%")) {
                    continue;
                }

                //Exemple de format attendu : "06 n-B--b 06"
                //On vérifie si la ligne commence par un chiffre
                if (Character.isDigit(line.charAt(0))) {
                    // On découpe la ligne par les espaces pour récupérer les parties
                    // Exemple split : ["06", "n-B--b", "06"]
                    //\\s correspond à un espace blanc (espace, tabulation, etc.)
                    String[] parts = line.split("\\s+");

                    //Si on a au moins de caracteres en debut de ligne
                    if (parts.length >= 2) {
                        // On recupere le numero de ligne et le contenu
                        int rowNum = Integer.parseInt(parts[0]); // ex: 6
                        String rowContent = parts[1]; // ex: "n-B--b"

                        // Conversion du numéro de ligne fichier (1..6) vers l'index tableau (5..0)
                        // Rappel : index 0 = Haut (Ligne 6), index 5 = Bas (Ligne 1)
                        int y = 6 - rowNum;

                        // Remplissage de la ligne
                        for (int x = 0; x < 6; x++) {
                            char c = rowContent.charAt(x);
                            this.posPieces[y][x] = charToPiece(c);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur de format de fichier : " + e.getMessage());
        }
    }

    /**
     * Permet de sauvegarder la configuration courante dans un fichier texte
     */
    @Override
    public void saveToFile(String fileName) {
        // Initialisation d'un buffer pour écrire dans le fichier
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            // En-tête (A MODIFIER A NOUVEAU APRES)
            bw.write("% Sauvegarde Escampe");
            bw.newLine();
            bw.write("% ABCDEF");
            bw.newLine();

            // On parcourt le tableau de haut en bas (0 à 5)
            for (int y = 0; y < 6; y++) {
                int rowNum = 6 - y; // 0 -> 06, 5 -> 01

                // Formatage du numéro de ligne "06", "05", etc.
                String numStr = String.format("%02d", rowNum);

                // Ecriture du numero de ligne
                bw.write(numStr + " ");

                // Écriture du contenu de la ligne
                for (int x = 0; x < 6; x++) {
                    bw.write(pieceToChar(posPieces[y][x]));
                }

                bw.write(" " + numStr);
                //fin de ligne
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur d'écriture du fichier : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- MÉTHODES UTILITAIRES POUR LES COORDONNÉES ---

    /**
     * Convertit une coordonnée "A1" en indices tableau [y, x].
     * Rappel : Dans notre tableau, y=0 correspond à la ligne 6 ("06").
     * @return un tableau {y, x} ou null si format invalide.
     */
    private int[] parseCoordinate(String coord) {
        if (coord == null || coord.length() != 2) return null;

        char colChar = coord.charAt(0); // 'A'..'F'
        char rowChar = coord.charAt(1); // '1'..'6'

        if (colChar < 'A' || colChar > 'F') return null;
        if (rowChar < '1' || rowChar > '6') return null;

        int x = colChar - 'A';
        // Conversion '1' -> index 5 (Bas), '6' -> index 0 (Haut)
        int y = 6 - (rowChar - '0');

        return new int[]{y, x};
    }

    // --- VALIDATION DES COUPS (Cœur des règles) ---

    @Override
    public boolean isValidMove(String move, String player) {
        // 1. Gestion du coup spécial "E" (Passer son tour)
        // Règle 5 : Si un joueur ne peut bouger aucune pièce, il saute son tour.
        if (move.equals("E")) {
            // Note : Pour l'instant on accepte "E" syntaxiquement.
            // Une vérification rigoureuse demanderait de vérifier si possiblesMoves() est vide.
            return true;
        }

        // 2. Gestion du placement initial (ex: "C6/A6/...")
        // Si le coup contient "/", c'est une phase d'initialisation.
        if (move.contains("/")) {
            // Pour ce rendu intermédiaire, on peut simplifier ou renvoyer true
            // si on suppose que l'arbitre envoie des setups valides.
            // Une validation complète vérifierait que les pièces sont sur les 2 premières lignes.
            return true;
        }

        // 3. Analyse d'un coup standard "Depart-Arrivee" (ex: "B2-D2")
        String[] parts = move.split("-");
        if (parts.length != 2) return false; // Format incorrect

        int[] start = parseCoordinate(parts[0]);
        int[] end = parseCoordinate(parts[1]);

        if (start == null || end == null) return false; // Coordonnées hors plateau

        int y1 = start[0], x1 = start[1];
        int y2 = end[0], x2 = start[1]; // Oups, petite coquille corrigée ci-dessous
        // Correction :
        x2 = end[1];

        // --- VÉRIFICATIONS LOGIQUES ---

        // A. Vérifier qu'il y a une pièce au départ et qu'elle appartient au joueur
        int piece = posPieces[y1][x1];
        if (piece == VIDE) return false;

        boolean isWhitePiece = (piece > 0);
        boolean isWhitePlayer = player.equalsIgnoreCase("blanc");

        if (isWhitePiece != isWhitePlayer) return false; // On tente de bouger la pièce de l'adversaire

        // B. Vérifier le Liseré Imposé (Règle cruciale d'Escampe)
        int lisereDepart = LISERES[y1][x1];

        // Si lisereCourant != 0, le coup DOIT partir d'une case de ce liseré.
        if (this.lisereCourant != 0 && lisereDepart != this.lisereCourant) {
            return false;
        }

        // C. Vérifier la direction (Orthogonale uniquement)
        boolean isHorizontal = (y1 == y2);
        boolean isVertical = (x1 == x2);

        if (!isHorizontal && !isVertical) return false; // Diagonale interdite

        // D. Vérifier la distance (Doit être égale au liseré de départ)
        int distance = Math.abs((x2 - x1) + (y2 - y1)); // Comme l'un est 0, ça marche
        if (distance != lisereDepart) return false;

        // E. Vérifier le chemin (Pas de saut d'obstacle)
        // On parcourt les cases ENTRE départ et arrivée
        int dx = Integer.compare(x2, x1); // -1, 0, ou 1
        int dy = Integer.compare(y2, y1);

        int currX = x1 + dx;
        int currY = y1 + dy;

        while (currX != x2 || currY != y2) {
            if (posPieces[currY][currX] != VIDE) {
                return false; // Obstacle sur le chemin
            }
            currX += dx;
            currY += dy;
        }

        // F. Vérifier la case d'arrivée
        int targetPiece = posPieces[y2][x2];
        // On ne peut pas manger ses propres pièces
        if (targetPiece != VIDE) {
            boolean isTargetWhite = (targetPiece > 0);
            if (isTargetWhite == isWhitePlayer) return false; // Tir fratricide
        }

        // G. Vérifier Paladin vs Paladin (Optionnel selon interprétation "Imprenable")
        // Le sujet dit "les paladins en particulier étant imprenables" (Fin de partie).
        // Cela signifie généralement qu'on ne PEUT PAS se déplacer sur un paladin adverse.
        if (Math.abs(targetPiece) == PALADIN_BLANC) { // Si la cible est un paladin (1 ou -1)
            return false;
        }

        return true; // Si on arrive ici, le coup est valide !
    }

    @Override
    public String[] possiblesMoves(String player) {
        // TODO : Générer tous les coups valides pour 'player'
        ArrayList<String> possibleMoves = new ArrayList<>();
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                int piece = getPiece(x, y);
                if ((player.equals("blanc") && piece > 0) || (player.equals("noir") && piece < 0)) {
                    //pièce du joueur courant
                    String current_pos = "" + (char)('A' + x) + (6 - y);
                    computePossibleMoves(current_pos,current_pos,lisereCourant,possibleMoves, new HashSet<>());
                }
            }
        }
        return possibleMoves.toArray(new String[0]);
    }

    private void computePossibleMoves(String from, String current_pos, int lisereCount, ArrayList<String> possibleMoves, HashSet<String> visited) {
        int[] coords = parseCoordinate(current_pos);
        //si une piece est sur le chemin et qu'on regarde une case differente de la case de depart
        if (posPieces[coords[0]][coords[1]] != VIDE && !from.equals(current_pos)) {
            return;
        }
        //si on a depasse la distance
        if (lisereCount < 0) return;
        String move = from+'-'+current_pos;
        //si le coup est valide on l'ajoute
        if (lisereCount == 0 && isValidMove(from, current_pos) && !visited.contains(move)) {
            possibleMoves.add(move);
            visited.add(move);
        }
        //sinon on continue la recherche en profondeur
        String[] neighbors = getNeighbors(current_pos);
        for (String n:neighbors){
            computePossibleMoves(from,n,lisereCount-1,possibleMoves,visited);
        }
    }

    private String[] getNeighbors(String pos) {
        char col = pos.charAt(0); // lettre colonne
        int row = Integer.parseInt(pos.substring(1)); // chiffre ligne

        char minCol = 'A', maxCol = 'F';
        int minRow = 1, maxRow = 6;

        ArrayList<String> neighbors = new ArrayList<>();

        // gauche
        if (col > minCol) neighbors.add("" + (char)(col - 1) + row);
        // droite
        if (col < maxCol) neighbors.add("" + (char)(col + 1) + row);
        // haut
        if (row > minRow) neighbors.add("" + col + (row - 1));
        // bas
        if (row < maxRow) neighbors.add("" + col + (row + 1));

        return neighbors.toArray(new String[0]);
    }


    @Override
    public void play(String move, String player) {
        // 1. Gestion du coup "E" (Passe son tour)
        // Règle 5 : Si un joueur passe, l'autre récupère la main et joue ce qu'il veut (liseré libre).
        if (move.equals("E")) {
            this.lisereCourant = 0;
            this.joueurCourant = player.equalsIgnoreCase("blanc") ? "noir" : "blanc";
            return;
        }

        // 2. Gestion du placement initial (ex: "C6/A6/B5/D5/E6/F5")
        // Règle 1 & 2 : Utilisé en début de partie pour placer les pièces.
        if (move.contains("/")) {
            String[] positions = move.split("/");

            // La première coordonnée est toujours celle de la Licorne
            int[] licorneCoord = parseCoordinate(positions[0]);
            int licorneVal = player.equalsIgnoreCase("blanc") ? LICORNE_BLANCHE : LICORNE_NOIRE;
            if (licorneCoord != null) {
                posPieces[licorneCoord[0]][licorneCoord[1]] = licorneVal;
            }

            // Les coordonnées suivantes sont celles des Paladins
            int paladinVal = player.equalsIgnoreCase("blanc") ? PALADIN_BLANC : PALADIN_NOIR;
            for (int i = 1; i < positions.length; i++) {
                int[] palCoord = parseCoordinate(positions[i]);
                if (palCoord != null) {
                    posPieces[palCoord[0]][palCoord[1]] = paladinVal;
                }
            }

            // Après une phase de placement, le jeu commence (ou continue) sans contrainte immédiate.
            // (Si Noir vient de placer, Blanc placera. Si Blanc vient de placer, il jouera le premier coup LIBREMENT ).
            this.lisereCourant = 0;
            this.joueurCourant = player.equalsIgnoreCase("blanc") ? "noir" : "blanc";
            return;
        }

        // 3. Coup standard "Départ-Arrivée" (ex: "B2-D2")
        String[] parts = move.split("-");
        int[] start = parseCoordinate(parts[0]);
        int[] end = parseCoordinate(parts[1]);

        // On récupère la pièce qui bouge
        int piece = posPieces[start[0]][start[1]];

        // On vide la case de départ
        posPieces[start[0]][start[1]] = VIDE;

        // On pose la pièce sur la case d'arrivée
        // Note : Si la case contenait une Licorne adverse, elle est écrasée (capturée).
        // La validation (isValidMove) garantit déjà qu'on ne mange pas un Paladin.
        posPieces[end[0]][end[1]] = piece;

        // 4. Mise à jour du Liseré Imposé pour le PROCHAIN coup
        // Règle 3: "la pièce jouée doit partir d'une case ayant le même liseré que celle
        // sur laquelle l'autre joueur a posé sa propre pièce au tour précédent."
        this.lisereCourant = LISERES[end[0]][end[1]];

        // Changement de joueur
        this.joueurCourant = player.equalsIgnoreCase("blanc") ? "noir" : "blanc";

        // Debug optionnel pour suivre la partie
        // System.out.println("Coup joué : " + move + " | Prochain liseré imposé : " + this.lisereCourant);
    }

    @Override
    public boolean gameOver() {
        boolean licorneBlancheEnVie = false;
        boolean licorneNoireEnVie = false;

        // On parcourt tout le plateau pour chercher les licornes
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                int piece = posPieces[y][x];

                if (piece == LICORNE_BLANCHE) {
                    licorneBlancheEnVie = true;
                } else if (piece == LICORNE_NOIRE) {
                    licorneNoireEnVie = true;
                }
            }
        }

        // La partie est finie si l'une des deux licornes n'est plus sur le plateau
        return !(licorneBlancheEnVie && licorneNoireEnVie);
    }

    // --- PROGRAMME PRINCIPAL ---

    // --- PROGRAMME PRINCIPAL DE TEST ---
    public static void main(String[] args) {
        EscampeBoard board = new EscampeBoard();
        System.out.println("--- Test Partie 6 : GameOver ---");

        // 1. Simulation d'un plateau avec les deux licornes
        board.posPieces[0][0] = LICORNE_NOIRE;
        board.posPieces[5][5] = LICORNE_BLANCHE;
        System.out.println("Deux licornes présentes -> GameOver ? " + board.gameOver());
        // Attendu : false

        // 2. Simulation d'une capture (on supprime la licorne blanche)
        board.posPieces[5][5] = VIDE;
        System.out.println("Licorne blanche capturée -> GameOver ? " + board.gameOver());
        // Attendu : true
    }
}
