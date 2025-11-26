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

    // --- MÉTHODES DE L'INTERFACE ---

    @Override
    public void setFromFile(String fileName) {
        // TODO : Lire le fichier et remplir posPieces
        // Format : lignes indicées de bas en haut
        System.out.println("Lecture du fichier : " + fileName);
    }

    @Override
    public void saveToFile(String fileName) {
        // TODO : Écrire posPieces dans un fichier
        System.out.println("Sauvegarde vers : " + fileName);
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
