package games.ecampe;


import iialib.games.model.escampe.Partie1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

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

    @Override
    public boolean isValidMove(String move, String player) {
        // TODO : Vérifier syntaxe, liseré, distance, obstacles
        return false;
    }

    @Override
    public String[] possiblesMoves(String player) {
        // TODO : Générer tous les coups valides pour 'player'
        return new String[0];
    }

    @Override
    public void play(String move, String player) {
        // TODO : Mettre à jour posPieces et lisereCourant
        System.out.println("Joueur " + player + " joue " + move);
    }

    @Override
    public boolean gameOver() {
        // TODO : Vérifier la présence des deux licornes (2 et -2)
        return false;
    }

    // --- PROGRAMME PRINCIPAL ---

    public static void main(String[] args) {
        EscampeBoard board = new EscampeBoard();
        System.out.println("Plateau initialisé.");

        // Test simple d'affichage (temporaire)
        System.out.println("Liseré en A1 (0,0) : " + LISERES[0][0]);
    }
}
